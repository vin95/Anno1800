package com.anno1800.simulation;

import com.anno1800.agents.Agent;
import com.anno1800.agents.AgentImpl.WeightedScoringAgent;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.rewards.Reward;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Generates a single-table CSV dataset for strategy analysis.
 *
 * <p>Output: 1002 rows (334 games x 3 players), one row per player perspective.
 * Features include active objective cards, start player position, and aggregated
 * start resident card rewards. Target columns include final points, rank, and winner flag.
 */
public final class DatasetGenerator {

    private static final String CSV_DELIMITER = ";";

    private static final int NUM_PLAYERS = 3;
    private static final int NUM_GAMES = 334;
    private static final int MAX_ROUNDS = 300;
    private static final int MAX_ATTEMPTS = 20000;
    private static final long DATASET_SEED = 20260516L;

    private static final List<String> OBJECTIVE_NAMES = List.of(
            "MostInvestors",
            "MostEngineers",
            "NewWorldExplorer",
            "BasicGoodsProducer",
            "ExplorerTrader",
            "LuxuryFactories",
            "SingleIslandBonus",
            "MostExpeditionCards",
            "ExtraAction",
            "ArtifactBonus",
            "NewWorldProductFactories",
            "AnimalBonus",
            "ArtisanGoodsFactories",
            "MostTradeChips",
            "PrestigeFactories",
            "ResidentCardsPenalty",
            "EngineerGoodsFactories",
            "DiscardResidentCard",
            "InvestorExhaustForGold",
            "MostResidentsTotal"
    );

    private static final List<StrategyFactory> STRATEGIES = List.of(
            new StrategyFactory("shipBuilding", WeightedScoringAgent::shipBuilding),
            new StrategyFactory("tradeShipFocus", WeightedScoringAgent::tradeShipFocus),
            new StrategyFactory("explorationFocus", WeightedScoringAgent::explorationFocus),
            new StrategyFactory("residentMassProduction", WeightedScoringAgent::residentMassProduction),
            new StrategyFactory("eliteResidentProduction", WeightedScoringAgent::eliteResidentProduction),
            new StrategyFactory("adaptiveResidentStrategy", WeightedScoringAgent::adaptiveResidentStrategy),
            new StrategyFactory("factoryVariety", WeightedScoringAgent::factoryVariety),
            new StrategyFactory("balanced", WeightedScoringAgent::balanced)
    );

