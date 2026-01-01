package com.anno1800.ui.examples;

import com.anno1800.game.tiles.OldWorldIsland;
import com.anno1800.game.tiles.NewWorldIsland;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.TradeShip;
import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.Producers;
import com.anno1800.data.gamedata.FactoryData;

import java.util.List;
import java.util.ArrayList;

/**
 * Demonstrates the island selection logic used in the discovery actions.
 */
public class IslandSelectionDemo {
    public static void main(String[] args) {
        System.out.println("=== Island Selection Logic Demo ===\n");

        demonstrateOldWorldIslandSelection();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateNewWorldIslandSelection();

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demonstrateOldWorldIslandSelection() {
        System.out.println("--- Old World Island Selection ---");
        System.out.println("Simulating player drawing 2 Old World Islands and choosing the best one...\n");

        // Create two test Old World Islands with different attributes
        OldWorldIsland island1 = new OldWorldIsland(
            2, 1, 1, // 4 total tiles
            new Reward.Gold(3),
            new Factory[]{FactoryData.getFactory(Producers.SAWMILL_GREEN)},
            new Shipyard[]{},
            new TradeShip[]{},
            new ExplorerShip[]{}
        );

        OldWorldIsland island2 = new OldWorldIsland(
            3, 2, 2, // 7 total tiles
            new Reward.NewResidents(2, 1),
            new Factory[]{},
            new Shipyard[]{new Shipyard(1)},
            new TradeShip[]{new TradeShip(1)},
            new ExplorerShip[]{}
        );

        List<OldWorldIsland> choices = List.of(island1, island2);

        System.out.println("Island Choice 1:");
        printOldWorldIslandDetails(island1);

        System.out.println("\nIsland Choice 2:");
        printOldWorldIslandDetails(island2);

        // Use the same selection logic as the actual implementation
        OldWorldIsland chosen = selectBestOldWorldIsland(choices);
        OldWorldIsland rejected = (chosen == island1) ? island2 : island1;

        System.out.println("\n🎯 Selection Result:");
        System.out.printf("✅ Chosen: Island %d (Total tiles: %d)%n", 
                        (chosen == island1) ? 1 : 2, 
                        getTotalTiles(chosen));
        System.out.printf("❌ Rejected: Island %d (Total tiles: %d)%n", 
                         (rejected == island1) ? 1 : 2, 
                         getTotalTiles(rejected));

        System.out.println("\n💡 Selection Strategy: Choose island with most total tiles");
        System.out.println("📦 Rejected island would return to bottom of stack");
        System.out.println("🎁 Chosen island's reward would be activated immediately");
    }

    private static void demonstrateNewWorldIslandSelection() {
        System.out.println("--- New World Island Selection ---");
        System.out.println("Simulating player drawing 2 New World Islands and choosing the best one...\n");

        // Create two test New World Islands with different plantation counts
        NewWorldIsland island1 = new NewWorldIsland(new Plantation[]{
            FactoryData.getPlantation(Producers.CACAO_PLANTATION)
        });

        NewWorldIsland island2 = new NewWorldIsland(new Plantation[]{
            FactoryData.getPlantation(Producers.SUGAR_PLANTATION),
            FactoryData.getPlantation(Producers.COFFEE_PLANTATION),
            FactoryData.getPlantation(Producers.TOBACCO_PLANTATION)
        });

        List<NewWorldIsland> choices = List.of(island1, island2);

        System.out.println("Island Choice 1:");
        printNewWorldIslandDetails(island1);

        System.out.println("\nIsland Choice 2:");
        printNewWorldIslandDetails(island2);

        // Use the same selection logic as the actual implementation
        NewWorldIsland chosen = selectBestNewWorldIsland(choices);
        NewWorldIsland rejected = (chosen == island1) ? island2 : island1;

        System.out.println("\n🎯 Selection Result:");
        System.out.printf("✅ Chosen: Island %d (Plantations: %d)%n", 
                         (chosen == island1) ? 1 : 2, 
                         chosen.getPlantations().length);
        System.out.printf("❌ Rejected: Island %d (Plantations: %d)%n", 
                          (rejected == island1) ? 1 : 2, 
                          rejected.getPlantations().length);

        System.out.println("\n💡 Selection Strategy: Choose island with most plantations");
        System.out.println("📦 Rejected island would return to bottom of stack");
        System.out.println("🃏 Player would draw 3 cards from ResidentStack3 (level 7)");
    }

    private static void printOldWorldIslandDetails(OldWorldIsland island) {
        System.out.printf("  🏝️ Tiles: %d land, %d coast, %d sea (Total: %d)%n",
                         island.getFreeLandTiles(), island.getFreeCoastTiles(), 
                         island.getFreeSeaTiles(), getTotalTiles(island));
        System.out.printf("  🎁 Reward: %s%n", island.getReward());
        System.out.printf("  🏭 Buildings: %d factories, %d shipyards%n",
                         island.getFactories().length, island.getShipyards().length);
        System.out.printf("  🚢 Ships: %d trade ships, %d explorer ships%n",
                         island.getTradeShips().length, island.getExplorerShips().length);
    }

    private static void printNewWorldIslandDetails(NewWorldIsland island) {
        System.out.printf("  🏝️ Plantations: %d%n", island.getPlantations().length);
        System.out.print("  🌱 Types: ");
        for (int i = 0; i < island.getPlantations().length; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(island.getPlantations()[i].getType().getDisplayName());
        }
        System.out.println();
    }

    // Copied from DiscoverOldWorldIsland for consistency
    private static OldWorldIsland selectBestOldWorldIsland(List<OldWorldIsland> islandChoices) {
        if (islandChoices.isEmpty()) {
            return null;
        }

        OldWorldIsland bestIsland = islandChoices.get(0);
        int bestTotalTiles = getTotalTiles(bestIsland);

        for (OldWorldIsland island : islandChoices) {
            int totalTiles = getTotalTiles(island);
            if (totalTiles > bestTotalTiles) {
                bestIsland = island;
                bestTotalTiles = totalTiles;
            }
        }

        return bestIsland;
    }

    private static int getTotalTiles(OldWorldIsland island) {
        return island.getFreeLandTiles() + island.getFreeCoastTiles() + island.getFreeSeaTiles();
    }

    // Copied from DiscoverNewWorldIsland for consistency
    private static NewWorldIsland selectBestNewWorldIsland(List<NewWorldIsland> islandChoices) {
        if (islandChoices.isEmpty()) {
            return null;
        }

        NewWorldIsland bestIsland = islandChoices.get(0);
        int bestPlantationCount = bestIsland.getPlantations().length;

        for (NewWorldIsland island : islandChoices) {
            int plantationCount = island.getPlantations().length;
            if (plantationCount > bestPlantationCount) {
                bestIsland = island;
                bestPlantationCount = plantationCount;
            }
        }

        return bestIsland;
    }
}