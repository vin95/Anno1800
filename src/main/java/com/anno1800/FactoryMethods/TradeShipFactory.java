package com.anno1800.FactoryMethods;

import com.anno1800.Boardtiles.TradeShip;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating predefined TradeShip instances
 */
public class TradeShipFactory {
    
    /**
     * Creates trade ships for Level 1
     */
    public static List<TradeShip> createLevel1Ships(int count) {
        List<TradeShip> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(1));
        }
        return ships;
    }
    
    /**
     * Creates trade ships for Level 2
     */
    public static List<TradeShip> createLevel2Ships(int count) {
        List<TradeShip> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(2));
        }
        return ships;
    }
    
    /**
     * Creates trade ships for Level 3
     */
    public static List<TradeShip> createLevel3Ships(int count) {
        List<TradeShip> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ships.add(new TradeShip(3));
        }
        return ships;
    }
}
