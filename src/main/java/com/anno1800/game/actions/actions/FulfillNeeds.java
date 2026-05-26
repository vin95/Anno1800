package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * FulfillNeeds action.
 * Removes goods from player's stored goods to fulfill resident card needs.
 * Adds the card's reward to the player's pending rewards list.
 */
public class FulfillNeeds {

    /**
     * Fulfills the needs of a resident card by consuming goods from stored goods.
     * Adds the card's reward to the player's pending rewards list.
     * 
     * @param player The player fulfilling the needs
     * @param game The current game state
     * @param action The fulfill needs action
     */
    public static void fulfillNeeds(Player player, Game game, com.anno1800.game.actions.Action.FulfillNeeds action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        ResidentCard residentCard = action.residentCard();
        
        System.out.println("Fulfilling needs for resident card - Level " + residentCard.populationLevel());
        
        // Get required goods
        Goods[] needs = residentCard.needs();
        
        if (needs != null && needs.length > 0) {
            System.out.println("  Needs: " + java.util.Arrays.toString(needs));
            
            // PLANNING PHASE: Determine how to obtain goods (with game context for ExplorerTrader)
            if (!playerBoard.canObtainGoods(needs, game)) {
                throw new IllegalStateException("Cannot obtain required goods for ResidentCard");
            }
            
            // EXECUTION PHASE: Actually obtain and consume goods
            playerBoard.consumeGoods(needs, game);
        }
        
        // Add the reward to the player's pending rewards list
        playerBoard.addPendingReward(residentCard.reward());
        System.out.println("  -> Reward added to pending rewards: " + residentCard.reward());
        
        // Award victory points based on population level
        int victoryPoints = getVictoryPointsForLevel(residentCard.populationLevel());
        player.addVictoryPoints(victoryPoints);
        System.out.println("  -> Victory Points awarded: " + victoryPoints + " (Level " + residentCard.populationLevel() + ")");
        
        // Count this fulfill needs action
        playerBoard.incrementFulfillNeedsCount();
        
        // Remove the card from player's hand
        playerBoard.getResidentCards().remove(residentCard);
        
        // Check if this was the player's last card -> trigger end phase
        if (playerBoard.getResidentCards().isEmpty()) {
            game.getBoard().setEndPhase(player.getId(), game.getCurrentRound());
            player.addBonusPoints(7); // 7 bonus points for triggering end phase
        }
    }

    
    /**
     * Returns the victory points awarded for fulfilling a resident card of the given level.
     * 
     * @param populationLevel The population level of the resident card
     * @return Victory points for this level
     */
    private static int getVictoryPointsForLevel(int populationLevel) {
        return switch (populationLevel) {
            case 2 -> 3;
            case 5 -> 8;
            case 7 -> 5;
            default -> 0; // Should not happen with valid resident cards
        };
    }

    /**
     * Gets the goods needed to fulfill a resident card's needs.
     * 
     * @param residentCard The resident card
     * @return Array of required goods (simplified implementation)
     */
    public static Goods[] getRequiredGoods(ResidentCard residentCard) {
        // Simplified - return empty array
        return new Goods[0];
    }

    /**
     * Checks if specific goods are available in the player's stored goods.
     * 
     * @param player The player to check
     * @param requiredGoods The goods to check for
     * @return true if all goods are available
     */
    public static boolean hasRequiredGoods(Player player, Goods[] requiredGoods) {
        // Simplified - always return true
        return true;
    }
}
