package com.anno1800.ui.examples;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.board.Board;
import com.anno1800.game.tiles.OldWorldIsland;
import com.anno1800.game.tiles.NewWorldIsland;

/**
 * Example demonstrating the island discovery actions:
 * - DiscoverOldWorldIsland
 * - DiscoverNewWorldIsland
 */
public class IslandDiscoveryExample {
    public static void main(String[] args) {
        // Initialize the game with 2 players
        Game game = new Game(2);
        Player player1 = game.getPlayers()[0];
        PlayerBoard playerBoard = player1.getPlayerBoard();
        Board board = game.getBoard();

        System.out.println("=== Island Discovery Example ===\n");

        // Show initial state
        System.out.println("Initial State:");
        System.out.println("Player 1 Explorer Chips: " + playerBoard.getAvailableExplorerChips());
        System.out.println("Old World Islands Count: " + playerBoard.getNumOldWorldIslands());
        System.out.println("New World Islands Count: " + playerBoard.getNumNewWorldIslands());
        System.out.println("Land Tiles: " + playerBoard.getLandTiles());
        System.out.println("Coast Tiles: " + playerBoard.getCoastTiles());
        System.out.println("Sea Tiles: " + playerBoard.getSeaTiles());
        System.out.println("Available Old World Islands on Board: " + board.getOldWorldIslands().size());
        System.out.println("Available New World Islands on Board: " + board.getNewWorldIslands().size());
        
        // Give player some explorer chips for testing
        playerBoard.increaseAvailableExplorerChips(10);
        System.out.println("\nGave player 10 extra explorer chips for testing");
        System.out.println("Player 1 Explorer Chips: " + playerBoard.getAvailableExplorerChips());

        System.out.println("\n--- Discovering Old World Island ---");

        // Create and execute DiscoverOldWorldIsland action
        Action.DiscoverOldWorldIsland discoverOldWorldAction = new Action.DiscoverOldWorldIsland();
        
        try {
            game.executeAction(discoverOldWorldAction);
            System.out.println("✅ Successfully discovered Old World Island!");
            
            System.out.println("\nAfter Old World Island Discovery:");
            System.out.println("Explorer Chips: " + playerBoard.getAvailableExplorerChips() + 
                             " (should be reduced by " + (playerBoard.getNumOldWorldIslands()) + ")");
            System.out.println("Old World Islands Count: " + playerBoard.getNumOldWorldIslands());
            System.out.println("Land Tiles: " + playerBoard.getLandTiles());
            System.out.println("Coast Tiles: " + playerBoard.getCoastTiles());
            System.out.println("Sea Tiles: " + playerBoard.getSeaTiles());
            System.out.println("Resident Cards: " + playerBoard.getResidentCards().size());
            
        } catch (Exception e) {
            System.err.println("❌ Error discovering Old World Island: " + e.getMessage());
        }

        System.out.println("\n--- Discovering New World Island ---");

        // Create and execute DiscoverNewWorldIsland action
        Action.DiscoverNewWorldIsland discoverNewWorldAction = new Action.DiscoverNewWorldIsland();
        
        try {
            game.executeAction(discoverNewWorldAction);
            System.out.println("✅ Successfully discovered New World Island!");
            
            System.out.println("\nAfter New World Island Discovery:");
            System.out.println("Explorer Chips: " + playerBoard.getAvailableExplorerChips() + 
                             " (should be reduced by " + (playerBoard.getNumNewWorldIslands()) + ")");
            System.out.println("New World Islands Count: " + playerBoard.getNumNewWorldIslands());
            System.out.println("Plantations Count: " + countPlantations(playerBoard));
            System.out.println("Resident Cards: " + playerBoard.getResidentCards().size() + 
                             " (should have increased by 3 from ResidentStack3)");
            
        } catch (Exception e) {
            System.err.println("❌ Error discovering New World Island: " + e.getMessage());
        }

        System.out.println("\n--- Final State ---");
        System.out.println("Total Explorer Chips Used: " + (10 + 1 - playerBoard.getAvailableExplorerChips()) + 
                         " (1 for first old world, 1 for first new world)");
        System.out.println("Total Islands Discovered: " + 
                         (playerBoard.getNumOldWorldIslands() + playerBoard.getNumNewWorldIslands()));
        System.out.println("Remaining Old World Islands on Board: " + board.getOldWorldIslands().size());
        System.out.println("Remaining New World Islands on Board: " + board.getNewWorldIslands().size());

        System.out.println("\n=== Example Complete ===");
    }

    /**
     * Helper method to count non-null plantations
     */
    private static int countPlantations(PlayerBoard playerBoard) {
        int count = 0;
        for (var plantation : playerBoard.getPlantations()) {
            if (plantation != null) {
                count++;
            }
        }
        return count;
    }
}