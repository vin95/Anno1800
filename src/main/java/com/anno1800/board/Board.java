package com.anno1800.board;

import com.anno1800.cards.ResidentCard;
import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.NewWorldIsland;
import com.anno1800.Boardtiles.OldWorldIsland;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.FactoryMethods.ExplorerShipFactory;
import com.anno1800.FactoryMethods.FactoryFactory;
import com.anno1800.FactoryMethods.ShipyardFactory;
import com.anno1800.FactoryMethods.TradeShipFactory;
import com.anno1800.cards.ExpeditionCard;
import com.anno1800.data.ExpeditionCardData;
import com.anno1800.data.NewWorldIslandsData;
import com.anno1800.data.OldWorldIslandData;
import com.anno1800.data.ResidentCardData;

import java.util.Deque;
import java.util.List;

/**
 * Game board containing all card stacks
 */
public class Board {
    // 35 Factory stacks with 2 factories each
    private final List<Deque<Factory>> factoryStacks;
    
    // 3 Resident card stacks
    private final Deque<ResidentCard> residentStack1;
    private final Deque<ResidentCard> residentStack2;
    private final Deque<ResidentCard> residentStack3;
    
    // Expedition card stack (also called Relict cards)
    private final Deque<ExpeditionCard> expeditionStack;
    
    // 3 Shipyard stacks (Level 1-3) - noch nicht implementiert
    private final Deque<Shipyard> shipyardLevel1;
    private final Deque<Shipyard> shipyardLevel2;
    private final Deque<Shipyard> shipyardLevel3;
    
    // 3 Trade ship stacks (Level 1-3) - noch nicht implementiert
    private final Deque<TradeShip> tradeShipLevel1;
    private final Deque<TradeShip> tradeShipLevel2;
    private final Deque<TradeShip> tradeShipLevel3;
    
    // 3 Explorer ship stacks (Level 1-3) - noch nicht implementiert
    private final Deque<ExplorerShip> explorerShipLevel1;
    private final Deque<ExplorerShip> explorerShipLevel2;
    private final Deque<ExplorerShip> explorerShipLevel3;

    // Old World Islands
    private final Deque<OldWorldIsland> oldWorldIslands;

    // New World Islands
    private final Deque<NewWorldIsland> newWorldIslands;

    private int farmers = 20;
    private int workers = 15;
    private int artisans = 10;
    private int engineers = 7;
    private int investors = 5;

    private int gold = 100;

    public Board(
        List<Deque<Factory>> factoryStacks,
        Deque<ResidentCard> residentStack1,
        Deque<ResidentCard> residentStack2,
        Deque<ResidentCard> residentStack3,
        Deque<ExpeditionCard> expeditionStack,
        Deque<Shipyard> shipyardLevel1,
        Deque<Shipyard> shipyardLevel2,
        Deque<Shipyard> shipyardLevel3,
        Deque<TradeShip> tradeShipLevel1,
        Deque<TradeShip> tradeShipLevel2,
        Deque<TradeShip> tradeShipLevel3,
        Deque<ExplorerShip> explorerShipLevel1,
        Deque<ExplorerShip> explorerShipLevel2,
        Deque<ExplorerShip> explorerShipLevel3,
        Deque<OldWorldIsland> oldWorldIslands,
        Deque<NewWorldIsland> newWorldIslands
    ) {
        if (factoryStacks.size() != 35) {
            throw new IllegalArgumentException("Board requires exactly 35 factory stacks");
        }
        
        this.factoryStacks = factoryStacks;
        this.residentStack1 = residentStack1;
        this.residentStack2 = residentStack2;
        this.residentStack3 = residentStack3;
        this.expeditionStack = expeditionStack;
        this.shipyardLevel1 = shipyardLevel1;
        this.shipyardLevel2 = shipyardLevel2;
        this.shipyardLevel3 = shipyardLevel3;
        this.tradeShipLevel1 = tradeShipLevel1;
        this.tradeShipLevel2 = tradeShipLevel2;
        this.tradeShipLevel3 = tradeShipLevel3;
        this.explorerShipLevel1 = explorerShipLevel1;
        this.explorerShipLevel2 = explorerShipLevel2;
        this.explorerShipLevel3 = explorerShipLevel3;
        this.oldWorldIslands = oldWorldIslands;
        this.newWorldIslands = newWorldIslands;
    }

    // Getter methods
    public List<Deque<Factory>> getFactoryStacks() {
        return factoryStacks;
    }

