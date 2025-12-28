package com.anno1800.game.factories;

import com.anno1800.game.tiles.ExplorerShip;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Factory class for creating predefined ExplorerShip instances
 */
public class ExplorerShipFactory {
    
    /**
     * Creates explorer ships for Level 1
     */
    public static Deque<ExplorerShip> createLevel1Ships(int count) {
        Deque<ExplorerShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(1));
        }
        return ships;
    }
    
    /**
     * Creates explorer ships for Level 2
     */
    public static Deque<ExplorerShip> createLevel2Ships(int count) {
        Deque<ExplorerShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(2));
        }
        return ships;
    }
    
    /**
     * Creates explorer ships for Level 3
     */
    public static Deque<ExplorerShip> createLevel3Ships(int count) {
        Deque<ExplorerShip> ships = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(3));
        }
        return ships;
    }
}
