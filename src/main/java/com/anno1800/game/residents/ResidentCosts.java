package com.anno1800.game.residents;

import com.anno1800.data.gamedata.Goods;

import static com.anno1800.data.gamedata.Goods.*;

import java.util.Map;

/**
 * Central data repository for resident settlement and upgrade costs.
 * Defines the costs for settling new residents or upgrading existing ones.
 */
public class ResidentCosts {
    
    /**
     * Represents the cost for settling or upgrading a resident.
     */
    public record Cost(Goods[] goods) {
    }
    
    // Settlement costs for new residents at each level
    private static final Map<Integer, Cost> SETTLEMENT_COSTS = Map.of(
        1, new Cost(new Goods[]{PLANKS}),
        2, new Cost(new Goods[]{PLANKS, BRICKS}),
        3, new Cost(new Goods[]{BRICKS, COAL, GOODS}),
        4, new Cost(new Goods[]{GOODS, STEELBARS, COAL, WINDOWS}),
        5, new Cost(new Goods[]{GOODS, STEELBARS, COAL, WINDOWS, COATS, WORKFORCE_3})
    );
    
    // Upgrade costs to reach a specific level from previous level
    private static final Map<Integer, Cost> UPGRADE_COSTS = Map.of(
        2, new Cost(new Goods[]{BRICKS}),          // Upgrade 1 -> 2
        3, new Cost(new Goods[]{GOODS, COAL}),     // Upgrade 2 -> 3
        4, new Cost(new Goods[]{WINDOWS, STEELBARS}),  // Upgrade 3 -> 4
        5, new Cost(new Goods[]{WORKFORCE_3, COATS})   // Upgrade 4 -> 5
    );
    
    /**
     * Get the settlement cost for a new resident at a specific level.
     * 
     * @param level The population level (1-5)
     * @return The cost to settle a new resident at this level
     * @throws IllegalArgumentException if level is not between 1 and 5
     */
    public static Cost getSettlementCost(int level) {
        Cost cost = SETTLEMENT_COSTS.get(level);
        if (cost == null) {
            throw new IllegalArgumentException("Invalid population level: " + level + ". Must be between 1 and 5.");
        }
        return cost;
    }
    
    /**
     * Get the upgrade cost to reach a specific level.
     * 
     * @param toLevel The target level (2-5)
     * @return The cost to upgrade to this level
     * @throws IllegalArgumentException if toLevel is not between 2 and 5
     */
    public static Cost getUpgradeCost(int toLevel) {
        Cost cost = UPGRADE_COSTS.get(toLevel);
        if (cost == null) {
            throw new IllegalArgumentException("Invalid upgrade level: " + toLevel + ". Must be between 2 and 5.");
        }
        return cost;
    }
    
    /**
     * Get all settlement costs.
     * 
     * @return Unmodifiable map of all settlement costs by level
     */
    public static Map<Integer, Cost> getAllSettlementCosts() {
        return Map.copyOf(SETTLEMENT_COSTS);
    }
    
    /**
     * Get all upgrade costs.
     * 
     * @return Unmodifiable map of all upgrade costs by level
     */
    public static Map<Integer, Cost> getAllUpgradeCosts() {
        return Map.copyOf(UPGRADE_COSTS);
    }
}
