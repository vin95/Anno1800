package com.anno1800.game.factories;

import com.anno1800.game.tiles.TradeShip;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Factory class for creating predefined TradeShip instances
 */
public class TradeShipFactory {
    
    /**
     * Creates trade ships for Level 1
     */
    public static Deque<TradeShip> createLevel1Ships(int count) {
        Deque<TradeShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(1));
        }
        return ships;
    }
    
    /**
     * Creates trade ships for Level 2
     */
    public static Deque<TradeShip> createLevel2Ships(int count) {
        Deque<TradeShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(2));
        }
        return ships;
    }
    
    /**
     * Creates trade ships for Level 3
     */
    public static Deque<TradeShip> createLevel3Ships(int count) {
        Deque<TradeShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(3));
        }
        return ships;
    }
}
