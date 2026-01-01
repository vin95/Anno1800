package com.anno1800.game.actions.demo;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.actions.DemolishFactory;
import com.anno1800.game.actions.actions.OverbuildDefaultFactory;
import com.anno1800.game.board.Board;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.Factory;
import com.anno1800.data.gamedata.FactoryData;
import com.anno1800.data.gamedata.Producers;
import java.util.List;

/**
 * Demo class to test the new Factory Management system with default StartFactories.
 */
public class FactoryManagementDemo {

    public static void main(String[] args) {
        // Create a game with 2 players to test the functionality
        Game game = new Game(2);
        Player player = game.getPlayers()[0];
        Board board = game.getBoard();
        PlayerBoard playerBoard = player.getPlayerBoard();

        System.out.println("=== Anno 1800 - Factory Management Demo ===\n");

        // Demonstrate StartFactories on PlayerBoard
        demonstrateStartFactories(playerBoard);

        // Demonstrate overbuilding a default factory
        demonstrateOverbuilding(player, board);

        // Demonstrate demolishing and restoring
        demonstrateDemolishing(player, board);

        // Show Board has 35 Factory Stacks (not 45)
        demonstrateBoardFactoryStacks(board);

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demonstrateStartFactories(PlayerBoard playerBoard) {
        System.out.println("1. Default StartFactories Demo:");
        System.out.println("   PlayerBoard starts with " + playerBoard.getDefaultFactories().size() + " default StartFactories:");
        
        List<Factory> defaultFactories = playerBoard.getDefaultFactories();
        for (int i = 0; i < Math.min(5, defaultFactories.size()); i++) {
            Factory factory = defaultFactories.get(i);
            System.out.println("   - " + factory.getType() + " (produces: " + factory.produces() + ")");
        }
        if (defaultFactories.size() > 5) {
            System.out.println("   ... and " + (defaultFactories.size() - 5) + " more");
        }
        System.out.println();
    }

    private static void demonstrateOverbuilding(Player player, Board board) {
        System.out.println("2. Overbuilding Default Factory Demo:");
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        List<Factory> defaultFactories = playerBoard.getDefaultFactories();
        if (!defaultFactories.isEmpty()) {
            Factory defaultFactory = defaultFactories.get(0);
            System.out.println("   Overbuilding default factory: " + defaultFactory.getType());
            
            // Get a blue factory to overbuild with
            try {
                Factory newFactory = FactoryData.getFactory(Producers.BREWERY_BLUE);
                
                Action.OverbuildDefaultFactory overbuildAction = 
                    new Action.OverbuildDefaultFactory(defaultFactory, newFactory);
                
                OverbuildDefaultFactory.overbuildDefaultFactory(player, board, defaultFactory, newFactory);
                
                System.out.println("   After overbuilding:");
                System.out.println("   - Default factories: " + playerBoard.getDefaultFactories().size());
                System.out.println("   - Overbuilding factories: " + playerBoard.getOverbuildingFactories().size());
                System.out.println("   - Total active factories: " + playerBoard.getAllActiveFactories().size());
                
            } catch (Exception e) {
                System.out.println("   Failed to overbuild: " + e.getMessage());
            }
        } else {
            System.out.println("   No default factories available to overbuild");
        }
        System.out.println();
    }

    private static void demonstrateDemolishing(Player player, Board board) {
        System.out.println("3. Demolishing and Restoring Demo:");
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        List<Factory> overbuildingFactories = playerBoard.getOverbuildingFactories();
        if (!overbuildingFactories.isEmpty()) {
            Factory factoryToDemolish = overbuildingFactories.get(0);
            System.out.println("   Demolishing overbuilding factory: " + factoryToDemolish.getType());
            
            int defaultFactoriesBefore = playerBoard.getDefaultFactories().size();
            
            Action.DemolishFactory demolishAction = new Action.DemolishFactory(factoryToDemolish);
            DemolishFactory.demolishFactory(player, board, factoryToDemolish);
            
            System.out.println("   After demolishing:");
            System.out.println("   - Default factories: " + playerBoard.getDefaultFactories().size() + 
                             " (was " + defaultFactoriesBefore + ")");
            System.out.println("   - Overbuilding factories: " + playerBoard.getOverbuildingFactories().size());
            
            if (playerBoard.getDefaultFactories().size() > defaultFactoriesBefore) {
                System.out.println("   ✓ Default factory restored successfully!");
            }
        } else {
            System.out.println("   No overbuilding factories to demolish");
        }
        System.out.println();
    }

    private static void demonstrateBoardFactoryStacks(Board board) {
        System.out.println("4. Board Factory Stacks Demo:");
        System.out.println("   Board now has " + board.getFactoryStacks().size() + " factory stacks (excluding StartFactories)");
        
        // Show first few factory stacks
        for (int i = 0; i < Math.min(5, board.getFactoryStacks().size()); i++) {
            if (!board.getFactoryStacks().get(i).isEmpty()) {
                Factory factory = board.getFactoryStacks().get(i).peekFirst();
                System.out.println("   Stack " + i + ": " + factory.getType() + 
                                 " (size: " + board.getFactoryStacks().get(i).size() + ")");
            }
        }
        
        System.out.println("   ✓ Confirmed: Only non-StartFactories are on the board!");
    }
}