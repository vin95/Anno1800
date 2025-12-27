package com.anno1800.FactoryMethods;

import com.anno1800.Boardtiles.Factory;
import com.anno1800.data.Factories;
import com.anno1800.data.FactoryData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Factory class for creating factory stacks for the game board.
 * Creates stacks of factories based on the number of players.
 */
public class FactoryFactory {
    
    /**
     * Creates all factory stacks from FactoryData.
     * Each factory type gets a stack with numPlayers copies.
     * 
     * @param numPlayers The number of players in the game
     * @return List of factory stacks, one for each factory type
     */
    public static List<Deque<Factory>> createFactoryStacks(int numPlayers) {
        List<Deque<Factory>> factoryStacks = new ArrayList<>();
        int numFactories = (numPlayers <= 2) ? 1 : 2;
        
        // Iterate through all factory types defined in FactoryData
        for (Factories factoryType : Factories.values()) {
            Deque<Factory> stack = new ArrayDeque<>();
            
            // Create numPlayers copies of this factory
            for (int i = 0; i < numFactories; i++) {
                Factory factoryTemplate = FactoryData.getFactory(factoryType);
                // Create a new instance for each copy
                Factory factoryCopy = new Factory(
                    factoryTemplate.type(),
                    factoryTemplate.costs(),
                    factoryTemplate.produces(),
                    factoryTemplate.populationLevel()
                );
                stack.addLast(factoryCopy);
            }
            
            factoryStacks.add(stack);
        }
        
        return factoryStacks;
    }
    
    /**
     * Gets a specific factory type by enum.
     * 
     * @param factoryType The factory type enum
     * @return A new factory instance of the specified type
     */
    public static Factory getFactory(Factories factoryType) {
        return FactoryData.getFactory(factoryType);
    }
}

