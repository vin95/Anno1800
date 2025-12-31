package com.anno1800.game.factories;

import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.TradeShip;
import java.util.ArrayDeque;
import java.util.Deque;
import com.anno1800.data.gamedata.ShipType;

public class ShipFactory {

    public static Deque<?> createShips(ShipType type, int level, int count) {
        switch (type) {
            case ExplorerShip:
                return createExplorerShips(level, count);
            case TradeShip:
                return createTradeShips(level, count);
            default:
                throw new IllegalArgumentException("Unknown ship type: " + type);
        }
    }

    private static Deque<ExplorerShip> createExplorerShips(int level, int count) {
        Deque<ExplorerShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(level));
        }
        return ships;
    }

    private static Deque<TradeShip> createTradeShips(int level, int count) {
        Deque<TradeShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(level));
        }
        return ships;
    }
}
