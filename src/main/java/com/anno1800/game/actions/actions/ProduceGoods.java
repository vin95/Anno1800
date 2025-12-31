package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Factory;
import static com.anno1800.game.residents.ResidentStatus.*;

/**
 * Produce goods in a factory by assigning a FIT resident to an empty slot.
 * The good is considered produced immediately and must be consumed by another
 * action
 * (e.g., BuildFactory, BuildShipyard) in the same turn.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 * 
 * IMPORTANT: ProduceGoods is always coupled with another action that consumes
 * the goods.
 * It cannot be executed standalone - the goods must be consumed immediately.
 */
public class ProduceGoods {
    public static Goods produceGoods(Player player, Factory factory) {
        // Find a FIT resident with the correct population level
        Resident residentToAssign = null;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == factory.populationLevel() &&
                    resident.getStatus() == FIT) {
                residentToAssign = resident;
                break;
            }
        }

        // Assign resident to first empty slot
        if (factory.getSlot1() == null) {
            factory.setSlot1(residentToAssign);
        } else {
            factory.setSlot2(residentToAssign);
        }

        // Update resident status to AT_WORK
        residentToAssign.setStatus(AT_WORK);

        System.out.println("Resident (level " + residentToAssign.getPopulationLevel() +
                ") assigned to factory " + factory.getType() +
                ". Produced: " + factory.produces());

        return factory.produces();
    }
}
