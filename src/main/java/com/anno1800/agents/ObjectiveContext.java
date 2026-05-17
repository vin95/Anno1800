package com.anno1800.agents;

import com.anno1800.game.cards.ObjectiveCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable context for objective card evaluation, computed once at game start.
 * Contains pre-calculated fixed parameters that affect game-end estimation and scoring.
 * 
 * <p>This context is cached and reused throughout the game since ObjectiveCards
 * do not change after initialization. It separates fixed parameters from flexible
 * ones (which are recalculated each turn in {@link GameContext}).
 * 
 * <h3>Fixed Parameters (calculated once at game start):</h3>
 * <ul>
 *   <li>ObjectiveCard bonuses that affect end-game scoring</li>
 *   <li>Action modifications enabled by ObjectiveCards</li>
 *   <li>Penalties for cards remaining in hand (e.g., ResidentCardsPenalty)</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * <pre>
 * // At game start:
 * ObjectiveContext objCtx = ObjectiveContext.compute(game.getActiveObjectiveCards(), game.getPlayers());
 * 
 * // Each turn (flexible parameters):
 * GameContext gameCtx = GameContext.compute(gameState, player);
 * 
 * // In scoring:
 * double score = evaluateAction(action, gameCtx, objCtx);
 * </pre>
 */
