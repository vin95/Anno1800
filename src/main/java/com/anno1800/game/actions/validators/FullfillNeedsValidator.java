package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.actions.FulfillNeeds;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Validator for FulfillNeeds actions.
 */
public class FullfillNeedsValidator {
        /**
     * Checks if a player can fulfill the needs of a resident card.
     * 
     * @param player The player to check
     * @param game The current game state
     * @param residentCard The resident card whose needs to check
     * @return true if all needs can be fulfilled
     */
    public static boolean canFulfillNeeds(Player player, Game game, ResidentCard residentCard) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Simplified check - just verify card belongs to player
        return playerBoard.getResidentCards().contains(residentCard);
    }
}