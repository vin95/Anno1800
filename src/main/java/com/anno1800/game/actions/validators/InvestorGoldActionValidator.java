package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;

/**
 * Validator for the InvestorGoldAction free action.
 * Checks if the player can exhaust an Investor to gain 5 Gold using the InvestorExhaustForGold ObjectiveCard.
 * 
 * Requirements:
 * - InvestorExhaustForGold ObjectiveCard must be active in the game
 * - Player has not used this action yet this turn
 * - The specified resident is an Investor (population level 5)
 * - The resident is not exhausted (Fit state)
 * - The resident belongs to the player
 */
public class InvestorGoldActionValidator {

    /**
     * Checks if the InvestorGoldAction can be executed.
     * 
     * @param action The InvestorGoldAction action
     * @param player The player attempting the action
     * @param game The current game state
     * @return true if the action can be executed
     */
    public static boolean canInvestorGoldAction(Action.InvestorGoldAction action, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if InvestorExhaustForGold ObjectiveCard is active
        boolean hasCard = game.getActiveObjectiveCards().stream()
            .anyMatch(card -> card instanceof ObjectiveCard.InvestorExhaustForGold);
        if (!hasCard) {
            return false;
        }
        
        // Check if already used this turn
        if (playerBoard.hasUsedInvestorGoldThisTurn()) {
            return false;
        }
        
        // Check if investor is valid
        Resident investor = action.investor();
        if (investor == null) {
            return false;
        }
        
        // Check if resident is an Investor (population level 5)
        if (investor.getPopulationLevel() != 5) {
            return false;
        }
        
        // Check if resident is not exhausted
        if (investor.isExhausted()) {
            return false;
        }
        
        // Check if resident belongs to the player
        if (!playerBoard.getResidents().contains(investor)) {
            return false;
        }
        
        return true;
    }
}
