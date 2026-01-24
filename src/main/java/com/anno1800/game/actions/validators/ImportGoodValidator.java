package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.Producer;

/**
 * Validates production and trade-related actions.
 */
public class ImportGoodValidator {

    public static boolean canImportGood(Action.ImportGood action, Player player, Game game) {
        // Player must have at least one New World Island
        if (player.getPlayerBoard().getNumNewWorldIslands() <= 0) {
            return false;
        }
        
        int availableTradeChips = player.getPlayerBoard().getAvailableTradeChips();
        // Rule: "Um eine Ressource von einer eigenen Neuen-Welt-Insel nutzen zu können, 
        //        muss ein Handels-Plättchen erschöpft werden"
        int requiredTradeChips = 1; // Import costs 1 trade chip
        
        // STANDARD LOGIC: Check if player has enough trade chips
        if (availableTradeChips >= requiredTradeChips) {
            // Player has enough trade chips - continue to plantation check
        } else {
            // Not enough trade chips - check if ExplorerTrader objective is active
            boolean explorerTraderActive = game.getBoard().getActiveObjectiveCards().stream()
                .anyMatch(card -> card instanceof ObjectiveCard.ExplorerTrader);
            
            if (explorerTraderActive) {
                // ALTERNATIVE: Use explorer chips instead (2 explorer = 1 trade)
                int availableExplorerChips = player.getPlayerBoard().getAvailableExplorerChips();
                int requiredExplorerChips = requiredTradeChips * 2; // 1 trade chip = 2 explorer chips
                if (availableExplorerChips < requiredExplorerChips) {
                    return false; // Not enough explorer chips either
                }
                // Has enough explorer chips - continue to plantation check
            } else {
                return false; // Not enough trade chips and ExplorerTrader not active
            }
        }
        
        // Check if player has a plantation that produces the requested good
        Producer[] plantations = player.getPlayerBoard().getFactories();
        boolean hasPlantation = false;
        
        for (Producer plantation : plantations) {
            // Check if this is a Plantation that produces the requested good
            if (plantation instanceof Plantation) {
                if (plantation.produces() == action.good()) {
                    hasPlantation = true;
                    break;
                }
            }
        }
        
        if (!hasPlantation) {
            return false; // Player doesn't have a plantation that produces this good
        }
        
        return true;
    }
}
