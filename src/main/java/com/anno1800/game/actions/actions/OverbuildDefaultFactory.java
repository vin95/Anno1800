package com.anno1800.game.actions.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.board.Board;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Overbuild default factory action.
 * Workers in the default factory are exhausted, slots are cleared,
 * and the default factory becomes passive until the new factory is demolished.
 */
public class OverbuildDefaultFactory {

    /**
     * Overbuilds a default factory with a new factory.
     * Workers are exhausted, slots cleared, and the default factory becomes passive.
     * The new factory is taken from the game board.
     * 
     * @param player The player performing the overbuild
     * @param board The game board
     * @param defaultFactory The default factory to overbuild
     * @param newFactory The new factory to build on top of the default
     */
    public static void overbuildDefaultFactory(Player player, Board board, Factory defaultFactory, Factory newFactory) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Take the new factory from the board
        Factory takenFactory = board.takeFactory(newFactory.getType());
        
        // Overbuild the default factory (exhausts workers, clears slots, marks as passive)
        playerBoard.overbuildDefaultFactory(defaultFactory, takenFactory);
    }
}