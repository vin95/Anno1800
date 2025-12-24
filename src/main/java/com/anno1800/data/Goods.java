package com.anno1800.data;

/**
 * Represents all available goods in Anno 1800.
 * Goods are available in infinite quantity and have no monetary value.
 */
public enum Goods {
    // green Materials
    PLANKS("Planks"),
    CORN("Corn"),
    POTATOES("Potatoes"),
    PIGS("Pigs"),
    WOOL("Wool"),

    // blue Materials
    COAL("Coal"),
    BRICKS("Bricks"),
    BEER("Beer"),
    BREAD("Bread"),
    GOODS("Goods"),
    STEELBARS("Steel Bars"),
    SAILS("Sails"),
    SNAPS("Snaps"),
    GLASS("Glass"),
    SAUSAGE("Sausages"),
    SOAP("Soap"),
    CANNED_MEAT("Canned Meat"),
    WORK_CLOTHES("Work Clothes"),

    // red Materials
    BRASS("Brass"),
    WINDOWS("Windows"),
    CHAMPAGNE("Champagne"),
    GLASSES("Glasses"),
    POCKETWATCHES("Pocketwatches"),
    SEWING_MACHINES("Sewing Machines"),
    COTTON_FABRIC("Cotton Fabric"),
    COFFEE("Coffee"),
    COATS("Coats"),
    DYNAMITE("Dynamite"),
    CANNONS("Cannons"),
    RUM("Rum"),
    CIGARS("Cigars"),
    CHOCOLATE("Chocolate"),
    WORKFORCE_3("Workforce Level 3"),

    // purple Materials
    STEAM_GEARS("Steam Gears"),
    CARS("Cars"),
    HIGHBIKES("Highbikes"),
    LIGHT_BULBS("Light Bulbs"),
    PHONOGRAPHS("Phonographs"),
    BIG_BERTA("Big Berta"),
    WORKFORCE_4("Workforce Level 4"),

    // turquoise Materials
    WORKFORCE_5("Workforce Level 5"),

    // new World Materials
    CACAO("Cacao"),
    SUGARCANE("Sugarcane"),
    TOBACCO("Tobacco"),
    COFFEE_BEANS("Coffee Beans"),
    COTTON("Cotton"),
    RUBBER("Rubber");

    private final String displayName;

    Goods(String displayName) {
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
