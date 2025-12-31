package com.anno1800.game.actions.actions;

import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import static com.anno1800.game.residents.ResidentStatus.*;

/**
 * Do overtime with a resident.
 * Changes the resident status from EXHAUSTED or AT_WORK to FIT.
 * Costs gold equal to the population level.
 * 
 * PRECONDITION: ActionValidator has verified:
 * - Player has a resident with the given population level and status EXHAUSTED
 * or AT_WORK
 * - Player has enough gold
 */
public class DoOvertime {
    public static void doOvertime(Player player, int populationLevel) {
        // Find the first resident with the given population level and status EXHAUSTED
        // or AT_WORK
        Resident targetResident = null;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == populationLevel &&
                    (resident.getStatus() == EXHAUSTED || resident.getStatus() == AT_WORK)) {
                targetResident = resident;
                break;
            }
        }

        // Change status to FIT
        targetResident.setStatus(FIT);

        // Deduct gold (1 gold per population level)
        // TODO: Implement gold deduction when PlayerBoard has a method for this
    }
}
