package com.anno1800.data.gamedata;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.StartFactory;
import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.Producer;
import com.anno1800.game.tiles.Producers;
import static com.anno1800.data.gamedata.Goods.*;
import static com.anno1800.game.tiles.Producers.*;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central data repository for all producer configurations.
 * Defines building costs, production output, and population requirements for each producer type.
 */
public class FactoryData {
    
    private static final Map<Producers, Producer> PRODUCER_CONFIGS = new EnumMap<>(Producers.class);
    
    static {
        // === START FACTORIES ===
        // Green Materials - Basic Resources (Population Level 1)
        PRODUCER_CONFIGS.put(SAWMILL_GREEN, new StartFactory(
            SAWMILL_GREEN,
            new Goods[]{},
            PLANKS,
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(GRAIN_FARM_GREEN, new StartFactory(
            GRAIN_FARM_GREEN,
            new Goods[]{},
            GRAIN,
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(POTATO_FARM_GREEN, new StartFactory(
            POTATO_FARM_GREEN,
            new Goods[]{},
            POTATOES,
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(PIG_FARM_GREEN, new StartFactory(
            PIG_FARM_GREEN,
            new Goods[]{},
            PIGS,
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(SHEEP_FARM_GREEN, new StartFactory(
            SHEEP_FARM_GREEN,
            new Goods[]{},
            WOOL,
            1,
            1
        ));
        // Red Materials - Artisan Goods (Start) (Population Level 1)
        PRODUCER_CONFIGS.put(COAL_MINE_RED, new StartFactory(
            COAL_MINE_RED,
            new Goods[]{PLANKS},
            COAL,
            1,
            2
        ));

        PRODUCER_CONFIGS.put(BRICK_FACTORY_RED, new StartFactory(
            BRICK_FACTORY_RED,
            new Goods[]{COAL},
            BRICKS,
            1,
            2
        ));

        PRODUCER_CONFIGS.put(WAREHOUSE_RED, new StartFactory(
            WAREHOUSE_RED,
            new Goods[]{BRICKS, WORKFORCE_3},
            GOODS,
            1,
            2
        ));

        PRODUCER_CONFIGS.put(STEEL_WORKS_RED, new StartFactory(
            STEEL_WORKS_RED,
            new Goods[]{BRICKS, COAL},
            STEELBARS,
            1,
            2
        ));

        PRODUCER_CONFIGS.put(SAILMAKERS_RED, new StartFactory(
            SAILMAKERS_RED,
            new Goods[]{PLANKS, WOOL},
            SAILS,
            1,
            2
        ));
        
        // === FACTORIES ===
        // Blue Materials - Worker Goods (Population Level 2)
        PRODUCER_CONFIGS.put(SAWMILL_BLUE, new Factory(
            SAWMILL_BLUE,
            new Goods[]{},
            PLANKS,
            2,
            1
        ));

        PRODUCER_CONFIGS.put(COAL_MINE_BLUE, new Factory(
            COAL_MINE_BLUE,
            new Goods[]{PLANKS},
            COAL,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(BRICK_FACTORY_BLUE, new Factory(
            BRICK_FACTORY_BLUE,
            new Goods[]{COAL},
            BRICKS,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(BREWERY_BLUE, new Factory(
            BREWERY_BLUE,
            new Goods[]{GRAIN, COAL},
            BEER,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(BAKERY_BLUE, new Factory(
            BAKERY_BLUE,
            new Goods[]{GRAIN, COAL},
            BREAD,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(WAREHOUSE_BLUE, new Factory(
            WAREHOUSE_BLUE,
            new Goods[]{BRICKS, WORKFORCE_3},
            GOODS,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(STEEL_WORKS_BLUE, new Factory(
            STEEL_WORKS_BLUE,
            new Goods[]{BRICKS, COAL},
            STEELBARS,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(SAILMAKERS_BLUE, new Factory(
            SAILMAKERS_BLUE,
            new Goods[]{PLANKS, WOOL},
            SAILS,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(DISTILLERY_BLUE, new Factory(
            DISTILLERY_BLUE,
            new Goods[]{POTATOES, COAL},
            SNAPS,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(GLASS_MAKER_BLUE, new Factory(
            GLASS_MAKER_BLUE,
            new Goods[]{GOODS, COAL},
            GLASS,
            2,
            1
        ));

        PRODUCER_CONFIGS.put(COTTON_MILL_BLUE, new Factory(
            COTTON_MILL_BLUE,
            new Goods[]{COTTON, STEAM_GEARS},
            COTTON_FABRIC,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(SLAUGHTERHOUSE_BLUE, new Factory(
            SLAUGHTERHOUSE_BLUE,
            new Goods[]{PIGS, COAL},
            SAUSAGE,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(SOAP_FACTORY_BLUE, new Factory(
            SOAP_FACTORY_BLUE,
            new Goods[]{PIGS, COAL},
            SOAP,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(CANNERY_BLUE, new Factory(
            CANNERY_BLUE,
            new Goods[]{PIGS, STEELBARS},
            CANNED_MEAT,
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(FRAMEWORK_KNITTERS_BLUE, new Factory(
            FRAMEWORK_KNITTERS_BLUE,
            new Goods[]{WOOL, COAL},
            WORK_CLOTHES,
            2,
            1
        ));
        
        // Red Materials - Artisan Goods (Population Level 3)
        PRODUCER_CONFIGS.put(BRASS_FOUNDRY_RED, new Factory(
            BRASS_FOUNDRY_RED,
            new Goods[]{GOODS, COAL},
            BRASS,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(WINDOW_MAKER_RED, new Factory(
            WINDOW_MAKER_RED,
            new Goods[]{PLANKS, GLASS},
            WINDOWS,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CHAMPAGNE_CELLAR_RED, new Factory(
            CHAMPAGNE_CELLAR_RED,
            new Goods[]{GOODS, GLASS, WORKFORCE_4},
            CHAMPAGNE,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(SPECTACLE_FACTORY_RED, new Factory(
            SPECTACLE_FACTORY_RED,
            new Goods[]{GLASS, BRASS, WORKFORCE_4},
            GLASSES,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CLOCKMAKERS_RED, new Factory(
            CLOCKMAKERS_RED,
            new Goods[]{GLASS, BRASS, WORKFORCE_4},
            POCKETWATCHES,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(SEWING_MACHINE_FACTORY_RED, new Factory(
            SEWING_MACHINE_FACTORY_RED,
            new Goods[]{STEELBARS, BRASS, WORKFORCE_4},
            SEWING_MACHINES,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(COTTON_MILL_RED, new Factory(
            COTTON_MILL_RED,
            new Goods[]{COTTON, PLANKS},
            COTTON_FABRIC,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(COFFEE_ROASTERS_RED, new Factory(
            COFFEE_ROASTERS_RED,
            new Goods[]{COFFEE_BEANS, COAL},
            COFFEE,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(FUR_DEALER_RED, new Factory(
            FUR_DEALER_RED,
            new Goods[]{SEWING_MACHINES, COTTON_FABRIC, GOODS},
            COATS,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(DYNAMITE_FACTORY_RED, new Factory(
            DYNAMITE_FACTORY_RED,
            new Goods[]{PIGS, BRICKS, GOODS},
            DYNAMITE,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CANNONS_FACTORY_RED, new Factory(
            CANNONS_FACTORY_RED,
            new Goods[]{GOODS, STEELBARS, WORKFORCE_4},
            CANNONS,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(RUM_DISTILLERY_RED, new Factory(
            RUM_DISTILLERY_RED,
            new Goods[]{PLANKS, SUGARCANE},
            RUM,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CIGAR_FACTORY_RED, new Factory(
            CIGAR_FACTORY_RED,
            new Goods[]{PLANKS, TOBACCO},
            CIGARS,
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CHOCOLATE_FACTORY_RED, new Factory(
            CHOCOLATE_FACTORY_RED,
            new Goods[]{PIGS, CACAO},
            CHOCOLATE,
            3,
            2
        ));
        
        // Purple Materials - Engineer Goods (Population Level 4)
        PRODUCER_CONFIGS.put(MOTOR_ASSEMBLY_PURPLE, new Factory(
            MOTOR_ASSEMBLY_PURPLE,
            new Goods[]{BRASS, STEELBARS, WORKFORCE_5},
            STEAM_GEARS,
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(CAR_FACTORY_PURPLE, new Factory(
            CAR_FACTORY_PURPLE,
            new Goods[]{RUBBER, STEAM_GEARS, WORKFORCE_5},
            CARS,
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(BICYCLE_FACTORY_PURPLE, new Factory(
            BICYCLE_FACTORY_PURPLE,
            new Goods[]{WORKFORCE_5, RUBBER, WORKFORCE_5},
            HIGHBIKES,
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(LIGHT_BULB_FACTORY_PURPLE, new Factory(
            LIGHT_BULB_FACTORY_PURPLE,
            new Goods[]{COAL, GLASS, WORKFORCE_5},
            LIGHT_BULBS,
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(GRAMOPHONE_FACTORY_PURPLE, new Factory(
            GRAMOPHONE_FACTORY_PURPLE,
            new Goods[]{BRASS, PLANKS, WORKFORCE_5},
            GRAMOPHONES,
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(HEAVY_WEAPONS_FACTORY_PURPLE, new Factory(
            HEAVY_WEAPONS_FACTORY_PURPLE,
            new Goods[]{DYNAMITE, STEELBARS, WORKFORCE_5},
            BIG_BERTA,
            4,
            3
        ));
        
        // === PLANTATIONS ===
        // New World Materials (Population Level 7)
        PRODUCER_CONFIGS.put(CACAO_PLANTATION, new Plantation(
            CACAO_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            CACAO,
            7
        ));
        PRODUCER_CONFIGS.put(SUGAR_PLANTATION, new Plantation(
            SUGAR_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            SUGARCANE,
            7
        ));
        PRODUCER_CONFIGS.put(TOBACCO_PLANTATION, new Plantation(
            TOBACCO_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            TOBACCO,
            7
        ));
        PRODUCER_CONFIGS.put(COFFEE_PLANTATION, new Plantation(
            COFFEE_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            COFFEE_BEANS,
            7
        ));
        PRODUCER_CONFIGS.put(COTTON_PLANTATION, new Plantation(
            COTTON_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            COTTON,
            7
        ));
        PRODUCER_CONFIGS.put(RUBBER_PLANTATION, new Plantation(
            RUBBER_PLANTATION,
            new Goods[]{},  // TODO: Add costs
            RUBBER,
            7
        ));
    }
    
    /**producer configuration for a specific producer type.
     * 
     * @param type The producer type
     * @return The producer configuration with costs, production, and requirements
     * @throws IllegalArgumentException if the producer type is not configured
     */
    public static Producer getProducer(Producers type) {
        Producer producer = PRODUCER_CONFIGS.get(type);
        if (producer == null) {
            throw new IllegalArgumentException("No configuration found for producer: " + type);
        }
        return producer;
    }
    
    /**
     * Get all configured producers.
     * 
     * @return Unmodifiable map of all producer configurations
     */
    public static Map<Producers, Producer> getAllProducers() {
        return Map.copyOf(PRODUCER_CONFIGS);
    }
    
    /**
     * @deprecated Use {@link #getProducer(Producers)} instead
     */
    @Deprecated
    public static Factory getFactory(Factories type) {
        // For backwards compatibility - convert old Factories enum to new Producers enum
        Producers producerType = Producers.valueOf(type.name());
        Producer producer = getProducer(producerType);
        if (producer instanceof Factory) {
            return (Factory) producer;
        }
        throw new IllegalArgumentException("Producer " + type + " is not a Factory");
    }
}
