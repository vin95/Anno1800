package com.anno1800.ui.output;

import com.anno1800.game.state.GameState;
import com.anno1800.game.state.GameState.BoardState;
import com.anno1800.game.state.GameState.PlayerState;
import com.anno1800.game.state.GameState.ResidentSummary;

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
            System.out.printf("      \"factories\": %d,%n", player.buildings().factoryCount());
            System.out.printf("      \"residents\": %d%n", player.residents().count());
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
        System.out.println("+- Shared Board ----------------------------------------------------------------------+");
        System.out.println("| Factories & Buildings:                 | Ships:                                     |");
        System.out.printf("|   Available Factories:    %3d          |   Trade Ship Level 1:     %3d              |%n", 
            board.factories().availableFactories(), board.ships().tradeShips().level1());
        System.out.printf("|   Shipyard Level 1:       %3d          |   Trade Ship Level 2:     %3d              |%n", 
            board.shipyards().level1(), board.ships().tradeShips().level2());
        System.out.printf("|   Shipyard Level 2:       %3d          |   Trade Ship Level 3:     %3d              |%n", 
            board.shipyards().level2(), board.ships().tradeShips().level3());
        System.out.printf("|   Shipyard Level 3:       %3d          |   Explorer Ship Level 1:  %3d              |%n", 
            board.shipyards().level3(), board.ships().explorerShips().level1());
        System.out.printf("|                                        |   Explorer Ship Level 2:  %3d              |%n", 
            board.ships().explorerShips().level2());
        System.out.printf("|                                        |   Explorer Ship Level 3:  %3d              |%n", 
            board.ships().explorerShips().level3());
        System.out.println("+----------------------------------------+--------------------------------------------+");
        System.out.println("| Cards:                                 | Islands:                                   |");
        System.out.printf("|   Resident Stack 1:       %3d          |   Old World Islands:      %3d              |%n", 
            board.residentCards().stack1Size(), board.islands().oldWorldSize());
        System.out.printf("|   Resident Stack 2:       %3d          |   New World Islands:      %3d              |%n", 
            board.residentCards().stack2Size(), board.islands().newWorldSize());
        System.out.printf("|   Resident Stack 3:       %3d          |                                            |%n", 
            board.residentCards().stack3Size());
        System.out.printf("|   Total Resident Cards:   %3d          |                                            |%n", 
            board.residentCards().totalAvailable());
        System.out.printf("|   Expedition Cards:       %3d          |                                            |%n", 
            board.expeditions().stackSize());
        System.out.println("+----------------------------------------+--------------------------------------------+");
        System.out.println("| Population Pool:                       | Resources:                                 |");
        System.out.printf("|   Farmers:                %3d          |   Gold Pool:              %3d              |%n", 
            board.resources().farmers(), board.resources().gold());
        System.out.printf("|   Workers:                %3d          |   Trade Chips:            %3d              |%n", 
            board.resources().workers(), board.resources().tradeChips());
        System.out.printf("|   Artisans:               %3d          |   Explorer Chips:         %3d              |%n", 
            board.resources().artisans(), board.resources().explorerChips());
        System.out.printf("|   Engineers:              %3d          |                                            |%n", 
            board.resources().engineers());
        System.out.printf("|   Investors:              %3d          |                                            |%n", 
            board.resources().investors());
        System.out.println("+-------------------------------------------------------------------------------------+");
    }
    
    private void printPlayers(GameState state) {
        for (int i = 0; i < state.players().size(); i++) {
            PlayerState player = state.players().get(i);
            boolean isCurrentPlayer = i == state.currentPlayerIndex();
            
            String marker = isCurrentPlayer ? "> " : "  ";
            System.out.printf("%s+- Player %d: %-57s -+%n", marker, i + 1, player.name());
            System.out.printf("%s| Position: %2d                                                          |%n",
                marker, player.position());
            System.out.printf("%s| Free Tiles:      Land: %2d | Coast: %2d | Sea: %2d                       |%n",
                marker, player.tiles().landtiles().size(), player.tiles().coasttiles().size(), player.tiles().seatiles().size());
            System.out.printf("%s| Buildings:  Factories: %2d | Shipyards: %2d                             |%n",
                marker, player.buildings().factoryCount(), player.buildings().shipyardCount());
            System.out.printf("%s| Ships:      Trade: %2d | Explorer: %2d                                  |%n",
                marker, player.ships().tradeShips().totalCount(), player.ships().explorerShips().totalCount());
            System.out.printf("%s|   Trade Ships:   L1: %2d | L2: %2d | L3: %2d                             |%n",
                marker, player.ships().tradeShips().levels().level1(), player.ships().tradeShips().levels().level2(), player.ships().tradeShips().levels().level3());
            System.out.printf("%s|   Explorer Ships: L1: %2d | L2: %2d | L3: %2d                            |%n",
                marker, player.ships().explorerShips().levels().level1(), player.ships().explorerShips().levels().level2(), player.ships().explorerShips().levels().level3());
                System.out.printf("%s| Trade Chips:    %2d | Explorer Chips: %2d                               |%n",
                marker, player.resources().availableTradeChips(), player.resources().availableExplorerChips());
                System.out.printf("%s| Resources:      Gold: %2d                                              |%n",
                marker, player.resources().gold());
            System.out.printf("%s| Cards:   Resident Cards: %2d                                           |%n",
                marker, player.cards().residentCardCount());
            System.out.printf("%s| Residents:  Total: %2d                                                 |%n",
                marker, player.residents().count());
            
            // Print resident details
            if (!player.residents().residents().isEmpty()) {
                System.out.printf("%s|   Level 1: %2d | Level 2: %2d | Level 3: %2d | Level 4: %2d | Level 5: %2d |%n",
                    marker,
                    countResidentsByLevel(player.residents().residents(), 1),
                    countResidentsByLevel(player.residents().residents(), 2),
                    countResidentsByLevel(player.residents().residents(), 3),
                    countResidentsByLevel(player.residents().residents(), 4),
                    countResidentsByLevel(player.residents().residents(), 5)
                );
                System.out.printf("%s|   Working: %2d | Fit: %2d | Exhausted: %2d                               |%n",
                    marker,
                    countResidentsByStatus(player.residents().residents(), "AT_WORK"),
                    countResidentsByStatus(player.residents().residents(), "FIT"),
                    countResidentsByStatus(player.residents().residents(), "EXHAUSTED")
                );
            }
            
            System.out.printf("%s+-----------------------------------------------------------------------+%n", marker);
            
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
