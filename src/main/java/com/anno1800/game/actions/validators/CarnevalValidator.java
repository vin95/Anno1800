package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentStatus;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.TradeShip;

/**
 * Validates whether Carneval is a meaningful reset action.
 *
 * Carneval should only be available if it would actually change state:
 * - at least one resident is not FIT, or
 * - at least one built factory has occupied worker slots, or
 * - available chips are below the maximum provided by owned ships.
 */
public class CarnevalValidator {

    public static boolean canCarneval(Action.Carneval action, Player player, Game game) {
        PlayerBoard board = player.getPlayerBoard();

        // 1) Residents reset needed?
        for (Resident resident : board.getResidents()) {
            if (resident.getStatus() != ResidentStatus.FIT) {
                return true;
            }
        }

        // 2) Factory slots occupied (workers assigned) and would be freed?
        Factory[] allFactories = board.getFactories();
        int numFactories = board.getNumFactories();
        for (int i = 0; i < numFactories; i++) {
            Factory factory = allFactories[i];
            if (factory != null && (factory.getSlot1() != null || factory.getSlot2() != null)) {
                return true;
            }
        }

        // 3) Chips would be replenished?
        int maxTradeChips = 0;
        for (TradeShip ship : board.getTradeShips()) {
            maxTradeChips += ship.getLevel();
        }

        int maxExplorerChips = 0;
        for (ExplorerShip ship : board.getExplorerShips()) {
            maxExplorerChips += ship.getLevel();
        }

        if (board.getAvailableTradeChips() < maxTradeChips) {
            return true;
        }
        if (board.getAvailableExplorerChips() < maxExplorerChips) {
            return true;
        }

        // Carneval would not change any relevant state.
        return false;
    }
}
