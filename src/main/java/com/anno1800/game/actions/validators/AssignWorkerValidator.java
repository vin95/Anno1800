package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Producer;
import static com.anno1800.game.residents.ResidentStatus.FIT;

/**
 * Validates worker management actions (assigning, exhausting workers).
 */
public class AssignWorkerValidator {

    public static boolean canAssignWorker(Action.AssignWorker action, Player player, Game game) {
        if ( action.slot() != 1 && action.slot() != 2) {
            return false; // Invalid slot number
        }
        if (action.factory() == null || action.resident() == null) {
            return false; // Factory or resident is null
        }
        
        // Validate slot number
        if (action.slot() != 1 && action.slot() != 2) {
            return false; // Invalid slot number (must be 1 or 2)
        }
        
        if (action.resident().getPopulationLevel() != action.factory().populationLevel()) {
            return false; // Resident population level does not match factory requirement
        }
        if (action.resident().getStatus() != FIT) {
            return false; // Resident is not FIT
        }
        if (!player.getPlayerBoard().getResidents().contains(action.resident())) {
            return false; // Resident does not belong to the player
        }
        
        // Check if factory belongs to player (search through array)
        boolean factoryFound = false;
        for (Producer factory : player.getPlayerBoard().getFactories()) {
            if (factory == action.factory()) {
                factoryFound = true;
                break;
            }
        }
        if (!factoryFound) {
            return false; // Factory does not belong to the player
        }
        
        // Check if the requested slot is available
        if (action.slot() == 1 && action.factory().getSlot1() != null) {
            return false; // Slot 1 is already occupied
        }
        if (action.slot() == 2 && action.factory().getSlot2() != null) {
            return false; // Slot 2 is already occupied
        }

        return true;
    }
}
