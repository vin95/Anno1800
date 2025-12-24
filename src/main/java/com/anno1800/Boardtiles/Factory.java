package com.anno1800.Boardtiles;

import com.anno1800.data.Goods;

/**
 * Represents a production facility.
 * Each factory type can exist twice in the game.
 * 
 * @param costs Input goods required for building this factory
 * @param produces Output good produced by this factory
 * @param workspaces Number of worker slots
 * @param populationLevel Required population level to use this factory
 */
public record Factory(
    Goods[] costs,
    Goods produces,
    int workspaces,
    int populationLevel
) {
}
