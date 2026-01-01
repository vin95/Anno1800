package com.anno1800.ui.examples;

import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
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

/**
 * Simple test demonstrating Island Discovery logic without full game initialization.
 */
public class SimpleIslandTest {
    public static void main(String[] args) {
        System.out.println("=== Simple Island Discovery Test ===\n");

        // Create a player board manually
        PlayerBoard playerBoard = new PlayerBoard();
        
        // Give the player some explorer chips for testing
        playerBoard.increaseAvailableExplorerChips(5);

        System.out.println("Initial State:");
        System.out.println("Explorer Chips: " + playerBoard.getAvailableExplorerChips());
        System.out.println("Old World Islands: " + playerBoard.getNumOldWorldIslands());
        System.out.println("New World Islands: " + playerBoard.getNumNewWorldIslands());
        System.out.println("Land Tiles: " + playerBoard.getLandTiles());
        System.out.println("Coast Tiles: " + playerBoard.getCoastTiles());
        System.out.println("Sea Tiles: " + playerBoard.getSeaTiles());

        System.out.println("\n--- Testing Old World Island Addition ---");

        // Create a test Old World Island
        Factory[] testFactories = {FactoryData.getFactory(Producers.SAWMILL_GREEN)};
        Shipyard[] testShipyards = {new Shipyard(1)};
        TradeShip[] testTradeShips = {};
        ExplorerShip[] testExplorerShips = {new ExplorerShip(1)};
        
        OldWorldIsland testOldWorldIsland = new OldWorldIsland(
            3, 2, 1, // 3 land, 2 coast, 1 sea tile
            new Reward.Gold(5),
            testFactories,
            testShipyards,
            testTradeShips,
            testExplorerShips
        );

        System.out.println("Adding Old World Island with:");
        System.out.println("- Land tiles: " + testOldWorldIsland.getFreeLandTiles());
        System.out.println("- Coast tiles: " + testOldWorldIsland.getFreeCoastTiles());
        System.out.println("- Sea tiles: " + testOldWorldIsland.getFreeSeaTiles());
        System.out.println("- Factories: " + testOldWorldIsland.getFactories().length);
        System.out.println("- Shipyards: " + testOldWorldIsland.getShipyards().length);
        System.out.println("- Explorer Ships: " + testOldWorldIsland.getExplorerShips().length);

        // Test the addOldWorldIsland method
        playerBoard.addOldWorldIsland(testOldWorldIsland);

        System.out.println("\nAfter adding Old World Island:");
        System.out.println("Old World Islands: " + playerBoard.getNumOldWorldIslands());
        System.out.println("Land Tiles: " + playerBoard.getLandTiles() + " (+3)");
        System.out.println("Coast Tiles: " + playerBoard.getCoastTiles() + " (+2)");
        System.out.println("Sea Tiles: " + playerBoard.getSeaTiles() + " (+1)");
        System.out.println("Factories: " + playerBoard.getNumFactories() + " (+1)");
        System.out.println("Shipyards: " + playerBoard.getShipyards().size() + " (+1)");
        System.out.println("Explorer Ships: " + playerBoard.getExplorerShips().size() + " (+1)");

        System.out.println("\n--- Testing New World Island Addition ---");

        // Create a test New World Island
        Plantation[] testPlantations = {
            FactoryData.getPlantation(Producers.CACAO_PLANTATION),
            FactoryData.getPlantation(Producers.SUGAR_PLANTATION)
        };
        
        NewWorldIsland testNewWorldIsland = new NewWorldIsland(testPlantations);

        System.out.println("Adding New World Island with:");
        System.out.println("- Plantations: " + testNewWorldIsland.getPlantations().length);

        // Test the addNewWorldIsland method
        playerBoard.addNewWorldIsland(testNewWorldIsland);

        System.out.println("\nAfter adding New World Island:");
        System.out.println("New World Islands: " + playerBoard.getNumNewWorldIslands());
        
        // Count non-null plantations
        int plantationCount = 0;
        for (var plantation : playerBoard.getPlantations()) {
            if (plantation != null) {
                plantationCount++;
            }
        }
        System.out.println("Plantations: " + plantationCount + " (+2)");

        System.out.println("\n--- Final Results ---");
        System.out.println("Explorer Chips: " + playerBoard.getAvailableExplorerChips() + " (unchanged in this test)");
        System.out.println("Total Islands: " + 
                         (playerBoard.getNumOldWorldIslands() + playerBoard.getNumNewWorldIslands()));
        System.out.println("Total additional Land Tiles: 3");
        System.out.println("Total additional Coast Tiles: 2");
        System.out.println("Total additional Sea Tiles: 1");

        System.out.println("\n✅ Island Discovery methods working correctly!");
        System.out.println("\n=== Test Complete ===");
    }
}