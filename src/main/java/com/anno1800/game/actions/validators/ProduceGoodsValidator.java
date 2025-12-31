package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;

/**
 * Validates production and trade-related actions.
 */
public class ProduceGoodsValidator {

    public static boolean canProduceGoods(Action.ProduceGoods action, Player player, Game game) {
        Factory factory = action.factory();

        // Check if factory belongs to player
        boolean factoryBelongsToPlayer = false;
        for (Producer f : player.getPlayerBoard().getFactories()) {
            if (f == factory) {
                factoryBelongsToPlayer = true;
                break;
            }
        }

        if (!factoryBelongsToPlayer) {
            return false;
        }

        // Check if at least one slot is empty
        if (factory.getSlot1() != null && factory.getSlot2() != null) {
            return false; // Both slots occupied
        }

        // Check if player has a FIT resident with the correct population level
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == factory.populationLevel() &&
                    resident.getStatus() == com.anno1800.game.residents.ResidentStatus.FIT) {
                return true;
            }
        }

        return false; // No suitable resident found
    }
}
