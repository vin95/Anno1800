package com.anno1800.game.actions.demo;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.actions.SwapResidentCardsAction;
import com.anno1800.game.actions.actions.UpgradeResidentAction;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo class to test the new random shuffling, SwapResidentCards, and UpgradeResident functionality.
 */
public class NewActionsDemo {

    public static void main(String[] args) {
        // Create a game with 2 players to test the functionality
        Game game = new Game(2);
        Player player = game.getPlayers()[0];
        Board board = game.getBoard();
        PlayerBoard playerBoard = player.getPlayerBoard();

        System.out.println("=== Anno 1800 - New Actions Demo ===\n");

        // Demonstrate randomized stacks
        demonstrateRandomizedStacks(board);

        // Give the player some resident cards and residents for testing
        setupPlayerForTesting(playerBoard, board);

        // Demonstrate SwapResidentCards
        demonstrateSwapResidentCards(player, board);

        // Demonstrate UpgradeResident
        demonstrateUpgradeResident(player, board);

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demonstrateRandomizedStacks(Board board) {
        System.out.println("1. Random Shuffling Demo:");
        System.out.println("   Card stacks are now shuffled randomly during board initialization!");
        
        // Show the first few cards from each stack to demonstrate randomness
        System.out.println("   First ResidentCard from Stack1 (levels 1-2): " + 
            (board.getResidentStack1().isEmpty() ? "Empty" : 
             "Level " + board.getResidentStack1().peekFirst().populationLevel()));
        
        System.out.println("   First ResidentCard from Stack2 (levels 3-5): " + 
            (board.getResidentStack2().isEmpty() ? "Empty" : 
             "Level " + board.getResidentStack2().peekFirst().populationLevel()));
        
        System.out.println("   First ResidentCard from Stack3 (levels 6-7): " + 
            (board.getResidentStack3().isEmpty() ? "Empty" : 
             "Level " + board.getResidentStack3().peekFirst().populationLevel()));
        
        System.out.println("   ExpeditionCards stack size: " + board.getExpeditionStack().size());
        System.out.println("   OldWorldIslands stack size: " + board.getOldWorldIslands().size());
        System.out.println("   NewWorldIslands stack size: " + board.getNewWorldIslands().size());
        System.out.println();
    }

    private static void setupPlayerForTesting(PlayerBoard playerBoard, Board board) {
        System.out.println("2. Setting up player for testing...");
        
        // Give player some resident cards
        if (!board.getResidentStack1().isEmpty()) {
            ResidentCard card1 = board.drawResidentCard(1);
            playerBoard.getResidentCards().add(card1);
            System.out.println("   Added ResidentCard level " + card1.populationLevel() + " to player");
        }
        
        if (!board.getResidentStack2().isEmpty()) {
            ResidentCard card2 = board.drawResidentCard(3);
            playerBoard.getResidentCards().add(card2);
            System.out.println("   Added ResidentCard level " + card2.populationLevel() + " to player");
        }
        
        // Give player some residents to upgrade
        try {
            Resident farmer = board.takeResident(1);
            playerBoard.getResidents().add(farmer);
            System.out.println("   Added Farmer (level 1) to player");
            
            Resident worker = board.takeResident(2);
            playerBoard.getResidents().add(worker);
            System.out.println("   Added Worker (level 2) to player");
        } catch (Exception e) {
            System.out.println("   Could not add residents: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demonstrateSwapResidentCards(Player player, Board board) {
        System.out.println("3. SwapResidentCards Demo:");
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        if (playerBoard.getResidentCards().isEmpty()) {
            System.out.println("   No resident cards to swap");
            return;
        }
        
        System.out.println("   Player has " + playerBoard.getResidentCards().size() + " resident cards");
        
        // Demonstrate swapping one card
        List<ResidentCard> cardsToSwap = new ArrayList<>();
        ResidentCard firstCard = playerBoard.getResidentCards().get(0);
        cardsToSwap.add(firstCard);
        
        System.out.println("   Swapping ResidentCard level " + firstCard.populationLevel());
        
        Action.SwapResidentCards swapAction = new Action.SwapResidentCards(
            cardsToSwap.toArray(new ResidentCard[0])
        );
        
        SwapResidentCardsAction.swapResidentCards(player, board, swapAction);
        
        System.out.println("   After swap, player has " + playerBoard.getResidentCards().size() + " resident cards");
        if (!playerBoard.getResidentCards().isEmpty()) {
            ResidentCard newCard = playerBoard.getResidentCards().get(0);
            System.out.println("   New first card is level " + newCard.populationLevel());
        }
        System.out.println();
    }

    private static void demonstrateUpgradeResident(Player player, Board board) {
        System.out.println("4. UpgradeResident Demo:");
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        System.out.println("   Player has " + playerBoard.getResidents().size() + " residents");
        
        // Count residents by level
        int[] residentCounts = new int[6]; // Index 0 unused, 1-5 for levels
        for (Resident resident : playerBoard.getResidents()) {
            if (resident.getPopulationLevel() >= 1 && resident.getPopulationLevel() <= 5) {
                residentCounts[resident.getPopulationLevel()]++;
            }
        }
        
        for (int level = 1; level <= 5; level++) {
            if (residentCounts[level] > 0) {
                System.out.println("   Level " + level + ": " + residentCounts[level] + " residents");
            }
        }
        
        // Try to upgrade a farmer (level 1) to worker (level 2)
        if (residentCounts[1] > 0) {
            System.out.println("   Upgrading 1 Farmer (level 1) to Worker (level 2)");
            
            Action.UpgradeResident upgradeAction = new Action.UpgradeResident(
                new int[]{1},           // amount: upgrade 1 resident
                new int[]{1, 2}         // levels: from level 1 to level 2
            );
            
            try {
                UpgradeResidentAction.upgradeResident(player, board, upgradeAction);
                System.out.println("   Upgrade successful!");
                
                // Show new counts
                int[] newCounts = new int[6];
                for (Resident resident : playerBoard.getResidents()) {
                    if (resident.getPopulationLevel() >= 1 && resident.getPopulationLevel() <= 5) {
                        newCounts[resident.getPopulationLevel()]++;
                    }
                }
                
                for (int level = 1; level <= 5; level++) {
                    if (newCounts[level] != residentCounts[level]) {
                        System.out.println("   After upgrade - Level " + level + ": " + newCounts[level] + " residents");
                    }
                }
            } catch (Exception e) {
                System.out.println("   Upgrade failed: " + e.getMessage());
            }
        } else {
            System.out.println("   No Farmers available to upgrade");
        }
        System.out.println();
    }
}