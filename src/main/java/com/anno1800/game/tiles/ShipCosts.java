package com.anno1800.game.tiles;

import com.anno1800.data.gamedata.Goods;
import static com.anno1800.data.gamedata.Goods.*;

/**
 * Central data repository for shipyard and ship construction costs.
 */
public class ShipCosts {
    
    /**
     * Get the construction cost for a shipyard of a specific level.
     * 
     * @param level The shipyard level (1-3)
     * @return Array of required goods
     */
    public static Goods[] getShipyardCost(int level) {
        return switch (level) {
            case 1 -> new Goods[]{}; // Level 1 shipyard has no build cost
            case 2 -> new Goods[]{PLANKS, BRICKS, STEELBARS};
            case 3 -> new Goods[]{PLANKS, BRICKS, STEELBARS, WINDOWS};
            default -> throw new IllegalArgumentException("Invalid shipyard level: " + level + ". Must be 1-3.");
        };
    }
    
    /**
     * Get the construction cost for a ship of a specific type and level.
     * 
     * @param level The ship level (1-3)
     * @return Array of required goods
     */
    public static Goods[] getShipCost(int level) {
        return switch (level) {
            case 1 -> new Goods[]{PLANKS, SAILS};
            case 2 -> new Goods[]{PLANKS, SAILS, STEELBARS};
            case 3 -> new Goods[]{PLANKS, SAILS, STEELBARS, CANNONS};
            default -> throw new IllegalArgumentException("Invalid ship level: " + level + ". Must be 1-3.");
        };
    }
}
