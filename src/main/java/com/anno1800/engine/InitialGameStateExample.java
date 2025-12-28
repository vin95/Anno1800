package com.anno1800.engine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Example showing how to capture and save the initial game state after initialization.
 * Demonstrates different output formats and file saving.
 */
public class InitialGameStateExample {
    
    private static final String OUTPUT_DIR = "game-states";
    
    public static void main(String[] args) {
        System.out.println("=== Initial Game State Capture ===\n");
        
        // 1. Initialize game
        System.out.println("Initializing game with 2 players...");
        Game game = new Game(2);
        System.out.println("✓ Game initialized\n");
        
        // 2. Capture initial game state
        GameState initialState = game.getState();
        System.out.println("✓ Game state captured\n");
        
        // 3. Print to console - different formats
        GameStatePrinter printer = new GameStatePrinter();
        
        System.out.println("=== DETAILED VIEW ===");
        printer.printDetailed(initialState);
        
        System.out.println("\n=== SUMMARY VIEW ===");
        printer.printSummary(initialState);
        
        System.out.println("\n=== JSON VIEW ===");
        printer.printJson(initialState);
        
        // 4. Save to files
        try {
            saveGameState(initialState, printer);
        } catch (IOException e) {
            System.err.println("Error saving game state: " + e.getMessage());
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    /**
     * Save game state to files in different formats.
     */
    private static void saveGameState(GameState state, GameStatePrinter printer) throws IOException {
        // Create output directory if it doesn't exist
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            System.out.println("\n✓ Created directory: " + OUTPUT_DIR);
        }
        
        // Generate timestamp for filenames
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        // Save detailed format
        String detailedFile = String.format("%s/gamestate_detailed_%s.txt", OUTPUT_DIR, timestamp);
        saveDetailedState(state, printer, detailedFile);
        System.out.println("✓ Saved detailed state: " + detailedFile);
        
        // Save summary format
        String summaryFile = String.format("%s/gamestate_summary_%s.txt", OUTPUT_DIR, timestamp);
        saveSummaryState(state, printer, summaryFile);
        System.out.println("✓ Saved summary state: " + summaryFile);
        
        // Save JSON format
        String jsonFile = String.format("%s/gamestate_%s.json", OUTPUT_DIR, timestamp);
        saveJsonState(state, printer, jsonFile);
        System.out.println("✓ Saved JSON state: " + jsonFile);
    }
    
    /**
     * Save detailed game state to file.
     */
    private static void saveDetailedState(GameState state, GameStatePrinter printer, String filename) 
            throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Redirect System.out temporarily
            PrintWriter originalOut = new PrintWriter(System.out);
            System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    writer.write(b);
                }
            }));
            
            printer.printDetailed(state);
            
            // Restore System.out
            System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out)));
            writer.flush();
        }
    }
    
    /**
     * Save summary game state to file.
     */
    private static void saveSummaryState(GameState state, GameStatePrinter printer, String filename) 
            throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.printf("ANNO 1800 - Game State Summary%n");
            writer.printf("Generated: %s%n%n", state.timestamp());
            writer.printf("Round: %d%n", state.round());
            writer.printf("Current Player: %s%n", 
                state.players().get(state.currentPlayerIndex()).name());
            writer.printf("%nPlayers: %d%n", state.players().size());
            
            for (int i = 0; i < state.players().size(); i++) {
                var player = state.players().get(i);
                writer.printf("  %d. %s - Factories: %d, Residents: %d%n",
                    i + 1,
                    player.name(),
                    player.factoryCount(),
                    player.residentCount());
            }
        }
    }
    
    /**
     * Save JSON game state to file.
     */
    private static void saveJsonState(GameState state, GameStatePrinter printer, String filename) 
            throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("{");
            writer.printf("  \"timestamp\": \"%s\",%n", state.timestamp());
            writer.printf("  \"round\": %d,%n", state.round());
            writer.printf("  \"currentPlayerIndex\": %d,%n", state.currentPlayerIndex());
            writer.printf("  \"boardState\": {%n");
            writer.printf("    \"availableFactories\": %d,%n", state.boardState().availableFactories());
            writer.printf("    \"availableResidentCards\": %d,%n", state.boardState().availableResidentCards());
            writer.printf("    \"farmers\": %d,%n", state.boardState().farmers());
            writer.printf("    \"workers\": %d,%n", state.boardState().workers());
            writer.printf("    \"artisans\": %d,%n", state.boardState().artisans());
            writer.printf("    \"engineers\": %d,%n", state.boardState().engineers());
            writer.printf("    \"investors\": %d,%n", state.boardState().investors());
            writer.printf("    \"gold\": %d%n", state.boardState().gold());
            writer.printf("  },%n");
            writer.printf("  \"players\": [%n");
            
            for (int i = 0; i < state.players().size(); i++) {
                var player = state.players().get(i);
                writer.printf("    {%n");
                writer.printf("      \"name\": \"%s\",%n", player.name());
                writer.printf("      \"freeLandTiles\": %d,%n", player.freeLandTiles());
                writer.printf("      \"freeCoastTiles\": %d,%n", player.freeCoastTiles());
                writer.printf("      \"freeSeaTiles\": %d,%n", player.freeSeaTiles());
                writer.printf("      \"factoryCount\": %d,%n", player.factoryCount());
                writer.printf("      \"residentCount\": %d,%n", player.residentCount());
                writer.printf("      \"shipyardCount\": %d,%n", player.shipyardCount());
                writer.printf("      \"tradeShipCount\": %d,%n", player.tradeShipCount());
                writer.printf("      \"explorerShipCount\": %d%n", player.explorerShipCount());
                writer.printf("    }");
                if (i < state.players().size() - 1) {
                    writer.printf(",%n");
                } else {
                    writer.printf("%n");
                }
            }
            
            writer.printf("  ]%n");
            writer.println("}");
        }
    }
}
