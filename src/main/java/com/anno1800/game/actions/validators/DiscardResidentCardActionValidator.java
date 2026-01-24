package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Validator for the DiscardResidentCardAction free action.
 * Checks if the player can discard a resident card using the DiscardResidentCard ObjectiveCard.
 * 
 * Requirements:
 * - DiscardResidentCard ObjectiveCard must be active in the game
 * - Player has not used this action yet this turn
 * - Player has at least 2 Explorer Chips
 * - Player has the specified ResidentCard in hand
 */
public class DiscardResidentCardActionValidator {

    /**
     * Checks if the DiscardResidentCardAction can be executed.
     * 
     * @param action The DiscardResidentCardAction action
     * @param player The player attempting the action
     * @param game The current game state
     * @return true if the action can be executed
     */
    public static boolean canDiscardResidentCard(Action.DiscardResidentCardAction action, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if DiscardResidentCard ObjectiveCard is active
        boolean hasCard = game.getActiveObjectiveCards().stream()
            .anyMatch(card -> card instanceof ObjectiveCard.DiscardResidentCard);
        if (!hasCard) {
            return false;
        }
        
        // Check if already used this turn
        if (playerBoard.hasUsedDiscardResidentCardThisTurn()) {
            return false;
        }
        
        // Check explorer chips (2 required)
        if (playerBoard.getAvailableExplorerChips() < 2) {
            return false;
        }
        
        // Check if player has the specified card in hand
        ResidentCard cardToDiscard = action.card();
        if (cardToDiscard == null || !playerBoard.getResidentCards().contains(cardToDiscard)) {
            return false;
        }
        
        return true;
    }
}
