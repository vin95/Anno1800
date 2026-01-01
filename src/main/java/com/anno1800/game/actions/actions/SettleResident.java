package com.anno1800.game.actions.actions;

import com.anno1800.game.board.Board;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import static com.anno1800.game.residents.ResidentStatus.FIT;

/**
 * Settle a new resident action.
 * Takes a resident from the GameBoard and adds them to the PlayerBoard with FIT status.
 */
public class SettleResident {

    /**
     * Settles a resident of the specified level.
     * Takes the resident from GameBoard and adds them to PlayerBoard with FIT status.
     * 
     * @param player The player settling the resident
     * @param game The current game state
     * @param action The settle resident action
     */
    public static void settleResident(Player player, Game game, com.anno1800.game.actions.Action.SettleResident action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();
        int level = action.level();
        
        // Check if there are residents of the requested level available on the board
        if (!hasResidentAvailable(board, level)) {
            throw new IllegalStateException("No residents of level " + level + " available on the board");
        }
        
        // Take a resident from the GameBoard
        Resident resident = board.takeResident(level);
        
        // Set status to FIT
        resident.setStatus(FIT);
        
        // Add to PlayerBoard
        playerBoard.getResidents().add(resident);
        
        System.out.println("Settled resident of level " + level + " with FIT status");
    }

    /**
     * Checks if a resident of the specified level is available on the board.
     * 
     * @param board The game board
     * @param level The population level to check
     * @return true if a resident is available
     */
    private static boolean hasResidentAvailable(Board board, int level) {
        try {
            // Try to check if the board has residents of this level
            switch (level) {
                case 1 -> { return board.getFarmers() > 0; }
                case 2 -> { return board.getWorkers() > 0; }
                case 3 -> { return board.getArtisans() > 0; }
                case 4 -> { return board.getEngineers() > 0; }
                case 5 -> { return board.getInvestors() > 0; }
                default -> { return false; }
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a player can settle a resident of the specified level.
     * 
     * @param player The player to check
     * @param game The current game state
     * @param level The population level to settle
     * @return true if settlement is possible
     */
    public static boolean canSettleResident(Player player, Game game, int level) {
        Board board = game.getBoard();
        
        // Check if level is valid
        if (level < 1 || level > 5) {
            return false;
        }
        
        // Check if there are residents available on the board
        return hasResidentAvailable(board, level);
    }

    // Legacy method for backward compatibility
    public static void settleResident(Player player, int level) {
        throw new UnsupportedOperationException("Use settleResident(Player, Game, SettleResident) instead");
    }
}
