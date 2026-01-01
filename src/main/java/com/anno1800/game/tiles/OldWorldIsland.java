package com.anno1800.game.tiles;

import com.anno1800.game.rewards.Reward;

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

    // Getter methods
    public int getFreeLandTiles() {
        return freeLandTiles;
    }

    public int getFreeCoastTiles() {
        return freeCoastTiles;
    }

    public int getFreeSeaTiles() {
        return freeSeaTiles;
    }

    public Reward getReward() {
        return reward;
    }

    public Factory[] getFactories() {
        return factories;
    }

    public Shipyard[] getShipyards() {
        return shipyards;
    }

    public TradeShip[] getTradeShips() {
        return tradeShips;
    }

    public ExplorerShip[] getExplorerShips() {
        return explorerShips;
    }

    public String getType() {
        return type;
    }
}
