package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Validates SwapResidentCards action.
 * Players can swap up to 3 ResidentCards of any population level.
 */
public class SwapResidentCardsValidator {

    /**
     * Validates SwapResidentCards action.
     * Requirements:
     * - Player must have 1-3 cards to swap
     * - Each card must be on player's board
     * - For each level being swapped, there must be enough cards available on the GameBoard stack
     * 
     * Example: If swapping 2x Level2 + 1x Level5, but only 1 card left on Level2 stack,
     * then only 1x Level2 can be swapped (plus the Level5 if available).
     * 
     * @param action The swap action
     * @param residentCards The cards to swap
     * @param player The player swapping cards
     * @param game The current game
     * @return true if at least one card can be swapped
     */
    public static boolean canSwapResidentCards(Action.SwapResidentCards action, ResidentCard[] residentCards, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board gameBoard = game.getBoard();
        
        // Must swap 1-3 cards
        if (residentCards == null || residentCards.length == 0 || residentCards.length > 3) {
            return false;
        }
        
        // Check if player has all cards to swap
        for (ResidentCard card : residentCards) {
            if (!playerBoard.getResidentCards().contains(card)) {
                return false;
            }
        }
        
        // Count how many cards of each level we want to swap
        int level2Count = 0;
        int level5Count = 0;
        int level7Count = 0;
        
        for (ResidentCard card : residentCards) {
            switch (card.populationLevel()) {
                case 2 -> level2Count++;
                case 5 -> level5Count++;
                case 7 -> level7Count++;
                default -> {
                    return false; // Invalid population level
                }
            }
        }
        
        // Check if enough cards available on each stack
        // At least ONE card must be swappable
        boolean canSwapAny = false;
        
        if (level2Count > 0 && !gameBoard.getResidentStack1().isEmpty()) {
            canSwapAny = true;
        }
        if (level5Count > 0 && !gameBoard.getResidentStack2().isEmpty()) {
            canSwapAny = true;
        }
        if (level7Count > 0 && !gameBoard.getResidentStack3().isEmpty()) {
            canSwapAny = true;
        }
        
        return canSwapAny;
    }
}
