package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

import java.util.Deque;

/**
 * Handler for the DiscardResidentCardAction free action.
 * 
 * Rule from ObjectiveCard: "nutze 2 Explorerchips um 1 ResidentCard 
 * auf das GameBoard zurückzulegen (unter den Stapel) 1x Pro Zug (freie Aktion)"
 * 
 * This is a free action that:
 * - Deducts 2 Explorer Chips from the player
 * - Removes the specified ResidentCard from the player's hand
 * - Places the card at the bottom of the appropriate resident stack
 * - Marks the action as used for this turn
 */
public class DiscardResidentCardAction {

    /**
     * Executes the DiscardResidentCardAction.
     * 
     * @param player The player performing the action
     * @param game The current game state
     * @param action The DiscardResidentCardAction action
     */
    public static void discardResidentCard(Player player, Game game, Action.DiscardResidentCardAction action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();
        ResidentCard card = action.card();
        
        // Deduct 2 Explorer Chips
        playerBoard.reduceAvailableExplorerChips(2);
        
        // Remove card from player's hand
        playerBoard.getResidentCards().remove(card);
        
        // Place card at the bottom of the appropriate stack based on population level
        Deque<ResidentCard> targetStack = getStackForCard(board, card.populationLevel());
        if (targetStack != null) {
            targetStack.addLast(card);  // addLast = bottom of stack
        }
        
        // Mark as used this turn
        playerBoard.markDiscardResidentCardUsed();
        
        System.out.println(player.getName() + " used Discard Resident Card: Paid 2 Explorer Chips to return a Level " 
            + card.populationLevel() + " card to the bottom of the deck!");
    }
    
    /**
     * Gets the appropriate resident stack for a card based on its population level.
     */
    private static Deque<ResidentCard> getStackForCard(Board board, int populationLevel) {
        if (populationLevel <= 2) {
            return board.getResidentStack1();
        } else if (populationLevel <= 5) {
            return board.getResidentStack2();
        } else {
            return board.getResidentStack3();
        }
    }
}
