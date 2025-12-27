package com.anno1800.player;

import static com.anno1800.Boardtiles.Producers.*;

import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.board.Board;
import com.anno1800.data.FactoryData;
import com.anno1800.residents.Resident;
import com.anno1800.residents.ResidentStatus;

public class PlayerBoard {
    int freeLandTiles = 0;
    int freeCoastTiles = 4;
    int freeSeaTiles = 2;
    int gold = 0;
    
    Factory[] factories = new Factory[]{
        copyFactory(SAWMILL_GREEN),
        copyFactory(GRAIN_FARM_GREEN),
        copyFactory(POTATO_FARM_GREEN),
        copyFactory(PIG_FARM_GREEN),
        copyFactory(SHEEP_FARM_GREEN),
        copyFactory(COAL_MINE_RED),
        copyFactory(BRICK_FACTORY_RED),
        copyFactory(WAREHOUSE_RED),
        copyFactory(STEEL_WORKS_RED),
        copyFactory(SAILMAKERS_RED)
    };
    
    Shipyard[] shipyards = new Shipyard[]{
        new Shipyard(1)
    };

    TradeShip[] tradeShips = new TradeShip[]{
        new TradeShip(1), 
        new TradeShip(1)
    };

    ExplorerShip[] explorerShips = new ExplorerShip[]{
        new ExplorerShip(1)
    };
    
    Resident[] residents = new Resident[]{
        // 4 Level 1 residents
        new Resident(1, ResidentStatus.FIT),
        new Resident(1, ResidentStatus.FIT),
        new Resident(1, ResidentStatus.FIT),
        new Resident(1, ResidentStatus.FIT),
        // 3 Level 2 residents
        new Resident(2, ResidentStatus.FIT),
        new Resident(2, ResidentStatus.FIT),
        new Resident(2, ResidentStatus.FIT),
        // 2 Level 3 residents
        new Resident(3, ResidentStatus.FIT),
        new Resident(3, ResidentStatus.FIT)
    };
    
    public PlayerBoard(Board board) {
        for (Resident resident : residents) {
            switch (resident.getPopulationLevel()) {
                case 1:
                    board.takeFarmer();
                    break;
                case 2:
                    board.takeWorker();
                    break;
                case 3:
                    board.takeArtisan();
                    break;
                default:
                    break;
            }
        }        
    }

    public Resident[] getResidents() {
        return residents;
    }

    public int getFreeLandTiles() {
        return freeLandTiles;
    }

    public int getFreeCoastTiles() {
        return freeCoastTiles;
    }

    public int getFreeSeaTiles() {
        return freeSeaTiles;
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

    public int getGold() {
        return gold;
    }

    public void adGold(int gold) {
        this.gold = this.gold + gold;
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