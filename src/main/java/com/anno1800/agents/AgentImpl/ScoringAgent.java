package com.anno1800.agents.AgentImpl;

import com.anno1800.agents.Agent;
import com.anno1800.agents.GameContext;
import com.anno1800.agents.ObjectiveContext;
import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

import java.util.List;
import java.util.Random;

/**
 * Abstract base agent that scores every possible action using game context and selects the best one.
 *
 * <p>Features:
 * <ul>
 *   <li><b>GameContext</b> – computed once per turn from the GameState snapshot.
 *       Captures estimated game length and current resource state.</li>
 *   <li><b>ObjectiveContext</b> – computed once at game start from active ObjectiveCards.
 *       Contains fixed parameters that don't change during the game.</li>
 *   <li><b>BasicRules</b> – universal score overrides that apply to ALL agents
 *       (e.g., Carneval at end of game = -1000).</li>
 *   <li><b>End Phase Planning</b> – use {@code context.remainingTurns()} to optimize strategy
 *       for the final turns. Example:
 *       <pre>
 *         if (context.isLastTurn()) {
 *             // Maximize immediate VP, avoid long-term investments
 *             return scoreImmediateVictoryPoints(action);
 *         } else if (context.hasTwoTurnsLeft()) {
 *             // Focus on 2-turn combos (e.g., upgrade + score)
 *             return scoreTwoTurnCombo(action);
 *         }
 *       </pre>
 *   </li>
 * </ul>
 *
 * <p>Inheritance chain:
 * <pre>
 *   Agent (interface)
 *     └── ScoringAgent (abstract)
 *           ├── AgentResidents1 (concrete strategy)
 *           ├── AgentResidents2 (concrete strategy)
 *           ├── AgentShips1 (concrete strategy)
 *           └── ...
 * </pre>
 *
 * <p>Subclasses must implement {@link #scoreAction(Action, GameState, Player, GameContext, ObjectiveContext)}.
 */
public abstract class ScoringAgent implements Agent {

    private final String name;
    protected final Random random;
    
    /** 
     * Cached ObjectiveContext, computed once at game start.
     * Contains fixed parameters from ObjectiveCards that don't change during the game.
     */
    private ObjectiveContext objectiveContext;

    protected ScoringAgent(String name, long seed) {
        this.name = name;
        this.random = new Random(seed);
    }

    protected ScoringAgent(String name) {
        this.name = name;
        this.random = new Random();
    }

    /**
     * Sets the ObjectiveContext for this agent.
     * This should be called once at game initialization, before any actions are scored.
     * 
     * @param objectiveContext The pre-computed objective context
     */
    public void setObjectiveContext(ObjectiveContext objectiveContext) {
        this.objectiveContext = objectiveContext;
    }

    /**
     * Gets the cached ObjectiveContext.
     * 
     * @return The ObjectiveContext, or null if not yet initialized
     */
    protected ObjectiveContext getObjectiveContext() {
        return objectiveContext;
    }

    // =========================================================================
    // Action Selection
    // =========================================================================

    @Override
    public Action selectAction(GameState gameState, List<Action> possibleActions, Player player) {
        if (possibleActions == null || possibleActions.isEmpty()) {
            return null;
        }

        GameContext context = GameContext.compute(gameState, player);
        
        // Use cached ObjectiveContext (or create empty one if not initialized)
        ObjectiveContext objContext = objectiveContext != null 
                ? objectiveContext 
                : ObjectiveContext.compute(List.of());

        Action bestAction = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Action action : possibleActions) {
            double score = applyBasicRules(action, context, objContext)
                         + scoreAction(action, gameState, player, context, objContext)
                         + random.nextDouble() * 0.001; // tie-breaker noise

            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        return bestAction;
    }

    // =========================================================================
    // Basic rules – apply to ALL agents
    // =========================================================================

    /**
     * Universal score modifiers that override strategy-specific scoring.
     * A very negative value (e.g. -1000) strongly discourages an action but still
     * allows it as a last resort if all other actions score equally badly.
     *
     * <p>Current rules:
     * <ul>
     *   <li><b>Carneval in last turn</b>: -1000 – Carneval bereitet Ressourcen für den
     *       nächsten Zug vor (setzt Bewohner zurück, leert Fabriken, gibt Chips zurück),
     *       aber es gibt keinen nächsten Zug mehr. Die Aktion verschwendet den letzten Zug.
     *       (Bei 2 verbleibenden Zügen kann Carneval sinnvoll sein: Zug 1 = Carneval, Zug 2 = produzieren)</li>
     *   <li><b>ViewResidentCards</b>: -500 – reine Info-Aktion, kein Spielvorteil.</li>
     * </ul>
     *
     * <p>Subclasses may override this method to add further rules. Always call
     * {@code super.applyBasicRules(action, context, objectiveContext)} to preserve the base rules.
     *
     * @param action  The action to evaluate
     * @param context The current game context (flexible parameters)
     * @param objectiveContext The objective context (fixed parameters)
     * @return Score modifier (0.0 if no rule applies)
     */
    protected double applyBasicRules(Action action, GameContext context, ObjectiveContext objectiveContext) {
        // Carneval im letzten Zug: komplett sinnlos, weil es nur Ressourcen für
        // den nächsten Zug vorbereitet, aber es gibt keinen nächsten Zug mehr.
        if (action instanceof Action.Carneval && context.isLastTurn()) {
            return -1000.0;
        }
        
        // ViewResidentCards: reine Informationsaktion, kein Spielvorteil.
        if (action instanceof Action.ViewResidentCards) {
            return -500.0;
        }

        return 0.0;
    }

    // =========================================================================
    // Abstract – subclasses provide strategy-specific scoring
    // =========================================================================

    /**
     * Strategy-specific scoring with full game context.
     *
     * <p>This method is called for each possible action. The agent selects the action
     * with the highest score. Negative scores are allowed.
     *
     * @param action    The action to score
     * @param gameState Immutable game state snapshot
     * @param player    The player this agent controls
     * @param context   Pre-computed game context (flexible parameters, never null)
     * @param objectiveContext Pre-computed objective context (fixed parameters, never null)
     * @return Numeric score (higher = more desirable)
     */
    protected abstract double scoreAction(Action action, GameState gameState, Player player, 
                                         GameContext context, ObjectiveContext objectiveContext);

    @Override
    public String getName() {
        return name;
    }
}
