package com.anno1800.data;

public enum Factories {
    // Green Materials - Basic Resources
    SAWMILL_GREEN("Sawmill Green"),
    GRAIN_FARM_GREEN("Grain Farm"),
    POTATO_FARM_GREEN("Potato Farm"),
    PIG_FARM_GREEN("Pig Farm"),
    SHEEP_FARM_GREEN("Sheep Farm"),
    
    // Blue Materials - Worker Goods
    SAWMILL_BLUE("Sawmill Blue"),
    COAL_MINE_BLUE("Coal Mine Blue"),
    BRICK_FACTORY_BLUE("Brick Factory Blue"),
    BREWERY_BLUE("Brewery"),
    BAKERY_BLUE("Bakery"),
    WAREHOUSE_BLUE("Warehouse Blue"),
    STEEL_WORKS_BLUE("Steel Works Blue"),
    SAILMAKERS_BLUE("Sailmakers Blue"),
    DISTILLERY_BLUE("Distillery"),
    GLASS_MAKER_BLUE("Glass Maker"),
    COTTON_MILL_BLUE("Cotton Mill Blue"),
    SLAUGHTERHOUSE_BLUE("Slaughterhouse"),
    SOAP_FACTORY_BLUE("Soap Factory"),
    CANNERY_BLUE("Cannery"),
    FRAMEWORK_KNITTERS_BLUE("Framework Knitters"),
    
    // Red Materials - Artisan Goods
    COAL_MINE_RED("Coal Mine Red"),
    BRICK_FACTORY_RED("Brick Factory Red"),
    WAREHOUSE_RED("Warehouse Red"),
    STEEL_WORKS_RED("Steel Works Red"),
    SAILMAKERS_RED("Sailmakers Red"),
    BRASS_FOUNDRY_RED("Brass Foundry"),
    WINDOW_MAKER_RED("Window Maker"),
    CHAMPAGNE_CELLAR_RED("Champagne Cellar"),
    SPECTACLE_FACTORY_RED("Spectacle Factory"),
    CLOCKMAKERS_RED("Clockmakers"),
    SEWING_MACHINE_FACTORY_RED("Sewing Machine Factory"),
    COTTON_MILL_RED("Cotton Mill"),
    COFFEE_ROASTERS_RED("Coffee Roasters"),
    FUR_DEALER_RED("Fur Dealer"),
    DYNAMITE_FACTORY_RED("Dynamite Factory"),
    CANNONS_FACTORY_RED("Cannons Factory"),
    RUM_DISTILLERY_RED("Rum Distillery"),
    CIGAR_FACTORY_RED("Cigar Factory"),
    CHOCOLATE_FACTORY_RED("Chocolate Factory"),
    
    // Purple Materials - Engineer Goods
    MOTOR_ASSEMBLY_PURPLE("Motor Assembly Line"),
    CAR_FACTORY_PURPLE("Car Factory"),
    BICYCLE_FACTORY_PURPLE("Bicycle Factory"),
    LIGHT_BULB_FACTORY_PURPLE("Light Bulb Factory"),
    GRAMOPHONE_FACTORY_PURPLE("Gramophone Factory"),
    HEAVY_WEAPONS_FACTORY_PURPLE("Heavy Weapons Factory"),
    
    // New World Materials
    CACAO_PLANTATION("Cacao Plantation"),
    SUGAR_PLANTATION("Sugar Plantation"),
    TOBACCO_PLANTATION("Tobacco Plantation"),
    COFFEE_PLANTATION("Coffee Plantation"),
    COTTON_PLANTATION("Cotton Plantation"),
    RUBBER_PLANTATION("Rubber Plantation");

    private final String displayName;

    Factories(String displayName) {
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
