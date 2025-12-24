package com.anno1800.engine;

import com.anno1800.board.Board;
import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.cards.ResidentCard;
import com.anno1800.cards.RelictCard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Main game controller that manages the board and game state
 */
public class Game {
    private final Board board;
    
    public Game() {
        this.board = initializeBoard();
    }
    
    /**
     * Initializes the game board with all card stacks
     */
    private Board initializeBoard() {
        // 35 Factory stacks (each with 2 factories)
        List<Deque<Factory>> factoryStacks = new ArrayList<>(35);
        for (int i = 0; i < 35; i++) {
            factoryStacks.add(new ArrayDeque<>());
        }
        
        // 3 Resident card stacks
        Deque<ResidentCard> residentStack1 = new ArrayDeque<>();
        Deque<ResidentCard> residentStack2 = new ArrayDeque<>();
        Deque<ResidentCard> residentStack3 = new ArrayDeque<>();
        
        // 1 Relict card stack
        Deque<RelictCard> relictStack = new ArrayDeque<>();
        
        // 3 Shipyard stacks (Level 1-3)
        Deque<Shipyard> shipyardLevel1 = new ArrayDeque<>();
        Deque<Shipyard> shipyardLevel2 = new ArrayDeque<>();
        Deque<Shipyard> shipyardLevel3 = new ArrayDeque<>();
        
        // 3 Trade ship stacks (Level 1-3)
        Deque<TradeShip> tradeShipLevel1 = new ArrayDeque<>();
        Deque<TradeShip> tradeShipLevel2 = new ArrayDeque<>();
        Deque<TradeShip> tradeShipLevel3 = new ArrayDeque<>();
        
        // 3 Explorer ship stacks (Level 1-3)
        Deque<ExplorerShip> explorerShipLevel1 = new ArrayDeque<>();
        Deque<ExplorerShip> explorerShipLevel2 = new ArrayDeque<>();
        Deque<ExplorerShip> explorerShipLevel3 = new ArrayDeque<>();
        
        return new Board(
            factoryStacks,
            residentStack1, residentStack2, residentStack3,
            relictStack,
            shipyardLevel1, shipyardLevel2, shipyardLevel3,
            tradeShipLevel1, tradeShipLevel2, tradeShipLevel3,
            explorerShipLevel1, explorerShipLevel2, explorerShipLevel3
        );
    }
    
    public Board getBoard() {
        return board;
    }
}
