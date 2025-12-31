package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;

/**
 * Validates production and trade-related actions.
 */
public class DoOvertimeValidator {

    /**
     * Validates DoOvertime action.
     * Requirements:
     * - Player must have a resident with the given population level and status
     * EXHAUSTED or AT_WORK
     * - Player must have enough gold (populationLevel * 1 gold)
     */
    public static boolean canDoOvertime(Action.DoOvertime action, Player player, Game game) {
        int populationLevel = action.populationLevel();

        // Check if player has a resident with the given population level and status
        // EXHAUSTED or AT_WORK
        boolean hasEligibleResident = false;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == populationLevel &&
                    (resident.getStatus() == com.anno1800.game.residents.ResidentStatus.EXHAUSTED ||
                            resident.getStatus() == com.anno1800.game.residents.ResidentStatus.AT_WORK)) {
                hasEligibleResident = true;
                break;
            }
        }

        if (!hasEligibleResident) {
            return false;
        }

        // Check if player has enough gold (1 gold per population level)
        int requiredGold = populationLevel;
        if (player.getPlayerBoard().getGold() < requiredGold) {
            return false;
        }

        return true;
    }
}
