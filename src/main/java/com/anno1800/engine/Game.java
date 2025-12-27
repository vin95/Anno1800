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
    private final int startPlayer;
    private int currentPlayer;
    
    public Game(int numPlayers) {
        this.board = Board.initializeBoard(numPlayers);
        this.players = Player.initializePlayers(numPlayers, this.board);
        this.actionHandler = new ActionHandler(this);
        
        // Initialize game state
        this.currentRound = 1;
        this.startPlayer = (int) (Math.random() * numPlayers);
        for (int i = 0; i < players.length; i++) {
            int position = ((i - startPlayer + numPlayers) % numPlayers) + 1;
            players[i].setPosition(position);
        }
        this.currentPlayer = this.startPlayer;
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
        currentPlayer = 0;  // Reset to first player
        
        System.out.println("=== Round " + currentRound + " begins ===");
    }
    
    /**
     * Advance to the next player.
     * If it was the last player, advance to the next round.
     */
    public void nextPlayer() {
        currentPlayer++;
        
        if (currentPlayer >= players.length) {
            // All players have finished - start new round
            nextRound();
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
     * Get the index of the current player.
     * 
     * @return Current player index (0-based)
     */
    public int getCurrentPlayerIndex() {
        return currentPlayer;
    }
    
    /**
     * Get the current player.
     * 
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return players[currentPlayer];
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
            currentPlayer
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
        inicializeGame();
        System.out.println("=== Game Start ===");
        System.out.println("Players: " + players.length);
        System.out.println("Starting Round: " + currentRound);
        
        // TODO: Implement game loop
        // while (!isGameOver()) {
        //     playTurn();
        // }
    }

    private void inicializeGame() {
        for (Player player : players) {
            player.getPlayerBoard().initializePlayerBoard(player);
        }
    }
    
    public Board getBoard() {
        return board;
    }

    public int getStartPlayer() {
        return startPlayer;
    }
    
    public Player[] getPlayers() {
        return players;
    }
}
