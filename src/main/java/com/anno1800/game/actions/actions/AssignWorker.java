package com.anno1800.game.actions.actions;

import static com.anno1800.game.residents.ResidentStatus.*;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;

/**
 * Assign a resident worker to a factory slot.
 */
public class AssignWorker {
    public static void assignWorker(Player player, Factory factory, Resident resident, int slot) {
        // Setze den Resident in den gewünschten Slot der Factory
        if (slot == 1) {
            factory.setSlot1(resident);
        } else {
            factory.setSlot2(resident);
        }
        // Setze Status auf AT_WORK
        resident.setStatus(AT_WORK);
    }
}
