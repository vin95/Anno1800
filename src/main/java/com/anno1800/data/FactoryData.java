package com.anno1800.data;

import com.anno1800.Boardtiles.Factory;
import static com.anno1800.data.Goods.*;
import static com.anno1800.data.Factories.*;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central data repository for all factory configurations.
 * Defines building costs, production output, and population requirements for each factory type.
 */
public class FactoryData {
    
    private static final Map<Factories, Factory> FACTORY_CONFIGS = new EnumMap<>(Factories.class);
    
    static {
        // Green Materials - Basic Resources (Population Level 1)
        FACTORY_CONFIGS.put(SAWMILL_GREEN, new Factory(
            SAWMILL_GREEN,
            new Goods[]{},
            PLANKS,
            1
        ));
        
        FACTORY_CONFIGS.put(GRAIN_FARM_GREEN, new Factory(
            GRAIN_FARM_GREEN,
            new Goods[]{},
            GRAIN,
            1
        ));
        
        FACTORY_CONFIGS.put(POTATO_FARM_GREEN, new Factory(
            POTATO_FARM_GREEN,
            new Goods[]{},
            POTATOES,
            1
        ));
        
        FACTORY_CONFIGS.put(PIG_FARM_GREEN, new Factory(
            PIG_FARM_GREEN,
            new Goods[]{},
            PIGS,
            1
        ));
        
        FACTORY_CONFIGS.put(SHEEP_FARM_GREEN, new Factory(
            SHEEP_FARM_GREEN,
            new Goods[]{},
            WOOL,
            1
        ));
        
        // Blue Materials - Worker Goods (Population Level 2)
        FACTORY_CONFIGS.put(SAWMILL_BLUE, new Factory(
            SAWMILL_BLUE,
            new Goods[]{},
            PLANKS,
            2
        ));

        FACTORY_CONFIGS.put(COAL_MINE_BLUE, new Factory(
            COAL_MINE_BLUE,
            new Goods[]{PLANKS},  // TODO: Add costs
            COAL,
            2
        ));
        
        FACTORY_CONFIGS.put(BRICK_FACTORY_BLUE, new Factory(
            BRICK_FACTORY_BLUE,
            new Goods[]{COAL},  // TODO: Add costs
            BRICKS,
            2
        ));
        
        FACTORY_CONFIGS.put(BREWERY_BLUE, new Factory(
            BREWERY_BLUE,
            new Goods[]{GRAIN, COAL},  // TODO: Add costs
            BEER,
            2
        ));
        
        FACTORY_CONFIGS.put(BAKERY_BLUE, new Factory(
            BAKERY_BLUE,
            new Goods[]{GRAIN, COAL},  // TODO: Add costs
            BREAD,
            2
        ));
        
        FACTORY_CONFIGS.put(WAREHOUSE_BLUE, new Factory(
            WAREHOUSE_BLUE,
            new Goods[]{BRICKS, WORKFORCE_3},  // TODO: Add costs
            GOODS,
            2
        ));
        
        FACTORY_CONFIGS.put(STEEL_WORKS_BLUE, new Factory(
            STEEL_WORKS_BLUE,
            new Goods[]{BRICKS, COAL},  // TODO: Add costs
            STEELBARS,
            2
        ));
        
        FACTORY_CONFIGS.put(SAILMAKERS_BLUE, new Factory(
            SAILMAKERS_BLUE,
            new Goods[]{PLANKS, WOOL},  // TODO: Add costs
            SAILS,
            2
        ));
        
        FACTORY_CONFIGS.put(DISTILLERY_BLUE, new Factory(
            DISTILLERY_BLUE,
            new Goods[]{POTATOES, COAL},  // TODO: Add costs
            SNAPS,
            2
        ));
        
        FACTORY_CONFIGS.put(GLASS_MAKER_BLUE, new Factory(
            GLASS_MAKER_BLUE,
            new Goods[]{GOODS, COAL},  // TODO: Add costs
            GLASS,
            2
        ));

        FACTORY_CONFIGS.put(COTTON_MILL_BLUE, new Factory(
            COTTON_MILL_BLUE,
            new Goods[]{COTTON, STEAM_GEARS},  // TODO: Add costs
            COTTON_FABRIC,
            2
        ));
        
        FACTORY_CONFIGS.put(SLAUGHTERHOUSE_BLUE, new Factory(
            SLAUGHTERHOUSE_BLUE,
            new Goods[]{PIGS, COAL},  // TODO: Add costs
            SAUSAGE,
            2
        ));
        
        FACTORY_CONFIGS.put(SOAP_FACTORY_BLUE, new Factory(
            SOAP_FACTORY_BLUE,
            new Goods[]{PIGS, COAL},  // TODO: Add costs
            SOAP,
            2
        ));
        
        FACTORY_CONFIGS.put(CANNERY_BLUE, new Factory(
            CANNERY_BLUE,
            new Goods[]{PIGS, STEELBARS},  // TODO: Add costs
            CANNED_MEAT,
            2
        ));
        
        FACTORY_CONFIGS.put(FRAMEWORK_KNITTERS_BLUE, new Factory(
            FRAMEWORK_KNITTERS_BLUE,
            new Goods[]{WOOL, COAL},  // TODO: Add costs
            WORK_CLOTHES,
            2
        ));
        
        // Red Materials - Artisan Goods (Population Level 3)
        FACTORY_CONFIGS.put(COAL_MINE_RED, new Factory(
            COAL_MINE_RED,
            new Goods[]{PLANKS},  // TODO: Add costs
            COAL,
            3
        ));

        FACTORY_CONFIGS.put(BRICK_FACTORY_RED, new Factory(
            BRICK_FACTORY_RED,
            new Goods[]{COAL},  // TODO: Add costs
            BRICKS,
            3
        ));

        FACTORY_CONFIGS.put(WAREHOUSE_RED, new Factory(
            WAREHOUSE_RED,
            new Goods[]{BRICKS, WORKFORCE_3},  // TODO: Add costs
            GOODS,
            3
        ));

        FACTORY_CONFIGS.put(STEEL_WORKS_RED, new Factory(
            STEEL_WORKS_RED,
            new Goods[]{BRICKS, COAL},  // TODO: Add costs
            STEELBARS,
            3
        ));

        FACTORY_CONFIGS.put(SAILMAKERS_RED, new Factory(
            SAILMAKERS_RED,
            new Goods[]{PLANKS, WOOL},  // TODO: Add costs
            SAILS,
            3
        ));

        FACTORY_CONFIGS.put(BRASS_FOUNDRY_RED, new Factory(
            BRASS_FOUNDRY_RED,
            new Goods[]{GOODS, COAL},  // TODO: Add costs
            BRASS,
            3
        ));
        
        FACTORY_CONFIGS.put(WINDOW_MAKER_RED, new Factory(
            WINDOW_MAKER_RED,
            new Goods[]{PLANKS, GLASS},  // TODO: Add costs
            WINDOWS,
            3
        ));
        
        FACTORY_CONFIGS.put(CHAMPAGNE_CELLAR_RED, new Factory(
            CHAMPAGNE_CELLAR_RED,
            new Goods[]{GOODS, GLASS, WORKFORCE_4},  // TODO: Add costs
            CHAMPAGNE,
            3
        ));
        
        FACTORY_CONFIGS.put(SPECTACLE_FACTORY_RED, new Factory(
            SPECTACLE_FACTORY_RED,
            new Goods[]{GLASS, BRASS, WORKFORCE_4},  // TODO: Add costs
            GLASSES,
            3
        ));
        
        FACTORY_CONFIGS.put(CLOCKMAKERS_RED, new Factory(
            CLOCKMAKERS_RED,
            new Goods[]{GLASS, BRASS, WORKFORCE_4},  // TODO: Add costs
            POCKETWATCHES,
            3
        ));
        
        FACTORY_CONFIGS.put(SEWING_MACHINE_FACTORY_RED, new Factory(
            SEWING_MACHINE_FACTORY_RED,
            new Goods[]{STEELBARS, BRASS, WORKFORCE_4},  // TODO: Add costs
            SEWING_MACHINES,
            3
        ));
        
        FACTORY_CONFIGS.put(COTTON_MILL_RED, new Factory(
            COTTON_MILL_RED,
            new Goods[]{COTTON, PLANKS},  // TODO: Add costs
            COTTON_FABRIC,
            3
        ));
        
        FACTORY_CONFIGS.put(COFFEE_ROASTERS_RED, new Factory(
            COFFEE_ROASTERS_RED,
            new Goods[]{COFFEE_BEANS, COAL},  // TODO: Add costs
            COFFEE,
            3
        ));
        
        FACTORY_CONFIGS.put(FUR_DEALER_RED, new Factory(
            FUR_DEALER_RED,
            new Goods[]{SEWING_MACHINES, COTTON_FABRIC, GOODS},  // TODO: Add costs
            COATS,
            3
        ));
        
        FACTORY_CONFIGS.put(DYNAMITE_FACTORY_RED, new Factory(
            DYNAMITE_FACTORY_RED,
            new Goods[]{PIGS, BRICKS, GOODS},  // TODO: Add costs
            DYNAMITE,
            3
        ));
        
        FACTORY_CONFIGS.put(CANNONS_FACTORY_RED, new Factory(
            CANNONS_FACTORY_RED,
            new Goods[]{GOODS, STEELBARS, WORKFORCE_4},  // TODO: Add costs
            CANNONS,
            3
        ));
        
        FACTORY_CONFIGS.put(RUM_DISTILLERY_RED, new Factory(
            RUM_DISTILLERY_RED,
            new Goods[]{PLANKS, SUGARCANE},  // TODO: Add costs
            RUM,
            3
        ));
        
        FACTORY_CONFIGS.put(CIGAR_FACTORY_RED, new Factory(
            CIGAR_FACTORY_RED,
            new Goods[]{PLANKS, TOBACCO},  // TODO: Add costs
            CIGARS,
            3
        ));
        
        FACTORY_CONFIGS.put(CHOCOLATE_FACTORY_RED, new Factory(
            CHOCOLATE_FACTORY_RED,
            new Goods[]{PIGS, CACAO},  // TODO: Add costs
            CHOCOLATE,
            3
        ));
        
        // Purple Materials - Engineer Goods (Population Level 4)
        FACTORY_CONFIGS.put(MOTOR_ASSEMBLY_PURPLE, new Factory(
            MOTOR_ASSEMBLY_PURPLE,
            new Goods[]{BRASS, STEELBARS, WORKFORCE_5},  // TODO: Add costs
            STEAM_GEARS,
            4
        ));
        
        FACTORY_CONFIGS.put(CAR_FACTORY_PURPLE, new Factory(
            CAR_FACTORY_PURPLE,
            new Goods[]{RUBBER, STEAM_GEARS, WORKFORCE_5},  // TODO: Add costs
            CARS,
            4
        ));
        
        FACTORY_CONFIGS.put(BICYCLE_FACTORY_PURPLE, new Factory(
            BICYCLE_FACTORY_PURPLE,
            new Goods[]{WORKFORCE_5, RUBBER, WORKFORCE_5},  // TODO: Add costs
            HIGHBIKES,
            4
        ));
        
        FACTORY_CONFIGS.put(LIGHT_BULB_FACTORY_PURPLE, new Factory(
            LIGHT_BULB_FACTORY_PURPLE,
            new Goods[]{COAL, GLASS, WORKFORCE_5},  // TODO: Add costs
            LIGHT_BULBS,
            4
        ));
        
        FACTORY_CONFIGS.put(GRAMOPHONE_FACTORY_PURPLE, new Factory(
            GRAMOPHONE_FACTORY_PURPLE,
            new Goods[]{BRASS, PLANKS, WORKFORCE_5},  // TODO: Add costs
            GRAMOPHONES,
            4
        ));
        
        FACTORY_CONFIGS.put(HEAVY_WEAPONS_FACTORY_PURPLE, new Factory(
            HEAVY_WEAPONS_FACTORY_PURPLE,
            new Goods[]{DYNAMITE, STEELBARS, WORKFORCE_5},  // TODO: Add costs
            BIG_BERTA,
            4
        ));
        
        // New World Materials (Various Levels)
        FACTORY_CONFIGS.put(CACAO_PLANTATION, new Factory(
            CACAO_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            CACAO,
            7
        ));
        
        FACTORY_CONFIGS.put(SUGAR_PLANTATION, new Factory(
            SUGAR_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            SUGARCANE,
            7
        ));
        
        FACTORY_CONFIGS.put(TOBACCO_PLANTATION, new Factory(
            TOBACCO_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            TOBACCO,
            7
        ));
        
        FACTORY_CONFIGS.put(COFFEE_PLANTATION, new Factory(
            COFFEE_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            COFFEE_BEANS,
            7
        ));
        
        FACTORY_CONFIGS.put(COTTON_PLANTATION, new Factory(
            COTTON_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            COTTON,
            7
        ));
        
        FACTORY_CONFIGS.put(RUBBER_PLANTATION, new Factory(
            RUBBER_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            RUBBER,
            7
        ));
    }
    
    /**
     * Get the factory configuration for a specific factory type.
     * 
     * @param type The factory type
     * @return The factory configuration with costs, production, and requirements
     * @throws IllegalArgumentException if the factory type is not configured
     */
    public static Factory getFactory(Factories type) {
        Factory factory = FACTORY_CONFIGS.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No configuration found for factory: " + type);
        }
        return factory;
    }
    
    /**
     * Get all configured factories.
     * 
     * @return Unmodifiable map of all factory configurations
     */
    public static Map<Factories, Factory> getAllFactories() {
        return Map.copyOf(FACTORY_CONFIGS);
    }
}
