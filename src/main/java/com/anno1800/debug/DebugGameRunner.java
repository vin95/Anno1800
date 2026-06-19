package com.anno1800.debug;

import com.anno1800.agents.AgentImpl.WeightedScoringAgent;
import com.anno1800.agents.Agent;
import com.anno1800.agents.AgentImpl.ScoringAgent;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.player.ProducedGood;
import com.anno1800.game.rewards.Reward;
import com.anno1800.game.state.GameState;
import com.anno1800.game.tiles.Factory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Runs a fully-simulated Anno 1800 debug game with seeded reproducibility.
 *
 * - Kartenstapel, Startspieler und Agenten-Strategien werden durch den Seed bestimmt.
 * - Nach jeder Aktion wird der GameState als JSON in game-states/Debuggame-XX/ gespeichert.
 * - Das Spiel bricht nach maxRounds ab (falls kein natürliches Spielende eintritt).
 *
 * Aufruf via Gradle:
 *   ./gradlew.bat debugGame
 * Oder per Skript:
 *   .\Debugging\debug-game.ps1
 */
public class DebugGameRunner {

    private static final String GAME_STATES_DIR = "game-states";

    /** Alle verfügbaren Strategien. Die Auswahl erfolgt seed-basiert. */
    private static final String[] STRATEGY_NAMES = {
        "shipBuilding",
        "tradeShipFocus",
        "explorationFocus",
        "residentMassProduction",
        "eliteResidentProduction",
        "adaptiveResidentStrategy",
        "factoryVariety",
        "balanced"
    };

