package com.anno1800.data.gamedata;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.StartFactory;
import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.Producer;

import static com.anno1800.data.gamedata.Goods.*;
import static com.anno1800.data.gamedata.Producers.*;

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
            Producers.SAWMILL_GREEN.getProduces(),
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(GRAIN_FARM_GREEN, new StartFactory(
            GRAIN_FARM_GREEN,
            new Goods[]{},
            Producers.GRAIN_FARM_GREEN.getProduces(),
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(POTATO_FARM_GREEN, new StartFactory(
            POTATO_FARM_GREEN,
            new Goods[]{},
            Producers.POTATO_FARM_GREEN.getProduces(),
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(PIG_FARM_GREEN, new StartFactory(
            PIG_FARM_GREEN,
            new Goods[]{},
            Producers.PIG_FARM_GREEN.getProduces(),
            1,
            1
        ));
        
        PRODUCER_CONFIGS.put(SHEEP_FARM_GREEN, new StartFactory(
            SHEEP_FARM_GREEN,
            new Goods[]{},
            Producers.SHEEP_FARM_GREEN.getProduces(),
            1,
            1
        ));
        // Red Materials - Artisan Goods (Start) (Population Level 1)
        PRODUCER_CONFIGS.put(COAL_MINE_RED, new StartFactory(
            COAL_MINE_RED,
            new Goods[]{PLANKS},
            Producers.COAL_MINE_RED.getProduces(),
            1,
            2
        ));

        PRODUCER_CONFIGS.put(BRICK_FACTORY_RED, new StartFactory(
            BRICK_FACTORY_RED,
            new Goods[]{COAL},
            Producers.BRICK_FACTORY_RED.getProduces(),
            1,
            2
        ));

        PRODUCER_CONFIGS.put(WAREHOUSE_RED, new StartFactory(
            WAREHOUSE_RED,
            new Goods[]{BRICKS, WORKFORCE_3},
            Producers.WAREHOUSE_RED.getProduces(),
            1,
            2
        ));

        PRODUCER_CONFIGS.put(STEEL_WORKS_RED, new StartFactory(
            STEEL_WORKS_RED,
            new Goods[]{BRICKS, COAL},
            Producers.STEEL_WORKS_RED.getProduces(),
            1,
            2
        ));

        PRODUCER_CONFIGS.put(SAILMAKERS_RED, new StartFactory(
            SAILMAKERS_RED,
            new Goods[]{PLANKS, WOOL},
            Producers.SAILMAKERS_RED.getProduces(),
            1,
            2
        ));
        
        // === FACTORIES ===
        // Blue Materials - Worker Goods (Population Level 2)
        PRODUCER_CONFIGS.put(SAWMILL_BLUE, new Factory(
            SAWMILL_BLUE,
            new Goods[]{},
            Producers.SAWMILL_BLUE.getProduces(),
            2,
            1
        ));

        PRODUCER_CONFIGS.put(COAL_MINE_BLUE, new Factory(
            COAL_MINE_BLUE,
            new Goods[]{PLANKS},
            Producers.COAL_MINE_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(BRICK_FACTORY_BLUE, new Factory(
            BRICK_FACTORY_BLUE,
            new Goods[]{COAL},
            Producers.BRICK_FACTORY_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(BREWERY_BLUE, new Factory(
            BREWERY_BLUE,
            new Goods[]{GRAIN, COAL},
            Producers.BREWERY_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(BAKERY_BLUE, new Factory(
            BAKERY_BLUE,
            new Goods[]{GRAIN, COAL},
            Producers.BAKERY_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(WAREHOUSE_BLUE, new Factory(
            WAREHOUSE_BLUE,
            new Goods[]{BRICKS, WORKFORCE_3},
            Producers.WAREHOUSE_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(STEEL_WORKS_BLUE, new Factory(
            STEEL_WORKS_BLUE,
            new Goods[]{BRICKS, COAL},
            Producers.STEEL_WORKS_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(SAILMAKERS_BLUE, new Factory(
            SAILMAKERS_BLUE,
            new Goods[]{PLANKS, WOOL},
            Producers.SAILMAKERS_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(DISTILLERY_BLUE, new Factory(
            DISTILLERY_BLUE,
            new Goods[]{POTATOES, COAL},
            Producers.DISTILLERY_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(GLASS_MAKER_BLUE, new Factory(
            GLASS_MAKER_BLUE,
            new Goods[]{GOODS, COAL},
            Producers.GLASS_MAKER_BLUE.getProduces(),
            2,
            1
        ));

        PRODUCER_CONFIGS.put(COTTON_MILL_BLUE, new Factory(
            COTTON_MILL_BLUE,
            new Goods[]{COTTON, STEAM_GEARS},
            Producers.COTTON_MILL_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(SLAUGHTERHOUSE_BLUE, new Factory(
            SLAUGHTERHOUSE_BLUE,
            new Goods[]{PIGS, COAL},
            Producers.SLAUGHTERHOUSE_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(SOAP_FACTORY_BLUE, new Factory(
            SOAP_FACTORY_BLUE,
            new Goods[]{PIGS, COAL},
            Producers.SOAP_FACTORY_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(CANNERY_BLUE, new Factory(
            CANNERY_BLUE,
            new Goods[]{PIGS, STEELBARS},
            Producers.CANNERY_BLUE.getProduces(),
            2,
            1
        ));
        
        PRODUCER_CONFIGS.put(FRAMEWORK_KNITTERS_BLUE, new Factory(
            FRAMEWORK_KNITTERS_BLUE,
            new Goods[]{WOOL, COAL},
            Producers.FRAMEWORK_KNITTERS_BLUE.getProduces(),
            2,
            1
        ));
        
        // Red Materials - Artisan Goods (Population Level 3)
        PRODUCER_CONFIGS.put(BRASS_FOUNDRY_RED, new Factory(
            BRASS_FOUNDRY_RED,
            new Goods[]{GOODS, COAL},
            Producers.BRASS_FOUNDRY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(WINDOW_MAKER_RED, new Factory(
            WINDOW_MAKER_RED,
            new Goods[]{PLANKS, GLASS},
            Producers.WINDOW_MAKER_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CHAMPAGNE_CELLAR_RED, new Factory(
            CHAMPAGNE_CELLAR_RED,
            new Goods[]{GOODS, GLASS, WORKFORCE_4},
            Producers.CHAMPAGNE_CELLAR_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(SPECTACLE_FACTORY_RED, new Factory(
            SPECTACLE_FACTORY_RED,
            new Goods[]{GLASS, BRASS, WORKFORCE_4},
            Producers.SPECTACLE_FACTORY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CLOCKMAKERS_RED, new Factory(
            CLOCKMAKERS_RED,
            new Goods[]{GLASS, BRASS, WORKFORCE_4},
            Producers.CLOCKMAKERS_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(SEWING_MACHINE_FACTORY_RED, new Factory(
            SEWING_MACHINE_FACTORY_RED,
            new Goods[]{STEELBARS, BRASS, WORKFORCE_4},
            Producers.SEWING_MACHINE_FACTORY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(COTTON_MILL_RED, new Factory(
            COTTON_MILL_RED,
            new Goods[]{COTTON, PLANKS},
            Producers.COTTON_MILL_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(COFFEE_ROASTERS_RED, new Factory(
            COFFEE_ROASTERS_RED,
            new Goods[]{COFFEE_BEANS, COAL},
            Producers.COFFEE_ROASTERS_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(FUR_DEALER_RED, new Factory(
            FUR_DEALER_RED,
            new Goods[]{SEWING_MACHINES, COTTON_FABRIC, GOODS},
            Producers.FUR_DEALER_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(DYNAMITE_FACTORY_RED, new Factory(
            DYNAMITE_FACTORY_RED,
            new Goods[]{PIGS, BRICKS, GOODS},
            Producers.DYNAMITE_FACTORY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CANNONS_FACTORY_RED, new Factory(
            CANNONS_FACTORY_RED,
            new Goods[]{GOODS, STEELBARS, WORKFORCE_4},
            Producers.CANNONS_FACTORY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(RUM_DISTILLERY_RED, new Factory(
            RUM_DISTILLERY_RED,
            new Goods[]{PLANKS, SUGARCANE},
            Producers.RUM_DISTILLERY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CIGAR_FACTORY_RED, new Factory(
            CIGAR_FACTORY_RED,
            new Goods[]{PLANKS, TOBACCO},
            Producers.CIGAR_FACTORY_RED.getProduces(),
            3,
            2
        ));
        
        PRODUCER_CONFIGS.put(CHOCOLATE_FACTORY_RED, new Factory(
            CHOCOLATE_FACTORY_RED,
            new Goods[]{PIGS, CACAO},
            Producers.CHOCOLATE_FACTORY_RED.getProduces(),
            3,
            2
        ));
        
        // Purple Materials - Engineer Goods (Population Level 4)
        PRODUCER_CONFIGS.put(MOTOR_ASSEMBLY_PURPLE, new Factory(
            MOTOR_ASSEMBLY_PURPLE,
            new Goods[]{BRASS, STEELBARS, WORKFORCE_5},
            Producers.MOTOR_ASSEMBLY_PURPLE.getProduces(),
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(CAR_FACTORY_PURPLE, new Factory(
            CAR_FACTORY_PURPLE,
            new Goods[]{RUBBER, STEAM_GEARS, WORKFORCE_5},
            Producers.CAR_FACTORY_PURPLE.getProduces(),
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(BICYCLE_FACTORY_PURPLE, new Factory(
            BICYCLE_FACTORY_PURPLE,
            new Goods[]{WORKFORCE_5, RUBBER, WORKFORCE_5},
            Producers.BICYCLE_FACTORY_PURPLE.getProduces(),
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(LIGHT_BULB_FACTORY_PURPLE, new Factory(
            LIGHT_BULB_FACTORY_PURPLE,
            new Goods[]{COAL, GLASS, WORKFORCE_5},
            Producers.LIGHT_BULB_FACTORY_PURPLE.getProduces(),
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(GRAMOPHONE_FACTORY_PURPLE, new Factory(
            GRAMOPHONE_FACTORY_PURPLE,
            new Goods[]{BRASS, PLANKS, WORKFORCE_5},
            Producers.GRAMOPHONE_FACTORY_PURPLE.getProduces(),
            4,
            3
        ));
        
        PRODUCER_CONFIGS.put(HEAVY_WEAPONS_FACTORY_PURPLE, new Factory(
            HEAVY_WEAPONS_FACTORY_PURPLE,
            new Goods[]{DYNAMITE, STEELBARS, WORKFORCE_5},
            Producers.HEAVY_WEAPONS_FACTORY_PURPLE.getProduces(),
            4,
            3
        ));
        
        // === PLANTATIONS ===
        // New World Materials (Population Level 7)
        PRODUCER_CONFIGS.put(CACAO_PLANTATION, new Plantation(
            CACAO_PLANTATION,
            new Goods[]{},
            Producers.CACAO_PLANTATION.getProduces(),
            7
        ));
        PRODUCER_CONFIGS.put(SUGAR_PLANTATION, new Plantation(
            SUGAR_PLANTATION,
            new Goods[]{},
            Producers.SUGAR_PLANTATION.getProduces(),
            7
        ));
        PRODUCER_CONFIGS.put(TOBACCO_PLANTATION, new Plantation(
            TOBACCO_PLANTATION,
            new Goods[]{},
            Producers.TOBACCO_PLANTATION.getProduces(),
            7
        ));
        PRODUCER_CONFIGS.put(COFFEE_PLANTATION, new Plantation(
            COFFEE_PLANTATION,
            new Goods[]{},
            Producers.COFFEE_PLANTATION.getProduces(),
            7
        ));
        PRODUCER_CONFIGS.put(COTTON_PLANTATION, new Plantation(
            COTTON_PLANTATION,
            new Goods[]{},
            Producers.COTTON_PLANTATION.getProduces(),
            7
        ));
        PRODUCER_CONFIGS.put(RUBBER_PLANTATION, new Plantation(
            RUBBER_PLANTATION,
            new Goods[]{},
            Producers.RUBBER_PLANTATION.getProduces(),
            7
        ));
    }
    
    /**
     * Get all configured producers.
     * 
     * @return Unmodifiable map of all producer configurations
     */
    public static Map<Producers, Producer> getAllProducers() {
        return Map.copyOf(PRODUCER_CONFIGS);
    }
    
    
    public static Factory getFactory(Producers type) {
        Producer producer = getProducer(type);
        if (producer instanceof Factory factory) {
            return factory;
        }
        throw new IllegalArgumentException("Producer " + type + " is not a Factory");
    }

    /**
     * Returns the Plantation instance for the given Producers enum, if it is a Plantation.
     * @param type The Producers enum value
     * @return Plantation instance
     * @throws IllegalArgumentException if the producer is not a Plantation
     */
    public static Plantation getPlantation(Producers type) {
        Producer producer = getProducer(type);
        if (producer instanceof Plantation plantation) {
            return plantation;
        }
        throw new IllegalArgumentException("Producer " + type + " is not a Plantation");
    }

    /**
     * Returns the Producer instance for the given Producers enum.
     * @param type The Producers enum value
     * @return Producer instance
     * @throws IllegalArgumentException if the producer is not found
     */
    public static Producer getProducer(Producers type) {
        Producer producer = PRODUCER_CONFIGS.get(type);
        if (producer != null) {
            return producer;
        }
        throw new IllegalArgumentException("No producer found for type: " + type);
    }
}
