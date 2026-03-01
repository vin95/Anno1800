package com.anno1800.agents;

import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

import java.util.List;
import java.util.Random;

/**
 * Abstract base agent that scores every possible action and selects the highest-scoring one.
 *
 * Subclasses implement {@link #scoreAction} to encode different strategies.
 * A small random noise is added to scores to break ties without systematic bias.
 */
public abstract class ScoringAgent implements Agent {

    private final String name;
    protected final Random random;

    protected ScoringAgent(String name, long seed) {
        this.name = name;
        this.random = new Random(seed);
    }

    protected ScoringAgent(String name) {
        this.name = name;
        this.random = new Random();
    }

    /**
     * Assigns a desirability score to a single action.
     * Higher score = more desirable. Negative scores are allowed.
     *
     * @param action      The action to evaluate
     * @param gameState   Immutable snapshot of the current game state
     * @param player      The player this agent controls
     * @return Numeric score for the action
     */
    protected abstract double scoreAction(Action action, GameState gameState, Player player);

    @Override
    public Action selectAction(GameState gameState, List<Action> possibleActions, Player player) {
        if (possibleActions == null || possibleActions.isEmpty()) {
            return null;
        }

        Action bestAction = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Action action : possibleActions) {
            double score = scoreAction(action, gameState, player);
            // Tiny random noise to break ties without systematic bias
            score += random.nextDouble() * 0.001;
            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        return bestAction;
    }

    @Override
    public String getName() {
        return name;
    }
}
