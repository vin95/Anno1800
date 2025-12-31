package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
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
        
        // Player must have more than 1 available trade chip
        if (player.getPlayerBoard().getAvailableTradeChips() <= 1) {
            return false;
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
