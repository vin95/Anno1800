package com.anno1800.game.actions.actions;

import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.TradeShip;

import static com.anno1800.game.residents.ResidentStatus.*;

/**
 * Celebrate carnival.
 */
public class Carneval {
    public static void carneval(Player player) {
        PlayerBoard board = player.getPlayerBoard();
        for (Resident resident : board.getResidents()) {
            resident.setStatus(FIT);
        }
        for (Factory factory : board.getFactories()) {
            factory.freeSlots();
        }
        board.resetAvailableTradeChips();
        for (TradeShip ship : board.getTradeShips()) {
            board.increaseAvailableTradeChips(ship.getLevel());
        }
        board.resetAvailableExplorerChips();
        for (ExplorerShip ship : board.getExplorerShips()) {
            board.increaseAvailableExplorerChips(ship.getLevel());
        }
    }
}
