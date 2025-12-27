package com.anno1800.Boardtiles;

import static com.anno1800.data.Factories.BRICK_FACTORY_BLUE;
import static com.anno1800.data.Factories.COAL_MINE_BLUE;
import static com.anno1800.data.Factories.SAILMAKERS_BLUE;
import static com.anno1800.data.Factories.STEEL_WORKS_BLUE;
import static com.anno1800.data.Factories.WAREHOUSE_BLUE;

import java.util.ArrayDeque;
import java.util.Deque;

import com.anno1800.Rewards.Reward;
import com.anno1800.data.FactoryData;

public class OldWorldIsland implements Island {
    int freeLandTiles;
    int freeCoastTiles;
    int freeSeaTiles;
    Reward reward;
    Factory[] factories;
    Shipyard[] shipyards;
    TradeShip[] tradeShips;
    ExplorerShip[] explorerShips;
    String type = "Old World Island";

    public OldWorldIsland(int freeLandTiles, int freeCoastTiles, int freeSeaTiles, Reward reward, Factory[] factories,
                         Shipyard[] shipyards, TradeShip[] tradeShips, ExplorerShip[] explorerShips) {
        this.freeLandTiles = freeLandTiles;
        this.freeCoastTiles = freeCoastTiles;
        this.freeSeaTiles = freeSeaTiles;
        this.reward = reward;
        this.factories = factories;
        this.shipyards = shipyards;
        this.tradeShips = tradeShips;
        this.explorerShips = explorerShips;
    }
}
