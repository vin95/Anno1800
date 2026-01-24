package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

import java.util.List;

/**
 * View all ResidentCards currently held by the player.
 * This is a free action that does not consume an action point.
 */
public class ViewResidentCards {

    /**
     * Displays all ResidentCards in the player's hand.
     * This action does not modify the game state.
     * 
     * @param player The player viewing their cards
     * @param game The current game state
     * @param action The ViewResidentCards action
     */
    public static void viewResidentCards(Player player, Game game, Action.ViewResidentCards action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        List<ResidentCard> residentCards = playerBoard.getResidentCards();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RESIDENT CARDS - " + player.getName());
        System.out.println("=".repeat(80));
        
        if (residentCards.isEmpty()) {
            System.out.println("No ResidentCards in hand.");
        } else {
            System.out.println("Total Cards: " + residentCards.size());
            System.out.println("-".repeat(80));
            
            for (int i = 0; i < residentCards.size(); i++) {
                ResidentCard card = residentCards.get(i);
                System.out.printf("\nCard %d:\n", i + 1);
                System.out.printf("  Population Level: %d (%s)\n", 
                    card.populationLevel(), 
                    getLevelName(card.populationLevel()));
                
                // Display needs
                System.out.print("  Needs: ");
                if (card.needs().length == 0) {
                    System.out.println("None");
                } else {
                    for (int j = 0; j < card.needs().length; j++) {
                        System.out.print(card.needs()[j]);
                        if (j < card.needs().length - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println();
                }
                
                // Display reward
                System.out.println("  Reward: " + card.reward());
            }
        }
        
        System.out.println("=".repeat(80));
        System.out.println("(This is a free action - no action consumed)");
        System.out.println("=".repeat(80) + "\n");
    }
    
    /**
     * Helper method to get the name of a population level.
     */
    private static String getLevelName(int level) {
        return switch (level) {
            case 1 -> "Farmer";
            case 2 -> "Worker";
            case 3 -> "Artisan";
            case 4 -> "Engineer";
            case 5 -> "Investor";
            case 6 -> "Jornalero";
            case 7 -> "Obrero";
            default -> "Unknown";
        };
    }
}
