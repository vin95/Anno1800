package com.anno1800.engine;

import com.anno1800.actions.Action;
import com.anno1800.actions.ActionHandler;
import com.anno1800.board.Board;
import com.anno1800.player.Player;

/**
 * Main game controller that manages the board and game state
 */
public class Game {
    private final Board board;
    private Player[] players;
    private final ActionHandler actionHandler;
    
    // Game state tracking
    private int currentRound;
    private Phase currentPhase;
    private int currentPlayerIndex;
    
    public Game(int numPlayers) {
        this.board = Board.initializeBoard(numPlayers);
        this.players = Player.initializePlayers(numPlayers, this.board);
        this.actionHandler = new ActionHandler(this);
        
        // Initialize game state
        this.currentRound = 1;
        this.currentPhase = Phase.PRODUCTION;
        this.currentPlayerIndex = 0;
    }
    
    /**
     * Execute a player action.
     * 
     * @param action The action to execute
     * @return true if action was successful
     */
    public boolean executeAction(Action action) {
        return actionHandler.execute(action, getCurrentPlayer());
    }
    
    /**
     * Advance to the next round.
     * Called when all players have completed their turns.
     */
    public void nextRound() {
        currentRound++;
        currentPlayerIndex = 0;  // Reset to first player
        currentPhase = Phase.PRODUCTION;  // Reset to first phase
        
        System.out.println("=== Round " + currentRound + " begins ===");
    }
    
    /**
     * Advance to the next phase for the current player.
     */
    public void nextPhase() {
        Phase[] phases = Phase.values();
        int currentPhaseIndex = currentPhase.ordinal();
        
        if (currentPhaseIndex < phases.length - 1) {
            // Move to next phase
            currentPhase = phases[currentPhaseIndex + 1];
        } else {
            // End of turn - move to next player
            nextPlayer();
        }
    }
    
    /**
     * Advance to the next player.
     * If it was the last player, advance to the next round.
     */
    private void nextPlayer() {
        currentPlayerIndex++;
        
        if (currentPlayerIndex >= players.length) {
            // All players have finished - start new round
            nextRound();
        } else {
            // Next player starts with first phase
            currentPhase = Phase.PRODUCTION;
        }
    }
    
    /**
     * Get the current round number.
     * 
     * @return Current round (starts at 1)
     */
    public int getCurrentRound() {
        return currentRound;
    }
    
    /**
     * Get the current game phase.
     * 
     * @return Current phase
     */
    public Phase getCurrentPhase() {
        return currentPhase;
    }
    
    /**
     * Get the index of the current player.
     * 
     * @return Current player index (0-based)
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    /**
     * Get the current player.
     * 
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }
    
    /**
     * Create a snapshot of the current game state.
     * 
     * @return Immutable GameState snapshot
     */
    public GameState getState() {
        return GameState.createSnapshot(
            board,
            players,
            currentRound,
            currentPhase,
            currentPlayerIndex
        );
    }
    
    /**
     * Check if the game has ended.
     * Override this method with your game's end condition.
     * 
     * @return true if game is over
     */
    public boolean isGameOver() {
        // TODO: Implement game end condition (e.g., max rounds, victory points)
        return false;
    }

    public void start() {
        System.out.println("=== Game Start ===");
        System.out.println("Players: " + players.length);
        System.out.println("Starting Round: " + currentRound);
        
        // TODO: Implement game loop
        // while (!isGameOver()) {
        //     playTurn();
        // }
    }
    
    public Board getBoard() {
        return board;
    }
    
    public Player[] getPlayers() {
        return players;
    }
}
