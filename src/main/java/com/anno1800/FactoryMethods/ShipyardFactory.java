package com.anno1800.FactoryMethods;

import com.anno1800.Boardtiles.Shipyard;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Factory class for creating predefined Shipyard instances
 */
public class ShipyardFactory {
    
    /**
     * Creates shipyards for Level 1
     */
    public static Deque<Shipyard> createLevel1Shipyards(int count) {
        Deque<Shipyard> shipyards = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            shipyards.add(new Shipyard(1));
        }
        return shipyards;
    }
    
    /**
     * Creates shipyards for Level 2
     */
    public static Deque<Shipyard> createLevel2Shipyards(int count) {
        Deque<Shipyard> shipyards = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            shipyards.add(new Shipyard(2));
        }
        return shipyards;
    }
    
    /**
     * Creates shipyards for Level 3
     */
    public static Deque<Shipyard> createLevel3Shipyards(int count) {
        Deque<Shipyard> shipyards = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            shipyards.add(new Shipyard(3));
        }
        return shipyards;
    }
}
