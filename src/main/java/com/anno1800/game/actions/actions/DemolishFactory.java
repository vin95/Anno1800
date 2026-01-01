package com.anno1800.game.actions.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.board.Board;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Demolish factory action.
 * Removes a factory from the player's board.
 */
public class DemolishFactory {

    /**
     * Demolishes a factory from the player's board.
     * 
     * @param player The player demolishing the factory
     * @param board The game board
     * @param factory The factory to demolish
     */
    public static void demolishFactory(Player player, Board board, Factory factory) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        if (!canDemolishFactory(player, factory)) {
            throw new IllegalStateException("Cannot demolish factory: " + factory);
        }
        
        // Remove factory from player board (simplified implementation)
        // In a real implementation, this would remove the factory from the player's collection
        System.out.println("Factory demolition not fully implemented - would remove: " + factory);
        
        System.out.println("Demolished factory: " + factory);
    }

    /**
     * Checks if a factory can be demolished.
     * 
     * @param player The player attempting to demolish
     * @param factory The factory to check
     * @return true if the factory can be demolished
     */
    public static boolean canDemolishFactory(Player player, Factory factory) {
        if (player == null || factory == null) {
            return false;
        }
        
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if player owns the factory
        return java.util.Arrays.asList(playerBoard.getFactories()).contains(factory);
    }
}