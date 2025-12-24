package com.anno1800.FactoryMethods;

import com.anno1800.Boardtiles.Shipyard;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating predefined Shipyard instances
 */
public class ShipyardFactory {
    
    /**
     * Creates shipyards for Level 1
     */
    public static List<Shipyard> createLevel1Shipyards(int count) {
        List<Shipyard> shipyards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shipyards.add(new Shipyard(1));
        }
        return shipyards;
    }
    
    /**
     * Creates shipyards for Level 2
     */
    public static List<Shipyard> createLevel2Shipyards(int count) {
        List<Shipyard> shipyards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shipyards.add(new Shipyard(2));
        }
        return shipyards;
    }
    
    /**
     * Creates shipyards for Level 3
     */
    public static List<Shipyard> createLevel3Shipyards(int count) {
        List<Shipyard> shipyards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shipyards.add(new Shipyard(3));
        }
        return shipyards;
    }
}