    /**
     * Initializes the game board with all card stacks
     */
    public static Board initializeBoard(int numPlayers) {
        // 35 Factory stacks
        List<Deque<Factory>> factoryStacks = FactoryFactory.createFactoryStacks(numPlayers);
        
        // 3 Resident card stacks
        Deque<ResidentCard> residentStack1 = ResidentCardData.getCardsForLevel(2);
        Deque<ResidentCard> residentStack2 = ResidentCardData.getCardsForLevel(5);
        Deque<ResidentCard> residentStack3 = ResidentCardData.getCardsForLevel(7);
        
        // Expedition card stack (also called Relict cards)
        Deque<ExpeditionCard> expeditionStack = ExpeditionCardData.getExpeditionCards();
        
        // 3 Shipyard stacks (Level 1-3)
        Deque<Shipyard> shipyardLevel1 = ShipyardFactory.createLevel1Shipyards(4);
        Deque<Shipyard> shipyardLevel2 = ShipyardFactory.createLevel2Shipyards(6);
        Deque<Shipyard> shipyardLevel3 = ShipyardFactory.createLevel3Shipyards(4);
        
        // 3 Trade ship stacks (Level 1-3)
        Deque<TradeShip> tradeShipLevel1 = TradeShipFactory.createLevel1Ships(6);
        Deque<TradeShip> tradeShipLevel2 = TradeShipFactory.createLevel2Ships(5);
        Deque<TradeShip> tradeShipLevel3 = TradeShipFactory.createLevel3Ships(5);
        
        // 3 Explorer ship stacks (Level 1-3)
        Deque<ExplorerShip> explorerShipLevel1 = ExplorerShipFactory.createLevel1Ships(6);
        Deque<ExplorerShip> explorerShipLevel2 = ExplorerShipFactory.createLevel2Ships(6);
        Deque<ExplorerShip> explorerShipLevel3 = ExplorerShipFactory.createLevel3Ships(6);

        // Old World Islands
        Deque<OldWorldIsland> oldWorldIslands = OldWorldIslandData.createOldWorldIslands();

        // New World Islands
        Deque<NewWorldIsland> newWorldIslands = NewWorldIslandsData.createNewWorldIslands();
        
        return new Board(
            factoryStacks,
            residentStack1, residentStack2, residentStack3,
            expeditionStack,
            shipyardLevel1, shipyardLevel2, shipyardLevel3,
            tradeShipLevel1, tradeShipLevel2, tradeShipLevel3,
            explorerShipLevel1, explorerShipLevel2, explorerShipLevel3,
            oldWorldIslands, newWorldIslands
        );
    }

    public Deque<Factory> getFactoryStacks(int index) {
        if (index < 0 || index >= 35) {
            throw new IllegalArgumentException("Factory stack index must be between 0 and 34");
        }
        return factoryStacks.get(index);
    }

    public Deque<ResidentCard> getResidentStack1() {
        return residentStack1;
    }

    public Deque<ResidentCard> getResidentStack2() {
        return residentStack2;
    }

    public Deque<ResidentCard> getResidentStack3() {
        return residentStack3;
    }

    public Deque<ExpeditionCard> getExpeditionStack() {
        return expeditionStack;
    }

    public Deque<Shipyard> getShipyardLevel1() {
        return shipyardLevel1;
    }

    public Deque<Shipyard> getShipyardLevel2() {
        return shipyardLevel2;
    }

    public Deque<Shipyard> getShipyardLevel3() {
        return shipyardLevel3;
    }

    public Deque<TradeShip> getTradeShipLevel1() {
        return tradeShipLevel1;
    }

    public Deque<TradeShip> getTradeShipLevel2() {
        return tradeShipLevel2;
    }

    public Deque<TradeShip> getTradeShipLevel3() {
        return tradeShipLevel3;
    }

    public Deque<ExplorerShip> getExplorerShipLevel1() {
        return explorerShipLevel1;
    }

    public Deque<ExplorerShip> getExplorerShipLevel2() {
        return explorerShipLevel2;
    }

    public Deque<ExplorerShip> getExplorerShipLevel3() {
        return explorerShipLevel3;
    }

    public Deque<OldWorldIsland> getOldWorldIslands() {
        return oldWorldIslands;
    }

    public Deque<NewWorldIsland> getNewWorldIslands() {
        return newWorldIslands;
    }

    public int getFarmers() {
        return farmers;
    }

    public int getWorkers() {
        return workers;
    }

    public int getArtisans() {
        return artisans;
    }

    public int getEngineers() {
        return engineers;
    }

    public int getInvestors() {
        return investors;
    }

    public int getGold() {
        return gold;
    }

    public void takeFarmer() {
        if (farmers > 0) {
            farmers--;
        }
    }

    public int returnFarmer() {
        farmers++;
        return farmers;
    }

    public void takeWorker() {
        if (workers > 0) {
            workers--;
        }
    }

    public int returnWorker() {
        workers++;
        return workers;
    }

    public void takeArtisan() {
        if (artisans > 0) {
            artisans--;
        }
    }

    public int returnArtisan() {
        artisans++;
        return artisans;
    }

    public void takeEngineer() {
        if (engineers > 0) {
            engineers--;
        }
    }

    public int returnEngineer() {
        engineers++;
        return engineers;
    }

    public void takeInvestor() {
        if (investors > 0) {
            investors--;
        }
    }

    public int returnInvestor() {
        investors++;
        return investors;
    }

    public int takeGold(int amount) {
        if (amount > gold) {
            System.out.println("Not enough gold available");
            gold = 0;
            return amount - gold;
        }
        gold -= amount;
        return amount;
    }

    public void returnGold(int amount) {
        gold += amount;
    }
}