    /**
     * Erstellt den passenden Agent zur Strategie.
     */
    private static WeightedScoringAgent createAgent(String strategy, String name, long agentSeed) {
        return switch (strategy) {
            case "shipBuilding"            -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.shipBuilding(), agentSeed);
            case "tradeShipFocus"          -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.tradeShipFocus(), agentSeed);
            case "explorationFocus"        -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.explorationFocus(), agentSeed);
            case "residentMassProduction"  -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.residentMassProduction(), agentSeed);
            case "eliteResidentProduction" -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.eliteResidentProduction(), agentSeed);
            case "adaptiveResidentStrategy"-> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.adaptiveResidentStrategy(), agentSeed);
            case "factoryVariety"          -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.factoryVariety(), agentSeed);
            default                        -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.balanced(), agentSeed);
        };
    }

    /**
     * Hauptmethode – wird vom Gradle-Task debugGame aufgerufen.
     *
    * Args: [seed] [numPlayers] [maxRounds]
    * Defaults: seed=42, numPlayers=3, maxRounds=100
     */
    public static void main(String[] args) {
        long seed       = args.length > 0 ? Long.parseLong(args[0]) : 42L;
        int numPlayers  = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        int maxRounds   = args.length > 2 ? Integer.parseInt(args[2]) : 100;

        System.out.println("=".repeat(72));
        System.out.println("  Anno 1800 - Debug Game (Agent-Simulation)");
        System.out.printf ("  Seed: %d  |  Spieler: %d  |  Max. Runden: %d%n", seed, numPlayers, maxRounds);
        System.out.println("=".repeat(72));

        // ---------------------------------------------------------------
        // Strategien seed-basiert auswählen
        // ---------------------------------------------------------------
        Random stratRng = new Random(seed);
        String[] chosenStrategies = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            chosenStrategies[i] = STRATEGY_NAMES[stratRng.nextInt(STRATEGY_NAMES.length)];
        }

        // ---------------------------------------------------------------
        // Spiel erstellen (seeded: Kartenstapel + Startspieler)
        // ---------------------------------------------------------------
        Game game = new Game(numPlayers, seed, maxRounds);

        // ---------------------------------------------------------------
        // Agents zuweisen (jeder Agent bekommt seed + Spielerindex als eigenen Seed)
        // ---------------------------------------------------------------
        System.out.println("\nAgenten:");
        for (int i = 0; i < numPlayers; i++) {
            long agentSeed = seed * 31L + i;
            WeightedScoringAgent agent = createAgent(chosenStrategies[i], "Spieler-" + (i + 1), agentSeed);
            game.setAgent(i, agent);
            System.out.printf("  Spieler %d: %s  (Strategie: %s)%n", i + 1, agent.getName(), chosenStrategies[i]);
        }

        // ---------------------------------------------------------------
        // Ausgabe-Verzeichnis anlegen
        // ---------------------------------------------------------------
        String gameDir = createGameDirectory();
        if (gameDir != null) {
            System.out.println("\nGame-States werden gespeichert in: " + gameDir);
        }

        // ---------------------------------------------------------------
        // After-Action-Callback: speichert nach jeder Aktion einen State
        // ---------------------------------------------------------------
        AtomicInteger actionCounter = new AtomicInteger(0);
        AtomicReference<Map<Integer, List<String>>> previousResidentCardsRef =
            new AtomicReference<>(snapshotResidentCardDetailsByPlayer(game));
        PrintStream actionLogOut = System.out;

        BiConsumer<Action, GameState> stateCallback = (action, state) -> {
            int n = actionCounter.incrementAndGet();
            String executedAction = action == null ? null : action.toString();
            String executedByPlayer = null;
            String executedActionDetails = null;
            String agentStrategyName = null;
            List<ScoringAgent.MainActionScore> mainActionScores = List.of();
            int playerIndex = state.currentPlayerIndex();
            if (playerIndex >= 0 && playerIndex < state.players().size()) {
                executedByPlayer = state.players().get(playerIndex).name();
                executedActionDetails = buildExecutedActionDetails(
                    action,
                    game,
                    playerIndex,
                    executedByPlayer,
                    previousResidentCardsRef.get()
                );
                agentStrategyName = chosenStrategies[playerIndex];
                Agent actingAgent = game.getAgent(playerIndex);
                if (actingAgent instanceof ScoringAgent scoringAgent) {
                    mainActionScores = scoringAgent.getLastMainActionScores();
                }
            }

            // Keep simulation output concise: one line per executed action.
            actionLogOut.printf(
                "Action %d | Round %d | %s -> %s%n",
                n,
                state.round(),
                executedByPlayer == null ? "UnknownPlayer" : executedByPlayer,
                executedAction == null ? "UnknownAction" : executedAction
            );

            saveGameState(
                state,
                game,
                "action_" + n,
                gameDir,
                executedAction,
                executedByPlayer,
                executedActionDetails,
                agentStrategyName,
                mainActionScores
            );

            previousResidentCardsRef.set(snapshotResidentCardDetailsByPlayer(game));
        };
        game.setAfterActionCallback(stateCallback);

        // Initialzustand speichern
        saveGameState(game.getState(), game, "initial", gameDir, null, null, null, null, List.of());

        // ---------------------------------------------------------------
        // Spiel starten
        // ---------------------------------------------------------------
        System.out.println();
        runSilenced(game::start);

        System.out.println("\nDebug-Spiel beendet. " + actionCounter.get() + " Aktionen wurden aufgezeichnet.");
        if (gameDir != null) {
            System.out.println("States gespeichert in: " + gameDir);
        }
    }

    /**
     * Runs an action while suppressing noisy game/action console output.
     */
    private static void runSilenced(Runnable action) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try (PrintStream nullStream = new PrintStream(OutputStream.nullOutputStream())) {
            System.setOut(nullStream);
            System.setErr(nullStream);
            action.run();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    private static String summarizeConsumedGoods(List<ProducedGood> consumedGoods) {
        if (consumedGoods == null || consumedGoods.isEmpty()) {
            return null;
        }

        List<String> segments = new ArrayList<>();
        int tradeChips = 0;
        int explorerChips = 0;

        for (ProducedGood producedGood : consumedGoods) {
            String sourceText;
            switch (producedGood.source()) {
                case ProducedGood.GoodSource.Produced(var factory, var resident) ->
                    sourceText = String.format("produziert (%s, Bewohnerstufe %d)", factory.getType(), resident.getPopulationLevel());
                case ProducedGood.GoodSource.Traded(var fromPlayer, var chipCost) -> {
                    tradeChips += chipCost;
                    String playerText = fromPlayer >= 0 ? "Spieler " + (fromPlayer + 1) : "Spieler unbekannt";
                    sourceText = String.format("gehandelt (%s, Kosten %d Chip%s)", playerText, chipCost, chipCost == 1 ? "" : "s");
                }
                case ProducedGood.GoodSource.TradedWithExplorer(var fromPlayer, var tradeChipCost, var explorerChipCost) -> {
                    explorerChips += explorerChipCost;
                    String playerText = fromPlayer >= 0 ? "Spieler " + (fromPlayer + 1) : "Spieler unbekannt";
                    sourceText = String.format(
                        "gehandelt (%s, Kosten %d Explorerchips via ExplorerTrader ~= %d Tradechips)",
                        playerText,
                        explorerChipCost,
                        tradeChipCost
                    );
                }
                case ProducedGood.GoodSource.Imported(var chip) -> {
                    explorerChips += chip;
                    sourceText = String.format("importiert (Explorerchips %d)", chip);
                }
                case ProducedGood.GoodSource.FromReward() -> sourceText = "aus Belohnung";
                case ProducedGood.GoodSource.Other(var desc) -> sourceText = desc;
            }
            segments.add(String.format("1x %s [%s]", producedGood.good(), sourceText));
        }

        if (tradeChips > 0 || explorerChips > 0) {
            segments.add(String.format("Chips verwendet: Tradechips=%d, Explorerchips=%d", tradeChips, explorerChips));
        }

        return String.join("; ", segments);
    }

    private static String buildExecutedActionDetails(
        Action action,
        Game game,
        int playerIndex,
        String executedByPlayer,
        Map<Integer, List<String>> previousResidentCardsByPlayer
    ) {
        if (action == null || playerIndex < 0 || playerIndex >= game.getPlayers().length) {
            return null;
        }

        PlayerBoard board = game.getPlayers()[playerIndex].getPlayerBoard();

        return switch (action) {
            case Action.TradeGoods(Goods good, int partnerPlayerIndex) -> {
                Player[] players = game.getPlayers();
                if (partnerPlayerIndex < 0 || partnerPlayerIndex >= players.length) {
                    yield String.format("Handel: 1x %s (Tradechips: unbekannt, mit Spieler %d)", good, partnerPlayerIndex + 1);
                }

                Player selectedPartner = players[partnerPlayerIndex];
                int cheapestTradeCosts = Integer.MAX_VALUE;
                for (Factory factory : selectedPartner.getPlayerBoard().getAllActiveFactories()) {
                    if (factory != null && factory.produces() == good) {
                        cheapestTradeCosts = Math.min(cheapestTradeCosts, factory.getTradeCosts());
                    }
                }

                if (cheapestTradeCosts == Integer.MAX_VALUE) {
                    yield String.format("Handel: 1x %s (Tradechips: unbekannt, mit Spieler %d)", good, partnerPlayerIndex + 1);
                }
                yield String.format(
                    "Handel: 1x %s (Tradechips: %d, mit Spieler %d); Mitspieler erhaelt: %dx gold.png",
                    good,
                    cheapestTradeCosts,
                    partnerPlayerIndex + 1,
                    cheapestTradeCosts
                );
            }
            case Action.BuildFactory ignored -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                String factoryLabel = formatFactoryForDetails(ignored.factory());
                if (factoryLabel == null) {
                    yield summary == null ? null : "Verbrauch fuer Aktion: " + summary;
                }
                if (summary == null) {
                    yield "Fabrik gebaut: " + factoryLabel;
                }
                yield "Fabrik gebaut: " + factoryLabel + "; Verbrauch fuer Aktion: " + summary;
            }
            case Action.OverbuildDefaultFactory overbuildDefaultFactory -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                String factoryLabel = formatFactoryForDetails(overbuildDefaultFactory.newFactory());
                if (factoryLabel == null) {
                    yield summary == null ? null : "Verbrauch fuer Aktion: " + summary;
                }
                if (summary == null) {
                    yield "Fabrik gebaut: " + factoryLabel;
                }
                yield "Fabrik gebaut: " + factoryLabel + "; Verbrauch fuer Aktion: " + summary;
            }
            case Action.BuildShipyard ignored -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                if (summary == null) {
                    yield null;
                }
                yield "Verbrauch fuer Aktion: " + summary;
            }
            case Action.BuildShips ignored -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                if (summary == null) {
                    yield null;
                }
                yield "Verbrauch fuer Aktion: " + summary;
            }
            case Action.UpgradeResident ignored -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                if (summary == null) {
                    yield null;
                }
                yield "Verbrauch fuer Aktion: " + summary;
            }
            case Action.SettleResident ignored -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                List<String> previousCards = previousResidentCardsByPlayer == null
                    ? List.of()
                    : previousResidentCardsByPlayer.getOrDefault(playerIndex, List.of());
                List<String> currentCards = currentResidentCardDetailsForPlayer(game, playerIndex);
                List<String> newlyDrawnCards = findNewEntries(previousCards, currentCards);

                String cardDrawText = null;
                if (!newlyDrawnCards.isEmpty()) {
                    cardDrawText = "Gezogene Einwohnerkarte: " + String.join(" | ", newlyDrawnCards);
                }

                if (cardDrawText == null && summary == null) {
                    yield null;
                }
                if (cardDrawText == null) {
                    yield "Verbrauch fuer Aktion: " + summary;
                }
                if (summary == null) {
                    yield cardDrawText;
                }
                yield cardDrawText + "; Verbrauch fuer Aktion: " + summary;
            }
            case Action.FulfillNeeds ignored -> {
                String summary = summarizeConsumedGoods(board.getLastConsumedGoods());
                if (summary == null) {
                    yield null;
                }
                yield "Verbrauch fuer Aktion: " + summary;
            }
            default -> null;
        };
    }

    private static String formatFactoryForDetails(Factory factory) {
        if (factory == null || factory.getType() == null) {
            return null;
        }

        String displayName = factory.getType().getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            return null;
        }

        String colorToken = inferFactoryColorSquare(factory);
        if (colorToken == null) {
            return displayName;
        }

        return displayName + " " + colorToken;
    }

    private static String inferFactoryColorSquare(Factory factory) {
        String typeName = factory.getType().name();
        if (typeName.endsWith("_BLUE")) {
            return "blue_square";
        }
        if (typeName.endsWith("_RED")) {
            return "red_square";
        }
        if (typeName.endsWith("_GREEN")) {
            return "green_square";
        }
        if (typeName.endsWith("_YELLOW")) {
            return "yellow_square";
        }
        if (typeName.endsWith("_ORANGE")) {
            return "orange_square";
        }
        if (typeName.endsWith("_PURPLE")) {
            return "purple_square";
        }
        if (typeName.endsWith("_BLACK")) {
            return "black_square";
        }
        if (typeName.endsWith("_WHITE")) {
            return "white_square";
        }
        return null;
    }

    private static Map<Integer, List<String>> snapshotResidentCardDetailsByPlayer(Game game) {
        Map<Integer, List<String>> snapshot = new HashMap<>();
        if (game == null || game.getPlayers() == null) {
            return snapshot;
        }

        Player[] players = game.getPlayers();
        for (int i = 0; i < players.length; i++) {
            snapshot.put(i, currentResidentCardDetailsForPlayer(game, i));
        }
        return snapshot;
    }

    private static List<String> currentResidentCardDetailsForPlayer(Game game, int playerIndex) {
        if (game == null || game.getPlayers() == null || playerIndex < 0 || playerIndex >= game.getPlayers().length) {
            return List.of();
        }

        Player player = game.getPlayers()[playerIndex];
        if (player == null) {
            return List.of();
        }

        PlayerBoard playerBoard = player.getPlayerBoard();
        if (playerBoard == null) {
            return List.of();
        }

        List<ResidentCard> cards = playerBoard.getResidentCards();
        List<String> details = new ArrayList<>(cards.size());
        for (ResidentCard card : cards) {
            details.add(formatResidentCardForDetails(card));
        }
        return details;
    }

    private static List<String> findNewEntries(List<String> previousValues, List<String> currentValues) {
        if (currentValues == null || currentValues.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> previousCounts = new HashMap<>();
        if (previousValues != null) {
            for (String value : previousValues) {
                if (value == null) {
                    continue;
                }
                previousCounts.put(value, previousCounts.getOrDefault(value, 0) + 1);
            }
        }

        List<String> added = new ArrayList<>();
        for (String value : currentValues) {
            if (value == null) {
                continue;
            }
            int remaining = previousCounts.getOrDefault(value, 0);
            if (remaining > 0) {
                previousCounts.put(value, remaining - 1);
            } else {
                added.add(value);
            }
        }

        return added;
    }

    private static String formatResidentCardForDetails(ResidentCard card) {
        if (card == null) {
            return "residentcard_lv_2.png => -";
        }

        String cardIcon = "residentcard_lv_" + card.populationLevel() + ".png";
        Goods[] needs = card.needs();
        String needsText;
        if (needs == null || needs.length == 0) {
            needsText = "-";
        } else {
            List<String> needIcons = new ArrayList<>(needs.length);
            for (Goods need : needs) {
                if (need == null) {
                    continue;
                }
                needIcons.add(need.name().toLowerCase(Locale.ROOT) + ".png");
            }
            needsText = needIcons.isEmpty() ? "-" : String.join(" , ", needIcons);
        }

        return cardIcon + " " + needsText + " => " + formatRewardForExport(card.reward());
    }

    // ===================================================================
    // Hilfsmethoden für Verzeichnis-Erstellung und State-Speicherung
    // ===================================================================

    private static String createGameDirectory() {
        try {
            Path baseDir = Paths.get(GAME_STATES_DIR);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            int num = 1;
            Path dir;
            do {
                dir = baseDir.resolve(String.format("Debuggame-%02d", num));
                num++;
            } while (Files.exists(dir));
            Files.createDirectories(dir);
            return dir.toString();
        } catch (IOException e) {
            System.err.println("Warnung: Konnte Spielverzeichnis nicht erstellen: " + e.getMessage());
            return null;
        }
    }

    private static void saveGameState(
        GameState state,
        Game game,
        String label,
        String dir,
        String executedAction,
        String executedByPlayer,
        String executedActionDetails,
        String agentStrategyName,
        List<ScoringAgent.MainActionScore> mainActionScores
    ) {
        if (dir == null) return;
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/%s_%s.json", dir, label, timestamp);

            try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
                w.println("{");
                w.printf("  \"timestamp\": \"%s\",%n", state.timestamp());
                w.printf("  \"round\": %d,%n", state.round());
                w.printf("  \"currentPlayer\": %d,%n", state.currentPlayerIndex());
                w.printf("  \"label\": \"%s\",%n", label);
                if (executedAction == null) {
                    w.println("  \"executedAction\": null,");
                } else {
                    String escapedAction = executedAction.replace("\\", "\\\\").replace("\"", "\\\"");
                    w.printf("  \"executedAction\": \"%s\",%n", escapedAction);
                }
                if (executedByPlayer == null) {
                    w.println("  \"executedByPlayer\": null,");
                } else {
                    String escapedPlayer = executedByPlayer.replace("\\", "\\\\").replace("\"", "\\\"");
                    w.printf("  \"executedByPlayer\": \"%s\",%n", escapedPlayer);
                }
                if (executedActionDetails == null) {
                    w.println("  \"executedActionDetails\": null,");
                } else {
                    String escapedDetails = executedActionDetails.replace("\\", "\\\\").replace("\"", "\\\"");
                    w.printf("  \"executedActionDetails\": \"%s\",%n", escapedDetails);
                }
                if (agentStrategyName == null) {
                    w.println("  \"agentStrategyName\": null,");
                } else {
                    String escapedStrategy = agentStrategyName.replace("\\", "\\\\").replace("\"", "\\\"");
                    w.printf("  \"agentStrategyName\": \"%s\",%n", escapedStrategy);
                }
                if (mainActionScores == null || mainActionScores.isEmpty()) {
                    w.println("  \"agentMainActionScores\": [],");
                } else {
                    w.println("  \"agentMainActionScores\": [");
                    for (int i = 0; i < mainActionScores.size(); i++) {
                        ScoringAgent.MainActionScore score = mainActionScores.get(i);
                        String escapedMainAction = score.mainAction().replace("\\", "\\\\").replace("\"", "\\\"");
                        String escapedVariant = score.bestActionVariant() == null
                            ? ""
                            : score.bestActionVariant().replace("\\", "\\\\").replace("\"", "\\\"");
                        String scoreText = String.format(Locale.US, "%.4f", score.score());
                        w.println("    {");
                        w.printf("      \"mainAction\": \"%s\",%n", escapedMainAction);
                        w.printf("      \"score\": %s,%n", scoreText);
                        w.printf("      \"selected\": %s,%n", score.selected() ? "true" : "false");
                        w.printf("      \"bestActionVariant\": \"%s\"%n", escapedVariant);
                        w.print("    }");
                        w.println(i < mainActionScores.size() - 1 ? "," : "");
                    }
                    w.println("  ],");
                }

                // Active objective cards for this game.
                w.println("  \"objectiveCards\": [");
                List<ObjectiveCard> objectiveCards = game == null ? List.of() : game.getActiveObjectiveCards();
                for (int i = 0; i < objectiveCards.size(); i++) {
                    ObjectiveCard objectiveCard = objectiveCards.get(i);
                    w.println("    {");
                    w.printf("      \"title\": \"%s\",%n", escapeJson(objectiveCard.getTitle()));
                    w.printf("      \"description\": \"%s\"%n", escapeJson(objectiveCard.getDescription()));
                    w.print("    }");
                    w.println(i < objectiveCards.size() - 1 ? "," : "");
                }
                w.println("  ],");

                // Board State
                w.println("  \"boardState\": {");
                w.println("    \"factories\": {");
                {
                    java.util.Map<com.anno1800.data.gamedata.Producers, Integer> blueprints = state.boardState().factories().blueprints();
                    java.util.List<com.anno1800.data.gamedata.Producers> keys = new java.util.ArrayList<>(blueprints.keySet());
                    for (int fi = 0; fi < keys.size(); fi++) {
                        com.anno1800.data.gamedata.Producers p = keys.get(fi);
                        String key = p.name().toLowerCase();
                        w.printf("      \"%s\": %d%s%n", key, blueprints.get(p), fi < keys.size() - 1 ? "," : "");
                    }
                }
                w.println("    },");
                w.println("    \"shipyards\": {");
                w.printf ("      \"level1\": %d,%n", state.boardState().shipyards().level1());
                w.printf ("      \"level2\": %d,%n", state.boardState().shipyards().level2());
                w.printf ("      \"level3\": %d%n",  state.boardState().shipyards().level3());
                w.println("    },");
                w.println("    \"ships\": {");
                w.println("      \"tradeShips\": {");
                w.printf ("        \"level1\": %d,%n", state.boardState().ships().tradeShips().level1());
                w.printf ("        \"level2\": %d,%n", state.boardState().ships().tradeShips().level2());
                w.printf ("        \"level3\": %d%n",  state.boardState().ships().tradeShips().level3());
                w.println("      },");
                w.println("      \"explorerShips\": {");
                w.printf ("        \"level1\": %d,%n", state.boardState().ships().explorerShips().level1());
                w.printf ("        \"level2\": %d,%n", state.boardState().ships().explorerShips().level2());
                w.printf ("        \"level3\": %d%n",  state.boardState().ships().explorerShips().level3());
                w.println("      }");
                w.println("    },");
                w.println("    \"cards\": {");
                w.printf ("      \"residentStack1\": %d,%n",     state.boardState().residentCards().stack1Size());
                w.printf ("      \"residentStack2\": %d,%n",     state.boardState().residentCards().stack2Size());
                w.printf ("      \"residentStack3\": %d,%n",     state.boardState().residentCards().stack3Size());
                w.printf ("      \"totalResidentCards\": %d,%n", state.boardState().residentCards().totalAvailable());
                w.printf ("      \"expeditionCards\": %d%n",     state.boardState().expeditions().stackSize());
                w.println("    },");
                w.println("    \"islands\": {");
                w.printf ("      \"oldWorldIslands\": %d,%n", state.boardState().islands().oldWorldSize());
                w.printf ("      \"newWorldIslands\": %d%n",  state.boardState().islands().newWorldSize());
                w.println("    },");
                w.println("    \"populationPool\": {");
                w.printf ("      \"farmers\": %d,%n",   state.boardState().resources().farmers());
                w.printf ("      \"workers\": %d,%n",   state.boardState().resources().workers());
                w.printf ("      \"artisans\": %d,%n",  state.boardState().resources().artisans());
                w.printf ("      \"engineers\": %d,%n", state.boardState().resources().engineers());
                w.printf ("      \"investors\": %d%n",  state.boardState().resources().investors());
                w.println("    },");
                w.println("    \"resources\": {");
                w.printf ("      \"goldPool\": %d,%n",     state.boardState().resources().gold());
                w.printf ("      \"tradeChips\": %d,%n",   state.boardState().resources().tradeChips());
                w.printf ("      \"explorerChips\": %d%n", state.boardState().resources().explorerChips());
                w.println("    }");
                w.println("  },");

                // Player States
                w.println("  \"players\": [");
                for (int i = 0; i < state.players().size(); i++) {
                    GameState.PlayerState p = state.players().get(i);
                    w.println("    {");
                    w.printf ("      \"name\": \"%s\",%n",     p.name());
                    w.printf ("      \"position\": %d,%n",     p.position());
                    w.println("      \"tiles\": {");
                    // landtiles
                    w.print("        \"landtiles\": [");
                    for (int ti = 0; ti < p.tiles().landtiles().size(); ti++) {
                        String t = p.tiles().landtiles().get(ti).replace("\"", "\\\"");
                        w.print("\"" + t + "\"");
                        if (ti < p.tiles().landtiles().size() - 1) w.print(", ");
                    }
                    w.println("],");
                    // coasttiles
                    w.print("        \"coasttiles\": [");
                    for (int ti = 0; ti < p.tiles().coasttiles().size(); ti++) {
                        String t = p.tiles().coasttiles().get(ti).replace("\"", "\\\"");
                        w.print("\"" + t + "\"");
                        if (ti < p.tiles().coasttiles().size() - 1) w.print(", ");
                    }
                    w.println("],");
                    // seatiles
                    w.print("        \"seatiles\": [");
                    for (int ti = 0; ti < p.tiles().seatiles().size(); ti++) {
                        String t = p.tiles().seatiles().get(ti).replace("\"", "\\\"");
                        w.print("\"" + t + "\"");
                        if (ti < p.tiles().seatiles().size() - 1) w.print(", ");
                    }
                    w.println("]");
                    w.println("      },");
                    w.println("      \"buildings\": {");
                    w.printf ("        \"factories\": %d,%n",  p.buildings().factoryCount());
                    w.printf ("        \"shipyards\": %d%n",   p.buildings().shipyardCount());
                    w.println("      },");
                    w.println("      \"ships\": {");
                    w.printf ("        \"tradeShips\": %d,%n",   p.ships().tradeShips().totalCount());
                    w.printf ("        \"explorerShips\": %d%n", p.ships().explorerShips().totalCount());
                    w.println("      },");
                    w.println("      \"resources\": {");
                    w.printf ("        \"gold\": %d,%n",           p.resources().gold());
                    w.printf ("        \"tradeChips\": %d,%n",     p.resources().availableTradeChips());
                    w.printf ("        \"explorerChips\": %d%n",   p.resources().availableExplorerChips());
                    w.println("      },");
                    w.println("      \"cards\": {");
                    w.printf ("        \"residentCards\": %d,%n",   p.cards().residentCardCount());
                    Player livePlayer = (game != null && i < game.getPlayers().length) ? game.getPlayers()[i] : null;
                    List<ResidentCard> residentCards = livePlayer == null
                        ? List.of()
                        : livePlayer.getPlayerBoard().getResidentCards();
                    w.println("        \"residentCardDetails\": [");
                    for (int cardIndex = 0; cardIndex < residentCards.size(); cardIndex++) {
                        ResidentCard residentCard = residentCards.get(cardIndex);
                        w.println("          {");
                        w.printf ("            \"populationLevel\": %d,%n", residentCard.populationLevel());
                        w.println("            \"needs\": [");
                        Goods[] needs = residentCard.needs();
                        for (int needIndex = 0; needIndex < needs.length; needIndex++) {
                            Goods need = needs[needIndex];
                            w.printf(
                                "              \"%s\"%s%n",
                                escapeJson(need.getDisplayName()),
                                needIndex < needs.length - 1 ? "," : ""
                            );
                        }
                        w.println("            ],");
                        w.printf("            \"reward\": \"%s\"%n", escapeJson(formatRewardForExport(residentCard.reward())));
                        w.print("          }");
                        w.println(cardIndex < residentCards.size() - 1 ? "," : "");
                    }
                    w.println("        ]");
                    w.println("      },");
                    w.println("      \"residents\": {");
                    w.printf ("        \"total\": %d,%n",          p.residents().count());

                    int[] levels = new int[6];
                    int onBoard = 0, working = 0, fit = 0, exhausted = 0;
                    int[] fitByLevel = new int[6];
                    int[] workingByLevel = new int[6];
                    int[] exhaustedByLevel = new int[6];
                    for (var r : p.residents().residents()) {
                        if (r.level() >= 1 && r.level() <= 5) levels[r.level()]++;
                        switch (r.status()) {
                            case "ON_BOARD"  -> onBoard++;
                            case "AT_WORK"   -> {
                                working++;
                                if (r.level() >= 1 && r.level() <= 5) {
                                    workingByLevel[r.level()]++;
                                }
                            }
                            case "FIT"       -> {
                                fit++;
                                if (r.level() >= 1 && r.level() <= 5) {
                                    fitByLevel[r.level()]++;
                                }
                            }
                            case "EXHAUSTED" -> {
                                exhausted++;
                                if (r.level() >= 1 && r.level() <= 5) {
                                    exhaustedByLevel[r.level()]++;
                                }
                            }
                        }
                    }
                    w.println("        \"byLevel\": {");
                    w.printf ("          \"level1\": %d,%n", levels[1]);
                    w.printf ("          \"level2\": %d,%n", levels[2]);
                    w.printf ("          \"level3\": %d,%n", levels[3]);
                    w.printf ("          \"level4\": %d,%n", levels[4]);
                    w.printf ("          \"level5\": %d%n",  levels[5]);
                    w.println("        },");
                    w.println("        \"byStatus\": {");
                    w.printf ("          \"onBoard\": %d,%n",   onBoard);
                    w.printf ("          \"working\": %d,%n",   working);
                    w.printf ("          \"fit\": %d,%n",       fit);
                    w.printf ("          \"exhausted\": %d%n",  exhausted);
                    w.println("        },");
                    w.println("        \"byStatusByLevel\": {");
                    w.println("          \"fit\": {");
                    w.printf ("            \"level1\": %d,%n", fitByLevel[1]);
                    w.printf ("            \"level2\": %d,%n", fitByLevel[2]);
                    w.printf ("            \"level3\": %d,%n", fitByLevel[3]);
                    w.printf ("            \"level4\": %d,%n", fitByLevel[4]);
                    w.printf ("            \"level5\": %d%n",  fitByLevel[5]);
                    w.println("          },");
                    w.println("          \"working\": {");
                    w.printf ("            \"level1\": %d,%n", workingByLevel[1]);
                    w.printf ("            \"level2\": %d,%n", workingByLevel[2]);
                    w.printf ("            \"level3\": %d,%n", workingByLevel[3]);
                    w.printf ("            \"level4\": %d,%n", workingByLevel[4]);
                    w.printf ("            \"level5\": %d%n",  workingByLevel[5]);
                    w.println("          },");
                    w.println("          \"exhausted\": {");
                    w.printf ("            \"level1\": %d,%n", exhaustedByLevel[1]);
                    w.printf ("            \"level2\": %d,%n", exhaustedByLevel[2]);
                    w.printf ("            \"level3\": %d,%n", exhaustedByLevel[3]);
                    w.printf ("            \"level4\": %d,%n", exhaustedByLevel[4]);
                    w.printf ("            \"level5\": %d%n",  exhaustedByLevel[5]);
                    w.println("          }");
                    w.println("        }");
                    w.println("      }");
                        // Discovered islands (old and new world) - populated from GameState.PlayerState
                        w.println("      ,");
                        w.println("      \"discoveredIslands\": {");
                        // Old World
                        w.print("        \"oldWorld\": [");
                        for (int oi = 0; oi < p.discoveredOldWorldIslands().size(); oi++) {
                            w.print("\"" + escapeJson(p.discoveredOldWorldIslands().get(oi)) + "\"");
                            if (oi < p.discoveredOldWorldIslands().size() - 1) w.print(", ");
                        }
                        w.println("],");
                        // New World
                        w.print("        \"newWorld\": [");
                        for (int ni = 0; ni < p.discoveredNewWorldIslands().size(); ni++) {
                            w.print("\"" + escapeJson(p.discoveredNewWorldIslands().get(ni)) + "\"");
                            if (ni < p.discoveredNewWorldIslands().size() - 1) w.print(", ");
                        }
                        w.println("]");
                        w.println("      }");
                    w.print("    }");
                    w.println(i < state.players().size() - 1 ? "," : "");
                }
                w.println("  ]");
                w.println("}");
            }
        } catch (IOException e) {
            System.err.println("Warnung: Konnte State nicht speichern (" + label + "): " + e.getMessage());
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String formatRewardForExport(Reward reward) {
        if (reward == null) {
            return "-";
        }

        return switch (reward) {
            case Reward.NewResidents(int amount, int populationLevel) ->
                String.format("%dx neuer Bewohner Stufe %d", amount, populationLevel);
            case Reward.UpgradeResidents(int amount, int level1, int level2) ->
                String.format("%dx Upgrade Stufe %d -> %d", amount, level1, level2);
            case Reward.ExtraAction ignored -> "Extra Action";
            case Reward.ExpeditionCards ignored -> "2 Expedition Cards";
            case Reward.FreeGoodsChoice freeGoodsChoice -> {
                Goods[] options = freeGoodsChoice.options();
                String optionsText = java.util.Arrays.stream(options)
                    .map(Goods::getDisplayName)
                    .toList()
                    .toString();
                Goods chosenGood = freeGoodsChoice.chosenGood();
                if (chosenGood == null) {
                    yield "Free Goods Choice " + optionsText;
                }
                yield "Free Goods Choice " + optionsText + " (gewaehlt: " + chosenGood.getDisplayName() + ")";
            }
            case Reward.TradePoints(int points) -> points + " Trade Points";
            case Reward.ExplorationPoints(int points) -> points + " Exploration Points";
            case Reward.Gold(int amount) -> amount + " Gold";
            case Reward.GoldAndTradePoints(int goldAmount, int tradePoints) ->
                goldAmount + " Gold + " + tradePoints + " Trade Points";
            case Reward.DiscardResidentCard(int amount) ->
                "Discard " + amount + " ResidentCard(s)";
            case Reward.BuildFactory(Factory factoryType) ->
                factoryType == null ? "Build Factory" : "Build Factory: " + factoryType.getType().getDisplayName();
        };
    }
}
