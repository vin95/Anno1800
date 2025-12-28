package com.anno1800.engine;

import java.time.Instant;
import java.util.List;

import com.anno1800.board.Board;
import com.anno1800.player.Player;

/**
 * Immutable snapshot of the complete game state at a specific point in time.
 * Used for saving/loading games, debugging, and AI agent decision-making.
 * 
 * Design principles:
 * - Immutable (all fields final, uses records)
 * - Self-contained (can be serialized/deserialized)
 * - No game logic (pure data structure)
 */
public record GameState(
    // Game metadata
    Instant timestamp,
    int round,
    int currentPlayerIndex,
    
    // Shared game board state
    BoardState boardState,
    
    // All players
    List<PlayerState> players
) {
    
    /**
     * Creates a GameState snapshot from the current game.
     * 
     * @param board The shared game board
     * @param players Array of all players
     * @param round Current round number
     * @param currentPlayerIndex Index of the current player
     * @return Immutable GameState snapshot
     */
    public static GameState createSnapshot(
        Board board, 
        Player[] players, 
        int round, 
        int currentPlayerIndex
    ) {
        return new GameState(
            Instant.now(),
            round,
            currentPlayerIndex,
            BoardState.fromBoard(board),
            PlayerState.fromPlayers(players)
        );
    }
    
    /**
     * Represents the shared board state.
     * Contains all stacks and pools available to all players.
     */
    public record BoardState(
        // Factory stacks - count available factories per stack
        int availableFactories,
        
        // Resident card stacks
        int residentStack1Size,
        int residentStack2Size,
        int residentStack3Size,
        
        int availableResidentCards,
        
        // Expedition cards
        int expeditionStackSize,
        
        // Shipyard stacks
        int shipyardLevel1Size,
        int shipyardLevel2Size,
        int shipyardLevel3Size,
        
        // Ship stacks
        int tradeShipLevel1Size,
        int tradeShipLevel2Size,
        int tradeShipLevel3Size,
        int explorerShipLevel1Size,
        int explorerShipLevel2Size,
        int explorerShipLevel3Size,
        
        // Island stacks
        int oldWorldIslandsSize,
        int newWorldIslandsSize,
        
        // Shared pools
        int farmers,
        int workers,
        int artisans,
        int engineers,
        int investors,
        int gold,
        int tradeChips,
        int explorerChips
    ) {
        /**
         * Creates a BoardState from the actual Board object.
         */
        public static BoardState fromBoard(Board board) {
            // Count total available factories across all stacks
            int totalFactories = board.getFactoryStacks().stream()
                .mapToInt(stack -> stack.size())
                .sum();
            
            int totalResidentCards = board.getResidentStack1().size() 
                + board.getResidentStack2().size() 
                + board.getResidentStack3().size();
            
            return new BoardState(
                totalFactories,
                board.getResidentStack1().size(),
                board.getResidentStack2().size(),
                board.getResidentStack3().size(),
                totalResidentCards,
                board.getExpeditionStack().size(),
                board.getShipyardLevel1().size(),
                board.getShipyardLevel2().size(),
                board.getShipyardLevel3().size(),
                board.getTradeShipLevel1().size(),
                board.getTradeShipLevel2().size(),
                board.getTradeShipLevel3().size(),
                board.getExplorerShipLevel1().size(),
                board.getExplorerShipLevel2().size(),
                board.getExplorerShipLevel3().size(),
                board.getOldWorldIslands().size(),
                board.getNewWorldIslands().size(),
                board.getFarmers(),
                board.getWorkers(),
                board.getArtisans(),
                board.getEngineers(),
                board.getInvestors(),
                board.getGold(),
                board.getTradeChips(),
                board.getExplorerChips()
            );
        }
    }
    
    /**
     * Represents a single player's state.
     */
    public record PlayerState(
        String name,
        int position,
        
        // Board tiles
        int freeLandTiles,
        int freeCoastTiles,
        int freeSeaTiles,
        
        // Buildings and ships
        int factoryCount,
        int shipyardCount,
        int tradeShipCount,
        int explorerShipCount,
        
        // Resources
        int gold,
        int availableTradeChips,
        int availableExplorerChips,
        
        // Residents
        int residentCount,
        List<ResidentSummary> residents,
        
        // Cards
        int residentCardCount
    ) {
        /**
         * Creates PlayerState list from Player array.
         */
        public static List<PlayerState> fromPlayers(Player[] players) {
            return java.util.Arrays.stream(players)
                .map(PlayerState::fromPlayer)
                .toList();
        }
        
        /**
         * Creates a PlayerState from a Player object.
         */
        public static PlayerState fromPlayer(Player player) {
            var board = player.getPlayerBoard();
            
            // Count residents
            var residents = board.getResidents().stream()
                .map(r -> new ResidentSummary(
                    r.getPopulationLevel(),
                    r.getStatus().name()
                ))
                .toList();
            
            return new PlayerState(
                player.getName(),
                player.getPosition(),
                board.getFreeLandTiles(),
                board.getFreeCoastTiles(),
                board.getFreeSeaTiles(),
                board.getFactories().length,
                board.getShipyards().size(),
                board.getTradeShips().size(),
                board.getExplorerShips().size(),
                board.getGold(),
                board.getAvailableTradeChips(),
                board.getAvailableExplorerChips(),
                board.getResidents().size(),
                residents,
                board.getResidentCards().size()
            );
        }
    }
    
    /**
     * Summary of a single resident.
     */
    public record ResidentSummary(
        int level,
        String status
    ) {}
}
