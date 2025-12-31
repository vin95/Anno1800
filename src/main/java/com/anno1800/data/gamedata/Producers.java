package com.anno1800.data.gamedata;

import static com.anno1800.data.gamedata.Goods.*;

/**
 * Unified enum containing all producer types from Factories, Plantations, and StartFactories.
 * This enum combines all production buildings available in the game.
 */
public enum Producers {
    
    // === START FACTORIES ===
    // Green Materials - Basic Resources
    SAWMILL_GREEN("Sawmill Green", PLANKS),
    GRAIN_FARM_GREEN("Grain Farm", GRAIN),
    POTATO_FARM_GREEN("Potato Farm", POTATOES),
    PIG_FARM_GREEN("Pig Farm", PIGS),
    SHEEP_FARM_GREEN("Sheep Farm", WOOL),
    
    // Red Materials - Artisan Goods (Start)
    COAL_MINE_RED("Coal Mine Red", COAL),
    BRICK_FACTORY_RED("Brick Factory Red", BRICKS),
    WAREHOUSE_RED("Warehouse Red", PLANKS),
    STEEL_WORKS_RED("Steel Works Red", STEELBARS),
    SAILMAKERS_RED("Sailmakers Red", SAILS),
    
    // === FACTORIES ===
    // Blue Materials - Worker Goods
    SAWMILL_BLUE("Sawmill Blue", PLANKS),
    COAL_MINE_BLUE("Coal Mine Blue", COAL),
    BRICK_FACTORY_BLUE("Brick Factory Blue", BRICKS),
    BREWERY_BLUE("Brewery", BEER),
    BAKERY_BLUE("Bakery", BREAD),
    WAREHOUSE_BLUE("Warehouse Blue", PLANKS),
    STEEL_WORKS_BLUE("Steel Works Blue", STEELBARS),
    SAILMAKERS_BLUE("Sailmakers Blue", SAILS),
    DISTILLERY_BLUE("Distillery", SNAPS),
    GLASS_MAKER_BLUE("Glass Maker", GLASSES),
    COTTON_MILL_BLUE("Cotton Mill Blue", WORK_CLOTHES),
    SLAUGHTERHOUSE_BLUE("Slaughterhouse", SAUSAGE),
    SOAP_FACTORY_BLUE("Soap Factory", SOAP),
    CANNERY_BLUE("Cannery", CANNED_MEAT),
    FRAMEWORK_KNITTERS_BLUE("Framework Knitters", WORK_CLOTHES),
    
    // Red Materials - Artisan Goods
    BRASS_FOUNDRY_RED("Brass Foundry", BRASS),
    WINDOW_MAKER_RED("Window Maker", WINDOWS),
    CHAMPAGNE_CELLAR_RED("Champagne Cellar", CHAMPAGNE),
    SPECTACLE_FACTORY_RED("Spectacle Factory", GLASSES),
    CLOCKMAKERS_RED("Clockmakers", POCKETWATCHES),
    SEWING_MACHINE_FACTORY_RED("Sewing Machine Factory", SEWING_MACHINES),
    COTTON_MILL_RED("Cotton Mill", COTTON_FABRIC),
    COFFEE_ROASTERS_RED("Coffee Roasters", COFFEE),
    FUR_DEALER_RED("Fur Dealer", COATS),
    DYNAMITE_FACTORY_RED("Dynamite Factory", DYNAMITE),
    CANNONS_FACTORY_RED("Cannons Factory", CANNONS),
    RUM_DISTILLERY_RED("Rum Distillery", RUM),
    CIGAR_FACTORY_RED("Cigar Factory", CIGARS),
    CHOCOLATE_FACTORY_RED("Chocolate Factory", CHOCOLATE),
    
    // Purple Materials - Engineer Goods
    MOTOR_ASSEMBLY_PURPLE("Motor Assembly Line", STEAM_GEARS),
    CAR_FACTORY_PURPLE("Car Factory", CARS),
    BICYCLE_FACTORY_PURPLE("Bicycle Factory", HIGHBIKES),
    LIGHT_BULB_FACTORY_PURPLE("Light Bulb Factory", LIGHT_BULBS),
    GRAMOPHONE_FACTORY_PURPLE("Gramophone Factory", GRAMOPHONES),
    HEAVY_WEAPONS_FACTORY_PURPLE("Heavy Weapons Factory", BIG_BERTA),
    
    // === PLANTATIONS ===
    // New World Materials
    CACAO_PLANTATION("Cacao Plantation", CACAO),
    SUGAR_PLANTATION("Sugar Plantation", SUGARCANE),
    TOBACCO_PLANTATION("Tobacco Plantation", TOBACCO),
    COFFEE_PLANTATION("Coffee Plantation", COFFEE_BEANS),
    COTTON_PLANTATION("Cotton Plantation", COTTON),
    RUBBER_PLANTATION("Rubber Plantation", RUBBER);
    private final String displayName;
    private final Goods produces;

    Producers(String displayName, Goods produces) {
        this.displayName = displayName;
        this.produces = produces;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public Goods getProduces() {
        return produces;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
