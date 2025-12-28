package com.anno1800.engine;

import com.anno1800.engine.GameState.BoardState;
import com.anno1800.engine.GameState.PlayerState;
import com.anno1800.engine.GameState.ResidentSummary;

/**
 * Formats and prints game state information in a human-readable format.
 * Provides different output formats for various use cases.
 */
public class GameStatePrinter {
    
    /**
     * Print a complete, detailed game state to the console.
     * 
     * @param state The game state to print
     */
    public void printDetailed(GameState state) {
        System.out.println("");
        System.out.println("------------------------------------------ANNO 1800 - GAME STATE-------------------------------------------");
        System.out.println("");
        System.out.println();
        
        printGameInfo(state);
        System.out.println();
        
        printBoardState(state.boardState());
        System.out.println();
        
        printPlayers(state);
        
        System.out.println("------------------------------------------------END OF STATE------------------------------------------------");
    }
    
    /**
     * Print a compact summary of the game state.
     * 
     * @param state The game state to print
     */
    public void printSummary(GameState state) {
        System.out.printf("Round %d | Current Player: %s%n",
            state.round(),
            state.players().get(state.currentPlayerIndex()).name()
        );
    }
    
    /**
     * Print the game state as a formatted JSON-like structure.
     * 
     * @param state The game state to print
     */
    public void printJson(GameState state) {
        System.out.println("{");
        System.out.printf("  \"timestamp\": \"%s\",%n", state.timestamp());
        System.out.printf("  \"round\": %d,%n", state.round());
        System.out.printf("  \"currentPlayer\": %d,%n", state.currentPlayerIndex());
        System.out.println("  \"players\": [");
        
        for (int i = 0; i < state.players().size(); i++) {
            PlayerState player = state.players().get(i);
            System.out.println("    {");
            System.out.printf("      \"name\": \"%s\",%n", player.name());
            System.out.printf("      \"factories\": %d,%n", player.factoryCount());
            System.out.printf("      \"residents\": %d%n", player.residentCount());
            System.out.print("    }");
            if (i < state.players().size() - 1) {
                System.out.println(",");
            } else {
                System.out.println();
            }
        }
        
        System.out.println("  ]");
        System.out.println("}");
    }
    
    private void printGameInfo(GameState state) {
        System.out.println("+- Game Information ----------------------------------------+");
        System.out.printf("| Timestamp:       %s%n", state.timestamp());
        System.out.printf("| Round:           %d%n", state.round());
        System.out.printf("| Current Player:  %s (Player %d)%n",
            state.players().get(state.currentPlayerIndex()).name(),
            state.currentPlayerIndex() + 1
        );
        System.out.println("+-------------------------------------------------------------+");
    }
    
    private void printBoardState(BoardState board) {
        System.out.println("+- Shared Board ----------------------------------------------+");
        System.out.printf("| Available Factories:      %3d%n", board.availableFactories());
        System.out.printf("| Available Resident Cards: %3d%n", board.availableResidentCards());
        System.out.println("|");
        System.out.println("| Population Pool:");
        System.out.printf("|   Farmers:     %3d%n", board.farmers());
        System.out.printf("|   Workers:     %3d%n", board.workers());
        System.out.printf("|   Artisans:    %3d%n", board.artisans());
        System.out.printf("|   Engineers:   %3d%n", board.engineers());
        System.out.printf("|   Investors:   %3d%n", board.investors());
        System.out.println("|");
        System.out.printf("| Gold Pool:     %3d%n", board.gold());
        System.out.println("+-------------------------------------------------------------+");
    }
    
    private void printPlayers(GameState state) {
        for (int i = 0; i < state.players().size(); i++) {
            PlayerState player = state.players().get(i);
            boolean isCurrentPlayer = i == state.currentPlayerIndex();
            
            String marker = isCurrentPlayer ? "> " : "  ";
            System.out.printf("%s+- Player %d: %-45s -+%n", marker, i + 1, player.name());
            System.out.printf("%s| Free Tiles:      Land: %2d | Coast: %2d | Sea: %2d                 |%n",
                marker, player.freeLandTiles(), player.freeCoastTiles(), player.freeSeaTiles());
            System.out.printf("%s| Buildings:  Factories: %2d | Shipyards: %2d                  |%n",
                marker, player.factoryCount(), player.shipyardCount());
            System.out.printf("%s| Ships:      Trade: %2d | Explorer: %2d                       |%n",
                marker, player.tradeShipCount(), player.explorerShipCount());
            System.out.printf("%s| Residents:  Total: %2d                                      |%n",
                marker, player.residentCount());
            
            // Print resident details
            if (!player.residents().isEmpty()) {
                System.out.printf("%s|   Level 1: %2d | Level 2: %2d | Level 3: %2d                |%n",
                    marker,
                    countResidentsByLevel(player.residents(), 1),
                    countResidentsByLevel(player.residents(), 2),
                    countResidentsByLevel(player.residents(), 3)
                );
                System.out.printf("%s|   Working: %2d | Ready: %2d | Exhausted: %2d                |%n",
                    marker,
                    countResidentsByStatus(player.residents(), "AT_WORK"),
                    countResidentsByStatus(player.residents(), "FIT"),
                    countResidentsByStatus(player.residents(), "EXHAUSTED")
                );
            }
            
            System.out.printf("%s+------------------------------------------------------------+%n", marker);
            
            if (i < state.players().size() - 1) {
                System.out.println();
            }
        }
    }
    
    private int countResidentsByLevel(java.util.List<ResidentSummary> residents, int level) {
        return (int) residents.stream()
            .filter(r -> r.level() == level)
            .count();
    }
    
    private int countResidentsByStatus(java.util.List<ResidentSummary> residents, String status) {
        return (int) residents.stream()
            .filter(r -> r.status().equals(status))
            .count();
    }
}
