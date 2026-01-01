package com.anno1800.game.actions.actions;

import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.NewWorldIsland;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Discover a New World Island.
 */
public class DiscoverNewWorldIsland {
    public static void discoverNewWorldIsland(Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();

        // Calculate explorer chip cost (number of already discovered NewWorldIslands + 1)
        int explorerChipCost = playerBoard.getNumNewWorldIslands() + 1;

        // Reduce player's available explorer chips
        playerBoard.reduceAvailableExplorerChips(explorerChipCost);

        // Get the New World Islands stack
        Deque<NewWorldIsland> newWorldIslandsStack = board.getNewWorldIslands();

        // Draw the 2 top islands for selection
        List<NewWorldIsland> islandChoices = new ArrayList<>();
        if (!newWorldIslandsStack.isEmpty()) {
            islandChoices.add(newWorldIslandsStack.pollFirst());
        }
        if (!newWorldIslandsStack.isEmpty()) {
            islandChoices.add(newWorldIslandsStack.pollFirst());
        }

        // For now, we'll implement a simple selection strategy:
        // Choose the island with the most valuable plantations
        // In a real implementation, this would be an interactive choice by the player/agent
        NewWorldIsland chosenIsland = selectBestNewWorldIsland(islandChoices);
        NewWorldIsland rejectedIsland = null;

        // Find the rejected island
        for (NewWorldIsland island : islandChoices) {
            if (island != chosenIsland) {
                rejectedIsland = island;
                break;
            }
        }

        // Put the rejected island back at the bottom of the stack
        if (rejectedIsland != null) {
            newWorldIslandsStack.addLast(rejectedIsland);
        }

        // Add the chosen island to the player board
        if (chosenIsland != null) {
            playerBoard.addNewWorldIsland(chosenIsland);

            // Draw 3 cards from ResidentStack3 (level 7 residents)
            drawResidentCardsFromStack3(player, board);
        }
    }

    /**
     * Selects the best New World Island from available choices.
     * This is a simplified selection strategy - in a real game, this would be an interactive choice.
     * 
     * @param islandChoices List of available islands to choose from
     * @return The selected island, or null if no islands available
     */
    private static NewWorldIsland selectBestNewWorldIsland(List<NewWorldIsland> islandChoices) {
        if (islandChoices.isEmpty()) {
            return null;
        }

        // Simple strategy: Choose the island with the most plantations
        NewWorldIsland bestIsland = islandChoices.get(0);
        int bestPlantationCount = bestIsland.getPlantations().length;

        for (NewWorldIsland island : islandChoices) {
            int plantationCount = island.getPlantations().length;
            if (plantationCount > bestPlantationCount) {
                bestIsland = island;
                bestPlantationCount = plantationCount;
            }
        }

        return bestIsland;
    }

    /**
     * Draws 3 cards from ResidentStack3 (level 7 residents) and adds them to the player's hand.
     */
    private static void drawResidentCardsFromStack3(Player player, Board board) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        for (int i = 0; i < 3; i++) {
            try {
                ResidentCard card = board.drawResidentCard(7); // Level 7 corresponds to ResidentStack3
                playerBoard.getResidentCards().add(card);
            } catch (IllegalStateException e) {
                // Stack is empty, no more cards to draw
                System.out.println("ResidentStack3 is empty, cannot draw more cards");
                break;
            }
        }
    }
}
