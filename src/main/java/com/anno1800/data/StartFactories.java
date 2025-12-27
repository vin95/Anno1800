package com.anno1800.data;

public enum StartFactories {
    // Green Materials - Basic Resources
    SAWMILL_GREEN("Sawmill Green"),
    GRAIN_FARM_GREEN("Grain Farm"),
    POTATO_FARM_GREEN("Potato Farm"),
    PIG_FARM_GREEN("Pig Farm"),
    SHEEP_FARM_GREEN("Sheep Farm"),
    
    // Red Materials - Artisan Goods
    COAL_MINE_RED("Coal Mine Red"),
    BRICK_FACTORY_RED("Brick Factory Red"),
    WAREHOUSE_RED("Warehouse Red"),
    STEEL_WORKS_RED("Steel Works Red"),
    SAILMAKERS_RED("Sailmakers Red");

    private final String displayName;

    StartFactories(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
