package com.anno1800.agents.AgentImpl;

import com.anno1800.agents.Agent;
import com.anno1800.agents.GameContext;
import com.anno1800.agents.ObjectiveContext;
import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.player.Player;
import com.anno1800.game.state.GameState;

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
            throw new IllegalArgumentException("No possible actions provided");
        }

        GameContext context = GameContext.compute(gameState, player);
        
        // Use cached ObjectiveContext (or create empty one if not initialized)
        ObjectiveContext objContext = objectiveContext != null 
                ? objectiveContext 
                : ObjectiveContext.compute(List.of(), new com.anno1800.game.player.Player[0]);
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
     *   <li><b>Expensive actions without resources</b>: Strong penalty for actions requiring resources we don't have.</li>
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
        double penalty = 0.0;
        
        // Carneval im letzten Zug: komplett sinnlos, weil es nur Ressourcen für
        // den nächsten Zug vorbereitet, aber es gibt keinen nächsten Zug mehr.
        if (action instanceof Action.Carneval && context.remainingTurns() == 1) {
            penalty += -1000.0;
        }
        
        // ViewResidentCards: reine Informationsaktion, kein Spielvorteil.
        if (action instanceof Action.ViewResidentCards) {
            penalty += -500.0;
        }
        
        // Long-term investments in the end phase (when only 1-2 turns remain)
        if (context.remainingTurns() > 0 && context.remainingTurns() <= 2) {
            penalty += penaltyForLongTermInvestment(action, context);
        }

        return penalty;
    }

    /**
     * Penalizes actions that are long-term investments when the game is about to end.
     * These actions require multiple turns to pay off, which we don't have.
     * 
     * @param action The action to evaluate
     * @param context Current game context
     * @return Penalty value (0 or negative)
     */
    private double penaltyForLongTermInvestment(Action action, GameContext context) {
        int turnsLeft = context.remainingTurns();
        
        return switch (action) {
            // BuildShipyard/BuildShips: Need ships + turns to use them effectively
            case Action.BuildShipyard ignored     -> turnsLeft == 1 ? -50.0 : -10.0;
            case Action.BuildShips ignored        -> turnsLeft == 1 ? -50.0 : -10.0;
            
            // Expedition: Need turns to play expedition cards
            case Action.Expedition ignored        -> turnsLeft == 1 ? -40.0 : -5.0;
            
            // DrawResidentCard: Need turns to fulfill the new card
            case Action.DrawResidentCard ignored  -> turnsLeft == 1 ? -30.0 : -5.0;
            
            // SettleResident at low level: Need turns to upgrade and use
            case Action.SettleResident a when a.level() <= 3 
                                              -> turnsLeft == 1 ? -25.0 : -5.0;
            
            // BuildFactory: Need turns to produce goods and score
            case Action.BuildFactory ignored      -> turnsLeft == 1 ? -20.0 : 0.0;
            
            default -> 0.0;
        };
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

    // =========================================================================
    // Helper methods for subclasses - Common scoring patterns
    // =========================================================================

    /**
     * Returns the immediate victory point value of fulfilling a ResidentCard.
     * Does NOT include bonuses from ending the game or other context factors.
     * 
     * @param card The ResidentCard being fulfilled
     * @return Base VP value (3, 5, or 8 depending on population level)
     */
    protected double getResidentCardVP(ResidentCard card) {
        if (card == null) return 0.0;
        
        return switch (card.populationLevel()) {
            case 2, 3 -> 3.0;  // Bauer/Arbeiter: 3 VP
            case 4, 5, 6 -> 8.0;  // Handwerker/Ingenieur/Investor: 8 VP
            case 7 -> 5.0;  // Neue-Welt-Karten: 5 VP
            default -> 2.0;
        };
    }

    /**
     * Calculates a bonus for actions that progress towards ending the game.
     * Considers whether ending now is strategically sound based on:
     * - Cards remaining in hand
     * - ResidentCardsPenalty objective card
     * - Current VP position vs opponents (if available in future)
     * 
     * @param cardsAfterAction How many cards player will have AFTER this action
     * @param objectiveContext The objective context
     * @return Bonus value (positive encourages ending, negative discourages)
     */
    protected double getGameEndingBonus(int cardsAfterAction, ObjectiveContext objectiveContext) {
        // Ending the game (playing last card) gives +7 VP from fireworks
        if (cardsAfterAction == 0) {
            // Check if we should avoid ending with penalty
            if (objectiveContext.hasResidentCardPenalty()) {
                // Good - we played all cards, no penalty
                return 20.0; // 7 VP from fireworks + strategic value
            } else {
                return 15.0; // Just the fireworks bonus
            }
        }
        
        // One card left after action - we're close to ending
        if (cardsAfterAction == 1) {
            if (objectiveContext.hasResidentCardPenalty()) {
                // Still have 1 card - would lose 2 VP if game ends now
                return 5.0; // Moderate bonus, but better to play the last card too
            } else {
                return 8.0; // Good progress towards ending
            }
        }
        
        return 0.0;
    }

    /**
     * Scales a score based on game phase (remaining turns).
     * Long-term investments become less valuable as the game progresses.
     * 
     * @param baseScore The base score to scale
     * @param context Current game context
     * @param investmentType Type of investment (SHORT, MEDIUM, LONG)
     * @return Scaled score
     */
    protected double scaleByGamePhase(double baseScore, GameContext context, InvestmentType investmentType) {
        if (!context.isEndPhase() || context.remainingTurns() < 0) {
            return baseScore; // Not in end phase, no scaling needed
        }
        
        int turnsLeft = context.remainingTurns();
        
        double scale = switch (investmentType) {
            case SHORT -> turnsLeft >= 1 ? 1.0 : 0.3;    // Payoff within 1 turn
            case MEDIUM -> turnsLeft >= 2 ? 1.0 : 0.2;   // Payoff needs 2 turns
            case LONG -> turnsLeft >= 3 ? 1.0 : 0.1;     // Payoff needs 3+ turns
        };
        
        return baseScore * scale;
    }

    /**
     * Investment type classification for scaling scores based on game phase.
     */
    protected enum InvestmentType {
        /** Actions with immediate payoff (FulfillNeeds, ProduceGoods, ActivateReward) */
        SHORT,
        
        /** Actions needing 2 turns to pay off (UpgradeResident, BuildFactory) */
        MEDIUM,
        
        /** Actions needing 3+ turns (BuildShips, Expedition, DrawResidentCard) */
        LONG
    }

    /**
     * Estimates the relative value of spending Gold on this action.
     * Returns 0 if action is free, negative if expensive relative to benefit.
     * 
     * @param goldCost Cost in gold
     * @param expectedValue Expected benefit of the action
     * @param context Current game context
     * @return Value assessment (0 = fair trade, negative = too expensive)
     */
    protected double evaluateGoldCost(int goldCost, double expectedValue, GameContext context) {
        if (goldCost == 0) return 0.0;
        
        // If we're low on gold, buying is more painful
        double scarcityPenalty = context.gold() < 5 ? -2.0 : 0.0;
        
        // Simple cost-benefit: we expect ~1 point per 2 gold spent
        double fairValue = goldCost / 2.0;
        
        if (expectedValue < fairValue) {
            return scarcityPenalty - (fairValue - expectedValue); // Too expensive
        }
        
        return scarcityPenalty;
    }

    /**
     * Evaluates whether we have sufficient resources for an action.
     * Returns 0 if affordable, increasingly negative penalty if resources are missing.
     * 
     * @param requiredGold Gold required
     * @param requiredTradeChips Trade chips required
     * @param requiredExplorerChips Explorer chips required
     * @param context Current game context
     * @return Penalty (0 or negative)
     */
    protected double checkResourceAvailability(int requiredGold, int requiredTradeChips, 
                                              int requiredExplorerChips, GameContext context) {
        double penalty = 0.0;
        
        if (context.gold() < requiredGold) {
            penalty -= (requiredGold - context.gold()) * 5.0; // -5 per missing gold
        }
        
        if (context.tradeChips() < requiredTradeChips) {
            penalty -= (requiredTradeChips - context.tradeChips()) * 10.0; // Trade chips scarce
        }
        
        if (context.explorerChips() < requiredExplorerChips) {
            penalty -= (requiredExplorerChips - context.explorerChips()) * 8.0;
        }
        
        return penalty;
    }

    // =========================================================================
    // Name
    // =========================================================================

    @Override
    public String getName() {
        return name;
    }
}
