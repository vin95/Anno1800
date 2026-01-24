package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;

/**
 * Handler for the InvestorGoldAction free action.
 * 
 * Rule from ObjectiveCard: "1Investor Fit -> Exhaust und erhalte 5 Gold. 1x pro Zug"
 * 
 * This is a free action that:
 * - Exhausts the specified Investor resident
 * - Grants 5 Gold to the player
 * - Marks the action as used for this turn
 */
public class InvestorGoldAction {

    /**
     * Executes the InvestorGoldAction.
     * 
     * @param player The player performing the action
     * @param game The current game state
     * @param action The InvestorGoldAction action
     */
    public static void investorGoldAction(Player player, Game game, Action.InvestorGoldAction action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Resident investor = action.investor();
        
        // Exhaust the investor
        investor.exhaust();
        
        // Grant 5 Gold to the player
        playerBoard.gainGold(5);
        
        // Mark as used this turn
        playerBoard.markInvestorGoldUsed();
        
        System.out.println(player.getName() + " used Investor Gold: Exhausted an Investor to gain 5 Gold!");
    }
}
