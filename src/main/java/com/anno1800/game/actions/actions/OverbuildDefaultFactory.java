package com.anno1800.game.actions.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.board.Board;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Overbuild default factory action.
 * Replaces a default factory with a new factory.
 */
public class OverbuildDefaultFactory {

    /**
     * Overbuilds a default factory with a new factory.
     * 
     * @param player The player performing the overbuild
     * @param board The game board
     * @param defaultFactory The default factory to overbuild
     * @param newFactory The new factory to replace it with
     */
    public static void overbuildDefaultFactory(Player player, Board board, Factory defaultFactory, Factory newFactory) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        if (!canOverbuildDefaultFactory(player, defaultFactory, newFactory)) {
            throw new IllegalStateException("Cannot overbuild default factory");
        }
        
        // Remove the default factory and add the new factory
        playerBoard.overbuildDefaultFactory(defaultFactory, newFactory);
        
        System.out.println("Overbuilt default factory " + defaultFactory + " with " + newFactory);
    }

    /**
     * Checks if a default factory can be overbuilt.
     * 
     * @param player The player attempting to overbuild
     * @param defaultFactory The default factory to overbuild
     * @param newFactory The new factory to replace it with
     * @return true if the overbuild is possible
     */
    public static boolean canOverbuildDefaultFactory(Player player, Factory defaultFactory, Factory newFactory) {
        if (player == null || defaultFactory == null || newFactory == null) {
            return false;
        }
        
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if the default factory exists and can be overbuilt
        // For now, simple validation - check if player has default factories
        return playerBoard.getDefaultFactories().contains(defaultFactory);
    }
}