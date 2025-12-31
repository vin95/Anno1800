package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import static com.anno1800.game.residents.ResidentStatus.AT_WORK;
import static com.anno1800.game.residents.ResidentStatus.EXHAUSTED;

/**
 * Validates worker management actions (assigning, exhausting workers).
 */
public class ExhaustWorkerValidator {

    public static boolean canExhaustWorker(Action.ExhaustWorker action, Player player, Game game) {
        // Validate resident is not null
        if (action.resident() == null) {
            return false;
        }
        
        // Check if resident belongs to player
        if (!player.getPlayerBoard().getResidents().contains(action.resident())) {
            return false; // Resident does not belong to the player
        }
        
        // Resident must have status AT_WORK or EXHAUSTED
        if (action.resident().getStatus() != AT_WORK && action.resident().getStatus() != EXHAUSTED) {
            return false; // Resident is not AT_WORK or EXHAUSTED
        }
        
        // Player must have enough gold (1 gold per population level)
        int requiredGold = action.resident().getPopulationLevel();
        if (player.getPlayerBoard().getGold() < requiredGold) {
            return false; // Not enough gold
        }
        
        return true;
    }
}
