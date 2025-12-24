package com.anno1800.data;

public enum Factories {
    // Green Materials - Basic Resources
    SAWMILL("Sawmill"),
    GRAIN_FARM("Grain Farm"),
    POTATO_FARM("Potato Farm"),
    PIG_FARM("Pig Farm"),
    SHEEP_FARM("Sheep Farm"),
    
    // Blue Materials - Worker Goods
    COAL_MINE("Coal Mine"),
    BRICK_FACTORY("Brick Factory"),
    BREWERY("Brewery"),
    BAKERY("Bakery"),
    WAREHOUSE("Warehouse"),
    STEEL_WORKS("Steel Works"),
    SAILMAKERS("Sailmakers"),
    DISTILLERY("Distillery"),
    GLASS_MAKER("Glass Maker"),
    SLAUGHTERHOUSE("Slaughterhouse"),
    SOAP_FACTORY("Soap Factory"),
    CANNERY("Cannery"),
    FRAMEWORK_KNITTERS("Framework Knitters"),
    
    // Red Materials - Artisan Goods
    BRASS_FOUNDRY("Brass Foundry"),
    WINDOW_MAKER("Window Maker"),
    CHAMPAGNE_CELLAR("Champagne Cellar"),
    SPECTACLE_FACTORY("Spectacle Factory"),
    CLOCKMAKERS("Clockmakers"),
    SEWING_MACHINE_FACTORY("Sewing Machine Factory"),
    COTTON_MILL("Cotton Mill"),
    COFFEE_ROASTERS("Coffee Roasters"),
    FUR_DEALER("Fur Dealer"),
    DYNAMITE_FACTORY("Dynamite Factory"),
    WEAPONS_FACTORY("Weapons Factory"),
    RUM_DISTILLERY("Rum Distillery"),
    CIGAR_FACTORY("Cigar Factory"),
    CHOCOLATE_FACTORY("Chocolate Factory"),
    ARTISAN_RESIDENCE("Artisan Residence"),
    
    // Purple Materials - Engineer Goods
    MOTOR_ASSEMBLY("Motor Assembly Line"),
    CAR_FACTORY("Car Factory"),
    BICYCLE_FACTORY("Bicycle Factory"),
    LIGHT_BULB_FACTORY("Light Bulb Factory"),
    GRAMOPHONE_FACTORY("Gramophone Factory"),
    HEAVY_WEAPONS_FACTORY("Heavy Weapons Factory"),
    ENGINEER_RESIDENCE("Engineer Residence"),
    
    // Turquoise Materials - Investor Goods
    INVESTOR_RESIDENCE("Investor Residence"),
    
    // New World Materials
    CACAO_PLANTATION("Cacao Plantation"),
    SUGAR_PLANTATION("Sugar Plantation"),
    TOBACCO_PLANTATION("Tobacco Plantation"),
    COFFEE_PLANTATION("Coffee Plantation"),
    COTTON_PLANTATION("Cotton Plantation"),
    CAOUTCHOUC_PLANTATION("Caoutchouc Plantation");

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
