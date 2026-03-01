package com.anno1800.agents;

import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

import java.util.List;

/**
 * Abstract agent layer that adds two capabilities on top of {@link ScoringAgent}:
 *
 * <ol>
 *   <li><b>GameContext</b> – computed once per turn from the GameState snapshot.
 *       Captures estimated game length and current resource state.
 *       Available to all subclasses via {@code scoreStrategic(…, context)}.</li>
 *   <li><b>BasicRules</b> – universal score overrides that apply to ALL strategic
 *       agents regardless of their strategy.
 *       Subclasses can extend these rules via {@link #applyBasicRules(Action, GameContext)}.</li>
 * </ol>
 *
 * Inheritance chain:
 * <pre>
 *   Agent
 *     └── ScoringAgent          (scores + selects best)
 *           └── StrategicScoringAgent   (adds GameContext + BasicRules)
 *                 └── WeightedScoringAgent (adds StrategyWeights)
 * </pre>
 *
 * Subclasses must implement {@link #scoreStrategic(Action, GameState, Player, GameContext)}.
 */
public abstract class StrategicScoringAgent extends ScoringAgent {

    protected StrategicScoringAgent(String name, long seed) {
        super(name, seed);
    }

    protected StrategicScoringAgent(String name) {
        super(name);
    }

    // =========================================================================
    // selectAction – overrides ScoringAgent to inject GameContext
    // =========================================================================

    @Override
    public Action selectAction(GameState gameState, List<Action> possibleActions, Player player) {
        if (possibleActions == null || possibleActions.isEmpty()) {
            return null;
        }

        GameContext context = GameContext.compute(gameState, player);

        Action bestAction = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Action action : possibleActions) {
            double score = applyBasicRules(action, context)
                         + scoreStrategic(action, gameState, player, context)
                         + random.nextDouble() * 0.001; // tie-breaker noise

            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        return bestAction;
    }

    // =========================================================================
    // Basic rules – apply to ALL strategic agents
    // =========================================================================

    /**
     * Universal score modifiers that override strategy-specific scoring.
     * A very negative value (e.g. -1000) strongly discourages an action but still
     * allows it as a last resort if all other actions score equally badly.
     *
     * <p>Current rules:
     * <ul>
     *   <li><b>Carneval in last round</b>: -1000 – neue Bewohner können nicht mehr
     *       genutzt werden, die Aktion verschwendet einen Zug.</li>
     *   <li><b>ViewResidentCards</b>: -500 – reine Info-Aktion, kein Spielvorteil.</li>
     * </ul>
     *
     * <p>Subclasses may override this method to add further rules. Always call
     * {@code super.applyBasicRules(action, context)} to preserve the base rules.
     *
     * @param action  The action to evaluate
     * @param context The current game context
     * @return Score modifier (0.0 if no rule applies)
     */
    protected double applyBasicRules(Action action, GameContext context) {
        return switch (action) {
            // Carneval at end of game: sinnlos, neue Einwohnerkarten können nicht
            // mehr ausgespielt werden bevor das Spiel endet.
            case Action.Carneval ignored when context.isEndPhaseLikely() -> -1000.0;

            // ViewResidentCards: reine Informationsaktion, kein Spielvorteil.
            case Action.ViewResidentCards ignored -> -500.0;

            default -> 0.0;
        };
    }

    // =========================================================================
    // Abstract – subclasses provide strategy-specific scoring
    // =========================================================================

    /**
     * Strategy-specific scoring with full game context.
     *
     * @param action    The action to score
     * @param gameState Immutable game state snapshot
     * @param player    The player this agent controls
     * @param context   Pre-computed game context (never null)
     * @return Numeric score (higher = more desirable)
     */
    protected abstract double scoreStrategic(Action action, GameState gameState, Player player, GameContext context);

    /**
     * Bridge to {@link ScoringAgent}'s abstract method.
     * Not called in normal flow since {@link #selectAction} is overridden.
     */
    @Override
    protected final double scoreAction(Action action, GameState gameState, Player player) {
        throw new UnsupportedOperationException(
            "StrategicScoringAgent uses scoreStrategic(…, GameContext). " +
            "This method should not be called directly.");
    }
}
