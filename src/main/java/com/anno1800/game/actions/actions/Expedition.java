package com.anno1800.game.actions.actions;

import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ExpeditionCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Expedition action.
 * Player reduces availableExplorerChips by 2 and gets up to 3 ExpeditionCards.
 */
public class Expedition {

    /**
     * Performs an expedition action.
     * Reduces player's available explorer chips by 2 and draws up to 3 expedition cards.
     * 
     * @param player The player performing the expedition
     * @param game The current game state
     * @param action The expedition action
     */
    public static void expedition(Player player, Game game, com.anno1800.game.actions.Action.Expedition action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();
        
        // Check if player has enough explorer chips
        if (playerBoard.getAvailableExplorerChips() < 2) {
            throw new IllegalStateException("Player needs at least 2 explorer chips to perform an expedition");
        }
        
        // Reduce explorer chips by 2
        playerBoard.reduceAvailableExplorerChips(2);
        
        // Draw up to 3 expedition cards (or fewer if not enough available)
        int cardsToDraw = Math.min(3, board.getExpeditionStack().size());
        
        for (int i = 0; i < cardsToDraw; i++) {
            ExpeditionCard card = board.drawExpeditionCard();
            playerBoard.getExpeditionCards().add(card);
        }
        
        System.out.println("Expedition completed: Drew " + cardsToDraw + " expedition cards");
    }

    /**
     * Checks if a player can perform an expedition.
     * 
     * @param player The player to check
     * @param game The current game state
     * @return true if the expedition is possible
     */
    public static boolean canExpedition(Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();
        
        // Check if player has enough explorer chips
        if (playerBoard.getAvailableExplorerChips() < 2) {
            return false;
        }
        
        // Check if there are expedition cards available
        return !board.getExpeditionStack().isEmpty();
    }
}