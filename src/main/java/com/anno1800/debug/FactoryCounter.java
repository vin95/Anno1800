package com.anno1800.debug;

import com.anno1800.data.gamedata.Producers;
import com.anno1800.data.gamedata.FactoryData;

public class FactoryCounter {
    public static void main(String[] args) {
        int factoryCount = 0;
        int startFactoryCount = 0;
        int plantationCount = 0;
        
        for (Producers producer : Producers.values()) {
            try {
                FactoryData.getFactory(producer);
                if (isStartFactory(producer)) {
                    startFactoryCount++;
                    System.out.println("StartFactory: " + producer.getDisplayName());
                } else {
                    factoryCount++;
                    System.out.println("Factory: " + producer.getDisplayName());
                }
            } catch (IllegalArgumentException e) {
                plantationCount++;
                System.out.println("Plantation: " + producer.getDisplayName());
            }
        }
        
        System.out.println("\nTotal StartFactories: " + startFactoryCount);
        System.out.println("Total Factories (on Board): " + factoryCount);
        System.out.println("Total Plantations: " + plantationCount);
        System.out.println("Total Producers: " + Producers.values().length);
        System.out.println("\n✓ Board should have " + factoryCount + " factory stacks");
    }
    
    private static boolean isStartFactory(Producers producer) {
        return switch (producer) {
            // GREEN StartFactories
            case SAWMILL_GREEN, GRAIN_FARM_GREEN, POTATO_FARM_GREEN, 
                 PIG_FARM_GREEN, SHEEP_FARM_GREEN,
            // RED StartFactories
                 COAL_MINE_RED, BRICK_FACTORY_RED, WAREHOUSE_RED, 
                 STEEL_WORKS_RED, SAILMAKERS_RED -> true;
            default -> false;
        };
    }
}