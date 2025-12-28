package com.anno1800.Boardtiles;

import com.anno1800.Rewards.Reward;

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
