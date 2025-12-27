package com.anno1800.engine;

import com.anno1800.board.Board;
import com.anno1800.player.Player;

/**
 * Example demonstrating how to use GameState.
 * This shows the complete workflow:
 * 1. Creating a game state snapshot
 * 2. Printing it
 * 3. Using it for game logic
 */
public class GameStateExample {
    
    public static void main(String[] args) {
        // 1. Setup: Create game with 2 players
        int numPlayers = 2;
        Board board = Board.initializeBoard(numPlayers);
        Player[] players = Player.initializePlayers(numPlayers);
        
        // 2. Create a snapshot of the current game state
        GameState state = GameState.createSnapshot(
            board,
            players,
            1,  // Round 1
            Phase.PRODUCTION,
            0   // Player 0's turn
        );
        
        // 3. Print the game state
        GameStatePrinter printer = new GameStatePrinter();
        
        System.out.println("=== DETAILED VIEW ===");
        printer.printDetailed(state);
        
        System.out.println("\n=== SUMMARY VIEW ===");
        printer.printSummary(state);
        
        System.out.println("\n=== JSON VIEW ===");
        printer.printJson(state);
        
        // 4. Access specific information from the state
        System.out.println("\n=== ACCESSING DATA ===");
        System.out.println("Current round: " + state.round());
        System.out.println("Current phase: " + state.currentPhase());
        System.out.println("Current player: " + state.players().get(state.currentPlayerIndex()).name());
        System.out.println("Available factories: " + state.boardState().availableFactories());
        System.out.println("Gold in pool: " + state.boardState().gold());
        
        // 5. Iterate through all players
        System.out.println("\n=== PLAYER OVERVIEW ===");
        for (int i = 0; i < state.players().size(); i++) {
            var player = state.players().get(i);
            System.out.printf("Player %d (%s): %d factories, %d residents%n",
                i + 1,
                player.name(),
                player.factoryCount(),
                player.residentCount()
            );
        }
    }
}
