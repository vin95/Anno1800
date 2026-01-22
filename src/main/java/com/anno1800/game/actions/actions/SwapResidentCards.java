package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

import java.util.Deque;

/**
 * Swap up to 3 ResidentCards with cards from the GameBoard stacks.
 * 
 * PRECONDITION: ActionValidator has verified player owns the cards and at least one can be swapped.
 * 
 * Rules:
 * - Old cards are placed at the BOTTOM of their respective stacks (drawn last)
 * - New cards are drawn from the TOP of the stack
 * - Stacks are NEVER shuffled after initialization
 * - If a stack has fewer cards than requested, swap as many as possible
 */
public class SwapResidentCards {
    
    /**
     * Swaps resident cards between player and game board.
     * For each card:
     * 1. Remove from player's hand
     * 2. Place at bottom of corresponding stack (level 2→Stack1, 5→Stack2, 7→Stack3)
     * 3. If stack has cards, draw new card from top
     * 4. Add new card to player's hand
     * 
     * @param player The player swapping cards
     * @param game The current game
     * @param action The swap action with cards to swap
     */
    public static void swapResidentCards(Player player, Game game, Action.SwapResidentCards action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board gameBoard = game.getBoard();
        ResidentCard[] cardsToSwap = action.cardsToSwap();
        
        // Validate
        if (cardsToSwap == null || cardsToSwap.length == 0 || cardsToSwap.length > 3) {
            throw new IllegalArgumentException("Must swap 1-3 cards, got: " + 
                (cardsToSwap == null ? "null" : cardsToSwap.length));
        }
        
        System.out.println("Swapping " + cardsToSwap.length + " resident card(s)");
        int swappedCount = 0;
        
        // Process each card
        for (ResidentCard oldCard : cardsToSwap) {
            // Verify player has this card
            if (!playerBoard.getResidentCards().contains(oldCard)) {
                System.out.println("  Skipping card (not owned): PopLv " + oldCard.populationLevel());
                continue;
            }
            
            int level = oldCard.populationLevel();
            Deque<ResidentCard> stack = getStackForLevel(gameBoard, level);
            
            // Check if stack has cards available
            if (stack.isEmpty()) {
                System.out.println("  Cannot swap Level " + level + " card: stack is empty");
                continue;
            }
            
            // 1. Remove old card from player's hand
            playerBoard.getResidentCards().remove(oldCard);
            
            // 2. Place old card at BOTTOM of stack (will be drawn last)
            stack.addLast(oldCard);
            System.out.println("  Returned Level " + level + " card to bottom of stack");
            
            // 3. Draw new card from TOP of stack
            ResidentCard newCard = stack.removeFirst();
            
            // 4. Add new card to player's hand
            playerBoard.getResidentCards().add(newCard);
            System.out.println("  Drew new Level " + level + " card: " + 
                java.util.Arrays.toString(newCard.needs()) + " → " + newCard.reward());
            
            swappedCount++;
        }
        
        if (swappedCount == 0) {
            throw new IllegalStateException("No cards could be swapped (all stacks empty or cards not owned)");
        }
        
        System.out.println("Successfully swapped " + swappedCount + " card(s)");
    }
    
    /**
     * Get the appropriate resident card stack for a given population level.
     * Level 2 → Stack1, Level 5 → Stack2, Level 7 → Stack3
     */
    private static Deque<ResidentCard> getStackForLevel(Board gameBoard, int level) {
        return switch (level) {
            case 2 -> gameBoard.getResidentStack1();
            case 5 -> gameBoard.getResidentStack2();
            case 7 -> gameBoard.getResidentStack3();
            default -> throw new IllegalArgumentException("Invalid resident card level: " + level);
        };
    }
}