    private DatasetGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path outputPath = args.length > 0
                ? Paths.get(args[0])
                : Paths.get("datasets", "strategy_dataset_1002.csv");
        generateDataset(outputPath);
    }

    public static void generateDataset(Path outputPath) throws IOException {
        Random random = new Random(DATASET_SEED);
        Files.createDirectories(outputPath.getParent());

        try (var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(csvHeader());
            writer.newLine();

            int rowId = 1;
            int successfulGames = 0;
            int attempts = 0;

            System.out.printf("[Dataset] Games completed: %d/%d", successfulGames, NUM_GAMES);

            while (successfulGames < NUM_GAMES && attempts < MAX_ATTEMPTS) {
                attempts++;
                int gameId = successfulGames + 1;

                try {
                    SimulationBundle bundle = runSilenced(() -> {
                        Game game = new Game(NUM_PLAYERS, false, MAX_ROUNDS);

                        List<AgentAssignment> assignments = assignStrategies(random, gameId);
                        for (int i = 0; i < NUM_PLAYERS; i++) {
                            game.setAgent(i, assignments.get(i).agent());
                        }

                        Map<Integer, StartFeatureRow> startRows = captureStartFeatures(game, assignments);

                        game.start();
                        applyFinalScoring(game);

                        return new SimulationBundle(game, startRows);
                    });

                    Game game = bundle.game();
                    Map<Integer, StartFeatureRow> startRows = bundle.startRows();

                    Map<Integer, Integer> totals = new HashMap<>();
                    for (int i = 0; i < NUM_PLAYERS; i++) {
                        totals.put(i, game.getPlayers()[i].getTotalPoints());
                    }
                    int roundsPlayed = Math.max(1, Math.min(MAX_ROUNDS, game.getCurrentRound() - 1));
                    String gameEndStatus = determineGameEndStatus(game);

                    Map<Integer, Integer> fulfillNeeds = new HashMap<>();
                    for (int i = 0; i < NUM_PLAYERS; i++) {
                        fulfillNeeds.put(i, game.getPlayers()[i].getPlayerBoard().getFulfillNeedsCount());
                    }

                    Map<Integer, Integer> ranks = computeDenseRanks(totals);
                    int maxPoints = totals.values().stream().max(Integer::compareTo).orElse(0);

                    for (int playerId = 0; playerId < NUM_PLAYERS; playerId++) {
                        StartFeatureRow start = startRows.get(playerId);
                        int finalPoints = totals.get(playerId);
                        int rank = ranks.get(playerId);
                        int isWinner = finalPoints == maxPoints ? 1 : 0;

                        writer.write(toCsvRow(
                                rowId,
                                gameId,
                                playerId,
                                start.seatPosition(),
                                start.isStartPlayer(),
                                start.objectiveFlags(),
                                start.startCardsL2Count(),
                                start.startCardsL5Count(),
                                start.startCardsL7Count(),
                                start.rewardCounts(),
                                start.strategyName(),
                                roundsPlayed,
                                gameEndStatus,
                                fulfillNeeds.get(playerId),
                                finalPoints,
                                rank,
                                isWinner
                        ));
                        writer.newLine();
                        rowId++;
                    }

                    successfulGames++;
                    System.out.printf("\r[Dataset] Games completed: %d/%d", successfulGames, NUM_GAMES);
                } catch (RuntimeException ex) {
                    // Skip unstable simulation runs and continue until enough valid rows exist.
                } catch (Exception ex) {
                    // Skip attempts with checked exceptions from simulation execution.
                }
            }

            System.out.println();

            if (successfulGames < NUM_GAMES) {
                throw new IllegalStateException(
                        "Could not generate enough valid games. Successful=" + successfulGames
                                + ", attempts=" + attempts
                );
            }
        }

        System.out.println("Dataset generated: " + outputPath.toAbsolutePath());
        System.out.println("Rows (without header): " + (NUM_GAMES * NUM_PLAYERS));
    }

    private static String csvHeader() {
        List<String> header = new ArrayList<>();
        header.add("row_id");
        header.add("game_id");
        header.add("player_id");
        header.add("seat_position");
        header.add("is_start_player");

        for (String objectiveName : OBJECTIVE_NAMES) {
            header.add("objective_" + objectiveName);
        }

        header.add("start_cards_l2_count");
        header.add("start_cards_l5_count");
        header.add("start_cards_l7_count");

        header.add("start_reward_new_residents_count");
        header.add("start_reward_upgrade_count");
        header.add("start_reward_tradepoints_count");
        header.add("start_reward_gold_count");
        header.add("start_reward_extraaction_count");
        header.add("start_reward_expedition_count");
        header.add("start_reward_discard_count");
        header.add("start_reward_explorationpoints_count");
        header.add("start_reward_freegoodschoice_count");
        header.add("start_reward_gold_and_tradepoints_count");

        header.add("strategy_name");
        header.add("rounds_played");
        header.add("game_end_status");
        header.add("fulfill_needs_count");
        header.add("final_points");
        header.add("rank");
        header.add("is_winner");

        return String.join(CSV_DELIMITER, header);
    }

    private static List<AgentAssignment> assignStrategies(Random random, int gameId) {
        List<AgentAssignment> result = new ArrayList<>(NUM_PLAYERS);
        for (int playerId = 0; playerId < NUM_PLAYERS; playerId++) {
            StrategyFactory chosen = STRATEGIES.get(random.nextInt(STRATEGIES.size()));
            String agentName = String.format(Locale.ROOT, "%s_g%03d_p%d", chosen.strategyName(), gameId, playerId + 1);
            Agent agent = chosen.factory().create(agentName);
            result.add(new AgentAssignment(chosen.strategyName(), agent));
        }
        return result;
    }

    private static Map<Integer, StartFeatureRow> captureStartFeatures(Game game, List<AgentAssignment> assignments) {
        Map<String, Integer> objectiveFlags = buildObjectiveFlags(game.getActiveObjectiveCards());
        Map<Integer, StartFeatureRow> startRows = new HashMap<>();

        Player[] players = game.getPlayers();
        for (int playerId = 0; playerId < players.length; playerId++) {
            Player player = players[playerId];
            List<ResidentCard> cards = player.getPlayerBoard().getResidentCards();

            int l2Count = 0;
            int l5Count = 0;
            int l7Count = 0;

            Map<String, Integer> rewardCounts = new LinkedHashMap<>();
            rewardCounts.put("new_residents", 0);
            rewardCounts.put("upgrade", 0);
            rewardCounts.put("tradepoints", 0);
            rewardCounts.put("gold", 0);
            rewardCounts.put("extraaction", 0);
            rewardCounts.put("expedition", 0);
            rewardCounts.put("discard", 0);
            rewardCounts.put("explorationpoints", 0);
            rewardCounts.put("freegoodschoice", 0);
            rewardCounts.put("gold_and_tradepoints", 0);

            for (ResidentCard card : cards) {
                switch (card.populationLevel()) {
                    case 2 -> l2Count++;
                    case 5 -> l5Count++;
                    case 7 -> l7Count++;
                    default -> {
                        // No other levels are expected in ResidentCardData, keep row robust.
                    }
                }

                Reward reward = card.reward();
                if (reward instanceof Reward.NewResidents) {
                    increment(rewardCounts, "new_residents");
                } else if (reward instanceof Reward.UpgradeResidents) {
                    increment(rewardCounts, "upgrade");
                } else if (reward instanceof Reward.TradePoints) {
                    increment(rewardCounts, "tradepoints");
                } else if (reward instanceof Reward.Gold) {
                    increment(rewardCounts, "gold");
                } else if (reward instanceof Reward.ExtraAction) {
                    increment(rewardCounts, "extraaction");
                } else if (reward instanceof Reward.ExpeditionCards) {
                    increment(rewardCounts, "expedition");
                } else if (reward instanceof Reward.DiscardResidentCard) {
                    increment(rewardCounts, "discard");
                } else if (reward instanceof Reward.ExplorationPoints) {
                    increment(rewardCounts, "explorationpoints");
                } else if (reward instanceof Reward.FreeGoodsChoice) {
                    increment(rewardCounts, "freegoodschoice");
                } else if (reward instanceof Reward.GoldAndTradePoints) {
                    increment(rewardCounts, "gold_and_tradepoints");
                }
            }

            int seatPosition = player.getPosition();
            int isStartPlayer = seatPosition == 1 ? 1 : 0;

            startRows.put(playerId, new StartFeatureRow(
                    seatPosition,
                    isStartPlayer,
                    objectiveFlags,
                    l2Count,
                    l5Count,
                    l7Count,
                    rewardCounts,
                    assignments.get(playerId).strategyName()
            ));
        }

        return startRows;
    }

    private static Map<String, Integer> buildObjectiveFlags(List<ObjectiveCard> activeObjectives) {
        Map<String, Integer> flags = new LinkedHashMap<>();
        for (String objectiveName : OBJECTIVE_NAMES) {
            flags.put(objectiveName, 0);
        }

        for (ObjectiveCard card : activeObjectives) {
            String name = card.getClass().getSimpleName();
            if (flags.containsKey(name)) {
                flags.put(name, 1);
            }
        }

        return flags;
    }

    private static <T> T runSilenced(Callable<T> callable) throws Exception {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        PrintStream nullStream = new PrintStream(OutputStream.nullOutputStream());
        try {
            System.setOut(nullStream);
            System.setErr(nullStream);
            return callable.call();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            nullStream.close();
        }
    }

    private static void applyFinalScoring(Game game) {
        Player[] players = game.getPlayers();

        for (ObjectiveCard objective : game.getActiveObjectiveCards()) {
            Map<Player, Integer> objectivePoints = objective.calculateEndGamePoints(players);
            for (Map.Entry<Player, Integer> entry : objectivePoints.entrySet()) {
                entry.getKey().addVictoryPoints(entry.getValue());
            }
        }

        for (Player player : players) {
            int goldVp = player.getPlayerBoard().getGold() / 3;
            player.addVictoryPoints(goldVp);
        }
    }

    private static Map<Integer, Integer> computeDenseRanks(Map<Integer, Integer> totals) {
        List<Map.Entry<Integer, Integer>> sorted = totals.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue(Comparator.reverseOrder()))
                .toList();

        Map<Integer, Integer> ranks = new HashMap<>();
        int currentRank = 1;
        Integer previousScore = null;

        for (Map.Entry<Integer, Integer> entry : sorted) {
            if (previousScore != null && !previousScore.equals(entry.getValue())) {
                currentRank++;
            }
            ranks.put(entry.getKey(), currentRank);
            previousScore = entry.getValue();
        }

        return ranks;
    }

    private static String toCsvRow(
            int rowId,
            int gameId,
            int playerId,
            int seatPosition,
            int isStartPlayer,
            Map<String, Integer> objectiveFlags,
            int startCardsL2Count,
            int startCardsL5Count,
            int startCardsL7Count,
            Map<String, Integer> rewardCounts,
            String strategyName,
            int roundsPlayed,
            String gameEndStatus,
            int fulfillNeedsCount,
            int finalPoints,
            int rank,
            int isWinner
    ) {
        List<String> values = new ArrayList<>();
        values.add(Integer.toString(rowId));
        values.add(Integer.toString(gameId));
        values.add(Integer.toString(playerId));
        values.add(Integer.toString(seatPosition));
        values.add(Integer.toString(isStartPlayer));

        for (String objectiveName : OBJECTIVE_NAMES) {
            values.add(Integer.toString(objectiveFlags.getOrDefault(objectiveName, 0)));
        }

        values.add(Integer.toString(startCardsL2Count));
        values.add(Integer.toString(startCardsL5Count));
        values.add(Integer.toString(startCardsL7Count));

        values.add(Integer.toString(rewardCounts.getOrDefault("new_residents", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("upgrade", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("tradepoints", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("gold", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("extraaction", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("expedition", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("discard", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("explorationpoints", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("freegoodschoice", 0)));
        values.add(Integer.toString(rewardCounts.getOrDefault("gold_and_tradepoints", 0)));

        values.add(strategyName);
        values.add(Integer.toString(roundsPlayed));
        values.add(gameEndStatus);
        values.add(Integer.toString(fulfillNeedsCount));
        values.add(Integer.toString(finalPoints));
        values.add(Integer.toString(rank));
        values.add(Integer.toString(isWinner));

        return String.join(CSV_DELIMITER, values);
    }

    private static void increment(Map<String, Integer> map, String key) {
        map.put(key, map.get(key) + 1);
    }

    private static String determineGameEndStatus(Game game) {
        int triggerRound = game.getBoard().getEndPhaseTriggeredInRound();
        boolean endedByEndCondition = triggerRound >= 0 && game.getCurrentRound() > triggerRound + 1;

        if (endedByEndCondition) {
            return "finished";
        }

        if (game.getCurrentRound() > MAX_ROUNDS) {
            return "aborted_" + MAX_ROUNDS + "_rounds";
        }

        return "finished";
    }

    private record StrategyFactory(String strategyName, AgentFactory factory) {
    }

    @FunctionalInterface
    private interface AgentFactory {
        Agent create(String name);
    }

    private record AgentAssignment(String strategyName, Agent agent) {
    }

    private record SimulationBundle(Game game, Map<Integer, StartFeatureRow> startRows) {
    }

    private record StartFeatureRow(
            int seatPosition,
            int isStartPlayer,
            Map<String, Integer> objectiveFlags,
            int startCardsL2Count,
            int startCardsL5Count,
            int startCardsL7Count,
            Map<String, Integer> rewardCounts,
            String strategyName
    ) {
    }
}