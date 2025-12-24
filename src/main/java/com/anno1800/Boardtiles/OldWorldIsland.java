package com.anno1800.Boardtiles;

public class OldWorldIsland {
    int freeLandTiles;
    int freeSeaTiles;
    Factory[] factories;
    Shipyard[] shipyards;
    TradeShip[] tradeShips;
    ExplorerShip[] explorerShips;

    public OldWorldIsland(int freeLandTiles, int freeSeaTiles, Factory[] factories,
                         Shipyard[] shipyards, TradeShip[] tradeShips, ExplorerShip[] explorerShips) {
        this.freeLandTiles = freeLandTiles;
        this.freeSeaTiles = freeSeaTiles;
        this.factories = factories;
        this.shipyards = shipyards;
        this.tradeShips = tradeShips;
        this.explorerShips = explorerShips;
    }
}
