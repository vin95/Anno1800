package com.anno1800.board;

import com.anno1800.cards.ResidentCard;
import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.cards.RelictCard;

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
    
    // 1 Relict card stack
    private final Deque<RelictCard> relictStack;
    
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

    public Board(
        List<Deque<Factory>> factoryStacks,
        Deque<ResidentCard> residentStack1,
        Deque<ResidentCard> residentStack2,
        Deque<ResidentCard> residentStack3,
        Deque<RelictCard> relictStack,
        Deque<Shipyard> shipyardLevel1,
        Deque<Shipyard> shipyardLevel2,
        Deque<Shipyard> shipyardLevel3,
        Deque<TradeShip> tradeShipLevel1,
        Deque<TradeShip> tradeShipLevel2,
        Deque<TradeShip> tradeShipLevel3,
        Deque<ExplorerShip> explorerShipLevel1,
        Deque<ExplorerShip> explorerShipLevel2,
        Deque<ExplorerShip> explorerShipLevel3
    ) {
        if (factoryStacks.size() != 35) {
            throw new IllegalArgumentException("Board requires exactly 35 factory stacks");
        }
        
        this.factoryStacks = factoryStacks;
        this.residentStack1 = residentStack1;
        this.residentStack2 = residentStack2;
        this.residentStack3 = residentStack3;
        this.relictStack = relictStack;
        this.shipyardLevel1 = shipyardLevel1;
        this.shipyardLevel2 = shipyardLevel2;
        this.shipyardLevel3 = shipyardLevel3;
        this.tradeShipLevel1 = tradeShipLevel1;
        this.tradeShipLevel2 = tradeShipLevel2;
        this.tradeShipLevel3 = tradeShipLevel3;
        this.explorerShipLevel1 = explorerShipLevel1;
        this.explorerShipLevel2 = explorerShipLevel2;
        this.explorerShipLevel3 = explorerShipLevel3;
    }

    // Getter methods
    public List<Deque<Factory>> getFactoryStacks() {
        return factoryStacks;
    }

    public Deque<Factory> getFactoryStack(int index) {
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

    public Deque<RelictCard> getRelictStack() {
        return relictStack;
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
}
