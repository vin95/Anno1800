package com.anno1800.FactoryMethods;

import com.anno1800.Boardtiles.Factory;
import com.anno1800.data.Goods;

import static com.anno1800.data.Goods.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating predefined Factory instances
 */
public class FactoryFactory {
    
    /**
     * Creates all 35 different factory types (each exists twice in the game)
     */
    public static List<Factory> createAllFactories() {
        List<Factory> factories = new ArrayList<>();
        
        // Tier 2 - Workers (Population Level 2)
        factories.add(new Factory(new Goods[]{}, PLANKS, 2, 2));
        factories.add(new Factory(new Goods[]{PLANKS}, COAL, 2, 2));
        factories.add(new Factory(new Goods[]{COAL}, BRICKS, 2, 2));
        factories.add(new Factory(new Goods[]{CORN, COAL}, BEER, 2, 2));
        factories.add(new Factory(new Goods[]{CORN, COAL}, BREAD, 2, 2));
        factories.add(new Factory(new Goods[]{BRICKS, WORKFORCE_3}, GOODS, 2, 2));
        factories.add(new Factory(new Goods[]{BRICKS, COAL}, STEELBARS, 2, 2));
        factories.add(new Factory(new Goods[]{PLANKS, WOOL}, SAILS, 2, 2));
        factories.add(new Factory(new Goods[]{POTATOES, COAL}, SNAPS, 2, 2));
        factories.add(new Factory(new Goods[]{GOODS, COAL}, GLASS, 2, 2));
        factories.add(new Factory(new Goods[]{STEAM_GEARS, COTTON}, COTTON_FABRIC, 2, 2));
        factories.add(new Factory(new Goods[]{PIGS, COAL}, SAUSAGE, 2, 2));
        factories.add(new Factory(new Goods[]{PIGS, COAL}, SOAP, 2, 2));
        factories.add(new Factory(new Goods[]{PIGS, STEELBARS}, CANNED_MEAT, 2, 2));
        factories.add(new Factory(new Goods[]{WOOL, COAL}, WORK_CLOTHES, 2, 2));
        
        // Tier 3 - Artisans (Population Level 3)
        factories.add(new Factory(new Goods[]{GOODS, COAL}, BRASS, 2, 3));
        factories.add(new Factory(new Goods[]{PLANKS, GLASS}, WINDOWS, 2, 3));
        factories.add(new Factory(new Goods[]{GLASS, GOODS, WORKFORCE_4}, CHAMPAGNE, 2, 3));
        factories.add(new Factory(new Goods[]{GLASS, BRASS, WORKFORCE_4}, GLASSES, 2, 3));
        factories.add(new Factory(new Goods[]{GLASS, BRASS, WORKFORCE_4}, POCKETWATCHES, 2, 3));
        factories.add(new Factory(new Goods[]{STEELBARS, BRASS, WORKFORCE_4}, SEWING_MACHINES, 2, 3));
        factories.add(new Factory(new Goods[]{COTTON, PLANKS}, COTTON_FABRIC, 2, 3));
        factories.add(new Factory(new Goods[]{COFFEE_BEANS, COAL}, COFFEE, 2, 3));
        factories.add(new Factory(new Goods[]{SEWING_MACHINES, COTTON_FABRIC, GOODS}, COATS, 2, 3));
        factories.add(new Factory(new Goods[]{PIGS, BRICKS, GOODS}, DYNAMITE, 2, 3));
        factories.add(new Factory(new Goods[]{STEELBARS, GOODS, WORKFORCE_4}, CANNONS, 2, 3));
        factories.add(new Factory(new Goods[]{PLANKS, SUGARCANE}, RUM, 2, 3));
        factories.add(new Factory(new Goods[]{TOBACCO, PLANKS}, CIGARS, 2, 3));
        factories.add(new Factory(new Goods[]{PIGS, CACAO}, CHOCOLATE, 2, 3));
        
        
        // Tier 4 - Engineers (Population Level 4)
        factories.add(new Factory(new Goods[]{BRASS, STEELBARS, WORKFORCE_5}, STEAM_GEARS, 2, 4));
        factories.add(new Factory(new Goods[]{RUBBER, STEAM_GEARS, WORKFORCE_5}, CARS, 2, 4));
        factories.add(new Factory(new Goods[]{STEELBARS, RUBBER, WORKFORCE_5}, HIGHBIKES, 2, 4));
        factories.add(new Factory(new Goods[]{COAL, GLASS, WORKFORCE_5}, LIGHT_BULBS, 2, 4));
        factories.add(new Factory(new Goods[]{BRASS, PLANKS, WORKFORCE_5}, PHONOGRAPHS, 2, 4));
        factories.add(new Factory(new Goods[]{DYNAMITE, STEAM_GEARS, WORKFORCE_5}, BIG_BERTA, 2, 4)); 
        
        return factories;
    }
    
    /**
     * Gets a specific factory by index (0-34)
     */
    public static Factory getFactory(int index) {
        List<Factory> factories = createAllFactories();
        if (index < 0 || index >= factories.size()) {
            throw new IllegalArgumentException("Factory index must be between 0 and 34");
        }
        return factories.get(index);
    }
}
