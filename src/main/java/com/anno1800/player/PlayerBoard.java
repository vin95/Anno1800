package com.anno1800.player;

import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.Boardtiles.shipType;
import com.anno1800.data.FactoryData;
import com.anno1800.residents.Resident;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerBoard {
    
    public enum ShipType {
        ExplorerShip,
        TradeShip
    }
    
    int landTiles = 10;
    int coastTiles = 5;
    int seaTiles = 5;
    int numShips = 0;
    int numFactories = 0;
    int numFactoriesOnCoast = 0;
    int numShipyards = 0;
    int gold = 0;
    
    Factory[] factories = new Factory[15];
    
    ArrayList<Shipyard> shipyards = new ArrayList<>();

    ArrayList<TradeShip> tradeShips = new ArrayList<>();

    ArrayList<ExplorerShip> explorerShips = new ArrayList<>();
    
    ArrayList<Resident> residents = new ArrayList<>();
    
    public PlayerBoard() {}

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    public int getFreeLandTiles() {
        return landTiles - factories.length - numFactoriesOnCoast;
    }

    public int getFreeCoastTiles() {
        return coastTiles - shipyards.size() - numFactoriesOnCoast;
    }

    public int getFreeSeaTiles() {
        return seaTiles - explorerShips.size() - tradeShips.size();
    }

    public Factory[] getFactories() {
        return factories;
    }

    public ArrayList<Shipyard> getShipyards() {
        return shipyards;
    }

    public ArrayList<TradeShip> getTradeShips() {
        return tradeShips;
    }

    public ArrayList<ExplorerShip> getExplorerShips() {
        return explorerShips;
    }

    public void adShip(int level, ShipType type) {
        switch (type) {
            case ExplorerShip -> explorerShips.add(new ExplorerShip(level));
            case TradeShip -> tradeShips.add(new TradeShip(level));
        }
    }

    public int getGold() {
        return gold;
    }

    public void adGold(int gold) {
        this.gold = this.gold + gold;
    }

    public void initializePlayerBoard(Player player) {
        PlayerBoard board = player.getPlayerBoard();
        board.adShip(1, ShipType.ExplorerShip);
        board.adShip(1, ShipType.ExplorerShip);
        board.adShip(1, ShipType.TradeShip);

    }
    
    /**
     * Creates a new Factory instance based on the template from FactoryData.
     * Each player gets their own Factory instances.
     */
    private static Factory copyFactory(com.anno1800.Boardtiles.Producers type) {
        Factory template = (Factory) FactoryData.getProducer(type);
        return new Factory(
            template.getType(),
            template.costs(),
            template.produces(),
            template.populationLevel()
        );
    }
}