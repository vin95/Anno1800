package com.anno1800.game.actions.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.board.Board;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentStatus;

/**
 * Demolish factory action.
 * Removes a factory from the player's board.
 * Residents working in the factory are exhausted.
 * The factory is returned to its stack on the game board.
 */
public class DemolishFactory {

    /**
     * Demolishes a factory from the player's board.
     * Workers in the factory are exhausted, slots are cleared,
     * and the factory is returned to its stack on the game board.
     * 
     * @param player The player demolishing the factory
     * @param board The game board
     * @param factory The factory to demolish
     */
    public static void demolishFactory(Player player, Board board, Factory factory) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Exhaust all residents working in the factory
        Resident slot1 = factory.getSlot1();
        Resident slot2 = factory.getSlot2();
        
        if (slot1 != null) {
            slot1.setStatus(ResidentStatus.EXHAUSTED);
        }
        if (slot2 != null) {
            slot2.setStatus(ResidentStatus.EXHAUSTED);
        }
        
        // Clear the factory's work slots
        factory.freeSlots();
        
        // Remove factory from player board
        playerBoard.removeFactory(factory);
        
        // Return factory to its stack on the game board
        board.returnFactory(factory);
    }
}