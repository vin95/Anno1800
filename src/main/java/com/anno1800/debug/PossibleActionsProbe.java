package com.anno1800.debug;

import com.anno1800.agents.Agent;
import com.anno1800.agents.AgentImpl.WeightedScoringAgent;
import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionGenerator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

/**
 * Replays a seeded debug game deterministically and prints all possible actions
 * before a specific action number.
 */
public class PossibleActionsProbe {

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

    private static WeightedScoringAgent createAgent(String strategy, String name, long agentSeed) {
        return switch (strategy) {
            case "shipBuilding"             -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.shipBuilding(), agentSeed);
            case "tradeShipFocus"           -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.tradeShipFocus(), agentSeed);
            case "explorationFocus"         -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.explorationFocus(), agentSeed);
            case "residentMassProduction"   -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.residentMassProduction(), agentSeed);
            case "eliteResidentProduction"  -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.eliteResidentProduction(), agentSeed);
            case "adaptiveResidentStrategy" -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.adaptiveResidentStrategy(), agentSeed);
            case "factoryVariety"           -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.factoryVariety(), agentSeed);
            default                          -> new WeightedScoringAgent(name, com.anno1800.agents.StrategyWeights.balanced(), agentSeed);
        };
    }

    private static void setCurrentPlayer(Game game, int playerIndex) throws Exception {
        Field field = Game.class.getDeclaredField("currentPlayer");
        field.setAccessible(true);
        field.setInt(game, playerIndex);
    }

    public static void main(String[] args) throws Exception {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 42L;
        int targetActionNumber = args.length > 1 ? Integer.parseInt(args[1]) : 7;
        int numPlayers = 3;
        int maxRounds = 100;

        Game game = new Game(numPlayers, seed, maxRounds);
        ActionGenerator actionGenerator = new ActionGenerator();

        Random stratRng = new Random(seed);
        Agent[] agents = new Agent[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            String strategy = STRATEGY_NAMES[stratRng.nextInt(STRATEGY_NAMES.length)];
            long agentSeed = seed * 31L + i;
            WeightedScoringAgent agent = createAgent(strategy, "Spieler-" + (i + 1), agentSeed);
            agents[i] = agent;
            game.setAgent(i, agent);
        }

        int actionCounter = 1;

        while (!game.isGameOver()) {
            for (int i = 0; i < numPlayers; i++) {
                int playerIndex = (game.getStartPlayer() + i) % numPlayers;
                setCurrentPlayer(game, playerIndex);
                Player player = game.getCurrentPlayer();
                Agent agent = agents[playerIndex];

                List<Action> possibleActions = actionGenerator.getPossibleActions(player, game);

                if (actionCounter == targetActionNumber) {
                    System.out.printf("Before Action %d | Round %d | Player %s%n", actionCounter, game.getCurrentRound(), player.getName());
                    System.out.println("Possible actions:");
                    for (Action action : possibleActions) {
                        System.out.println("- " + action);
                    }
                    return;
                }

                if (possibleActions.isEmpty()) {
                    actionCounter++;
                    continue;
                }

                Action chosen = agent.selectAction(game.getState(), possibleActions, player);
                if (chosen != null) {
                    game.executeAction(chosen);
                }
                actionCounter++;
            }

            game.nextRound();
        }

        System.out.println("Target action not reached before game end.");
    }
}
