package com.anno1800.FactoryMethods;

import com.anno1800.Boardtiles.ExplorerShip;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating predefined ExplorerShip instances
 */
public class ExplorerShipFactory {
    
    /**
     * Creates explorer ships for Level 1
     */
    public static List<ExplorerShip> createLevel1Ships(int count) {
        List<ExplorerShip> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(1));
        }
        return ships;
    }
    
    /**
     * Creates explorer ships for Level 2
     */
    public static List<ExplorerShip> createLevel2Ships(int count) {
        List<ExplorerShip> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(2));
        }
        return ships;
    }
    
    /**
     * Creates explorer ships for Level 3
     */
    public static List<ExplorerShip> createLevel3Ships(int count) {
        List<ExplorerShip> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ships.add(new ExplorerShip(3));
        }
        return ships;
    }
}