public record ObjectiveContext(
        
        /** List of active ObjectiveCards for this game. */
        List<ObjectiveCard> activeObjectiveCards,
        
        /** 
         * Penalty per ResidentCard remaining in hand at game end.
         * Typically -2 if ResidentCardsPenalty is active, 0 otherwise.
         */
        int residentCardPenalty,
        
        /** 
         * Whether ending the game early (with cards in hand) incurs a penalty.
         * If true, agents should strongly prefer playing all cards before ending.
         */
        boolean hasResidentCardPenalty,
        
        /**
         * Whether ExtraAction card is active (allows paying 3 Gold + 3 Explorer Chips
         * for an additional action per turn).
         */
        boolean hasExtraActionCard,
        
        /**
         * Whether ExplorerTrader card is active (allows using 2 Explorer Chips
         * instead of 1 Trade Chip for trading actions).
         */
        boolean hasExplorerTraderCard,
        
        /**
         * Whether DiscardResidentCard is active (allows paying 2 Explorer Chips
         * to discard a Resident Card once per turn).
         */
        boolean hasDiscardResidentCard,
        
        /**
         * Whether InvestorExhaustForGold is active (allows exhausting an Investor
         * to gain 10 Gold once per turn).
         */
        boolean hasInvestorExhaustForGold,
        
        /**
         * Map of objective types to their scoring potential.
         * Used for estimating which strategies are more valuable in this game.
         * Includes both positive (bonuses) and negative (penalties) values.
         * Action-only cards with 0 potential are excluded.
         * 
         * Example: "MostInvestors" → 10, "ResidentCardsPenalty" → -10
         */
        Map<String, Integer> objectiveScoringPotential,
        
        /**
         * Expected game length in rounds at game start.
         * Based on minimum starting hand cards across all players.
         * Used to scale VP-importance over the course of the game:
         * - Early game: VP less valuable (invest instead)
         * - Late game: VP very valuable (score aggressively)
         */
        int expectedGameLength

) {

    // =========================================================================
    // Factory
    // =========================================================================

    /**
     * Computes the ObjectiveContext from the list of active ObjectiveCards and game state.
     * This should be called once at game initialization.
     * 
     * @param activeObjectiveCards The 5 ObjectiveCards drawn at game start
     * @param players Array of all players (used to calculate expected game length)
     * @return An immutable ObjectiveContext to be reused throughout the game
     */
    public static ObjectiveContext compute(List<ObjectiveCard> activeObjectiveCards, com.anno1800.game.player.Player[] players) {
        
        // Check for ResidentCardsPenalty
        boolean hasResidentCardPenalty = activeObjectiveCards.stream()
                .anyMatch(card -> card instanceof ObjectiveCard.ResidentCardsPenalty);
        int residentCardPenalty = hasResidentCardPenalty ? -2 : 0;
        
        // Check for action modification cards
        boolean hasExtraActionCard = activeObjectiveCards.stream()
                .anyMatch(card -> card instanceof ObjectiveCard.ExtraAction);
        
        boolean hasExplorerTraderCard = activeObjectiveCards.stream()
                .anyMatch(card -> card instanceof ObjectiveCard.ExplorerTrader);
        
        boolean hasDiscardResidentCard = activeObjectiveCards.stream()
                .anyMatch(card -> card instanceof ObjectiveCard.DiscardResidentCard);
        
        boolean hasInvestorExhaustForGold = activeObjectiveCards.stream()
                .anyMatch(card -> card instanceof ObjectiveCard.InvestorExhaustForGold);
        
        // Build scoring potential map (include both positive and negative values)
        Map<String, Integer> scoringPotential = new HashMap<>();
        for (ObjectiveCard card : activeObjectiveCards) {
            String cardType = card.getClass().getSimpleName();
            int potential = estimateScoringPotential(card);
            if (potential != 0) {  // Include both bonuses and penalties, exclude action-only cards
                scoringPotential.put(cardType, potential);
            }
        }
        
        // Calculate expected game length: minimum starting hand cards across all players
        // This assumes ~1 card played per round on average
        int expectedGameLength = Integer.MAX_VALUE;
        for (com.anno1800.game.player.Player player : players) {
              int startCards = player.getPlayerBoard().getResidentCards().size();
            expectedGameLength = Math.min(expectedGameLength, startCards);
        }
        // Fallback if no players (shouldn't happen)
        if (expectedGameLength == Integer.MAX_VALUE) {
            expectedGameLength = 12; // Default assumption
        }
        
        return new ObjectiveContext(
                List.copyOf(activeObjectiveCards), // Make defensive copy
                residentCardPenalty,
                hasResidentCardPenalty,
                hasExtraActionCard,
                hasExplorerTraderCard,
                hasDiscardResidentCard,
                hasInvestorExhaustForGold,
                scoringPotential,
                expectedGameLength
        );
    }

    // =========================================================================
    // Scoring Potential Estimation
    // =========================================================================

    /**
     * Estimates the maximum scoring potential of an ObjectiveCard.
     * Used to help agents prioritize strategies aligned with active objectives.
     * Returns positive values for bonuses, negative for penalties, 0 for action-only cards.
     * 
     * @param card The ObjectiveCard to evaluate
     * @return Rough estimate of max points (positive/negative) or 0 if no direct scoring
     */
    private static int estimateScoringPotential(ObjectiveCard card) {
        return switch (card) {
            // "Most X" cards: first place value
            case ObjectiveCard.MostInvestors ignored -> 10;
            case ObjectiveCard.MostEngineers ignored -> 10;
            case ObjectiveCard.MostExpeditionCards ignored -> 10;
            case ObjectiveCard.MostTradeChips ignored -> 10;
            case ObjectiveCard.MostResidentsTotal ignored -> 10;
            
            // Factory-based scoring (realistic: 3-5 factories per type)
            case ObjectiveCard.LuxuryFactories ignored -> 6 * 4; // ~24 points
            case ObjectiveCard.NewWorldProductFactories ignored -> 6 * 4;
            case ObjectiveCard.PrestigeFactories ignored -> 6 * 2; // Only 2 types
            case ObjectiveCard.ArtisanGoodsFactories ignored -> 6 * 4;
            case ObjectiveCard.EngineerGoodsFactories ignored -> 6 * 2;
            case ObjectiveCard.BasicGoodsProducer ignored -> 3 * 5; // More common
            
            // Fixed bonuses
            case ObjectiveCard.SingleIslandBonus ignored -> 18;
            case ObjectiveCard.NewWorldExplorer ignored -> 6 * 3; // ~3 islands
            
            // Expedition bonuses (realistic: 4-6 cards)
            case ObjectiveCard.ArtifactBonus ignored -> 1 * 5;
            case ObjectiveCard.AnimalBonus ignored -> 1 * 5;
            
            // Penalty cards (negative potential)
            case ObjectiveCard.ResidentCardsPenalty ignored -> -10; // Avg ~5 cards
            
            // Action modification cards (no direct scoring)
            case ObjectiveCard.ExtraAction ignored -> 0;
            case ObjectiveCard.ExplorerTrader ignored -> 0;
            case ObjectiveCard.DiscardResidentCard ignored -> 0;
            case ObjectiveCard.InvestorExhaustForGold ignored -> 0;
            
            default -> 0;
        };
    }

    // =========================================================================
    // Convenience Methods
    // =========================================================================

    /**
     * Returns true if ending the game with cards in hand is heavily penalized.
     * Agents should prioritize playing all cards before ending if this is true.
     */
    public boolean shouldAvoidEndingWithCards() {
        return hasResidentCardPenalty;
    }

    /**
     * Returns the total penalty value for ending with N cards in hand.
     * 
     * @param cardCount Number of ResidentCards still in hand
     * @return Total penalty (typically 0 or negative)
     */
    public int calculateHandPenalty(int cardCount) {
        return residentCardPenalty * cardCount;
    }

    /**
     * Returns true if this objective card type is active in this game.
     * 
     * @param cardClass The ObjectiveCard class to check
     * @return true if at least one instance of this card type is active
     */
    public boolean hasObjective(Class<? extends ObjectiveCard> cardClass) {
        return activeObjectiveCards.stream()
                .anyMatch(card -> cardClass.isInstance(card));
    }

    /**
     * Returns the estimated scoring potential for a given objective type.
     * Can be positive (bonus), negative (penalty), or 0 (not active or action-only card).
     * Use Math.abs() to determine strategic importance regardless of sign.
     * 
     * @param objectiveType Simple class name of the ObjectiveCard (e.g., "MostInvestors")
     * @return Estimated max points (positive/negative/0 if not active or action-only)
     */
    public int getScoringPotential(String objectiveType) {
        return objectiveScoringPotential.getOrDefault(objectiveType, 0);
    }
}
