package com.anno1800.game.actions.actions;

import com.anno1800.game.board.Board;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.OldWorldIsland;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Discover a Old World Island.
 */
public class DiscoverOldWorldIsland {
    public static void discoverOldWorldIsland(Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();

        // Calculate explorer chip cost (number of already discovered OldWorldIslands + 1)
        int explorerChipCost = playerBoard.getNumOldWorldIslands() + 1;

        // Reduce player's available explorer chips
        playerBoard.reduceAvailableExplorerChips(explorerChipCost);

        // Get the Old World Islands stack
        Deque<OldWorldIsland> oldWorldIslandsStack = board.getOldWorldIslands();

        // Draw the 2 top islands for selection
        List<OldWorldIsland> islandChoices = new ArrayList<>();
        if (!oldWorldIslandsStack.isEmpty()) {
            islandChoices.add(oldWorldIslandsStack.pollFirst());
        }
        if (!oldWorldIslandsStack.isEmpty()) {
            islandChoices.add(oldWorldIslandsStack.pollFirst());
        }

        // For now, we'll implement a simple selection strategy:
        // Choose the island with the best reward or most valuable attributes
        // In a real implementation, this would be an interactive choice by the player/agent
        OldWorldIsland chosenIsland = selectBestOldWorldIsland(islandChoices);
        OldWorldIsland rejectedIsland = null;

        // Find the rejected island
        for (OldWorldIsland island : islandChoices) {
            if (island != chosenIsland) {
                rejectedIsland = island;
                break;
            }
        }

        // Put the rejected island back at the bottom of the stack
        if (rejectedIsland != null) {
            oldWorldIslandsStack.addLast(rejectedIsland);
        }

        // Add the chosen island to the player board
        if (chosenIsland != null) {
            playerBoard.addOldWorldIsland(chosenIsland);

            // Activate the reward of the chosen island
            ActivateReward.activateReward(player, chosenIsland.getReward(), game);
        }
    }

    /**
     * Selects the best Old World Island from available choices.
     * This is a simplified selection strategy - in a real game, this would be an interactive choice.
     * 
     * @param islandChoices List of available islands to choose from
     * @return The selected island, or null if no islands available
     */
    private static OldWorldIsland selectBestOldWorldIsland(List<OldWorldIsland> islandChoices) {
        if (islandChoices.isEmpty()) {
            return null;
        }

        // Simple strategy: Choose the island with the most total tiles
        OldWorldIsland bestIsland = islandChoices.get(0);
        int bestTotalTiles = getTotalTiles(bestIsland);

        for (OldWorldIsland island : islandChoices) {
            int totalTiles = getTotalTiles(island);
            if (totalTiles > bestTotalTiles) {
                bestIsland = island;
                bestTotalTiles = totalTiles;
            }
        }

        return bestIsland;
    }

    /**
     * Helper method to calculate total tiles for an Old World Island.
     */
    private static int getTotalTiles(OldWorldIsland island) {
        return island.getFreeLandTiles() + island.getFreeCoastTiles() + island.getFreeSeaTiles();
    }
}
