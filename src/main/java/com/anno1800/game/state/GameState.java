package com.anno1800.game.state;

import java.time.Instant;
import java.util.List;

import com.anno1800.game.board.Board;
import com.anno1800.game.player.Player;

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
    int startingPlayerIndex,
    
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
     * @param startingPlayerIndex Index of the starting player
     * @return Immutable GameState snapshot
     */
    public static GameState createSnapshot(
        Board board, 
        Player[] players, 
        int round, 
        int currentPlayerIndex,
        int startingPlayerIndex
    ) {
        return new GameState(
            Instant.now(),
            round,
            currentPlayerIndex,
            startingPlayerIndex,
            BoardState.fromBoard(board),
            PlayerState.fromPlayers(players)
        );
    }
    
    /**
     * Represents the shared board state.
     * Contains all stacks and pools available to all players.
     * 
     * Uses nested records for logical grouping instead of a flat 25-parameter structure.
     */
    public record BoardState(
        FactoryState factories,
        ResidentCardState residentCards,
        ExpeditionState expeditions,
        ShipyardState shipyards,
        ShipState ships,
        IslandState islands,
        ResourcePoolState resources,
        EndPhaseState endPhase
    ) {
        /**
         * Creates a BoardState from the actual Board object.
         */
        public static BoardState fromBoard(Board board) {
            return new BoardState(
                FactoryState.fromBoard(board),
                ResidentCardState.fromBoard(board),
                ExpeditionState.fromBoard(board),
                ShipyardState.fromBoard(board),
                ShipState.fromBoard(board),
                IslandState.fromBoard(board),
                ResourcePoolState.fromBoard(board),
                EndPhaseState.fromBoard(board)
            );
        }
        
        /**
         * Factory stacks state.
         */
        public record FactoryState(java.util.Map<com.anno1800.data.gamedata.Producers, Integer> blueprints) {
            static FactoryState fromBoard(Board board) {
                return new FactoryState(java.util.Collections.unmodifiableMap(board.getFactoryBlueprintCounts()));
            }
            /** Backwards-compat helper: total available blueprints across all factory types. */
            public int availableFactories() {
                return blueprints.values().stream().mapToInt(Integer::intValue).sum();
            }
        }
        
        /**
         * Resident card stacks state.
         */
        public record ResidentCardState(
            int stack1Size,
            int stack2Size,
            int stack3Size,
            int totalAvailable
        ) {
            static ResidentCardState fromBoard(Board board) {
                int s1 = board.getResidentStack1().size();
                int s2 = board.getResidentStack2().size();
                int s3 = board.getResidentStack3().size();
                return new ResidentCardState(s1, s2, s3, s1 + s2 + s3);
            }
        }
        
        /**
         * Expedition card stack state.
         */
        public record ExpeditionState(int stackSize) {
            static ExpeditionState fromBoard(Board board) {
                return new ExpeditionState(board.getExpeditionStack().size());
            }
        }
        
        /**
         * Shipyard stacks state (available shipyard tiles by level).
         */
        public record ShipyardState(int level1, int level2, int level3) {
            static ShipyardState fromBoard(Board board) {
                return new ShipyardState(
                    board.getShipyardLevel1().size(),
                    board.getShipyardLevel2().size(),
                    board.getShipyardLevel3().size()
                );
            }
        }
        
        /**
         * Ship stacks state (available ships by type and level).
         */
        public record ShipState(
            ShipLevelCounts tradeShips,
            ShipLevelCounts explorerShips
        ) {
            static ShipState fromBoard(Board board) {
                return new ShipState(
                    new ShipLevelCounts(
                        board.getTradeShipLevel1().size(),
                        board.getTradeShipLevel2().size(),
                        board.getTradeShipLevel3().size()
                    ),
                    new ShipLevelCounts(
                        board.getExplorerShipLevel1().size(),
                        board.getExplorerShipLevel2().size(),
                        board.getExplorerShipLevel3().size()
                    )
                );
            }
            
            /**
             * Ship counts by level (1, 2, 3).
             */
            public record ShipLevelCounts(int level1, int level2, int level3) {}
        }
        
        /**
         * Island stacks state (available islands by world).
         */
        public record IslandState(int oldWorldSize, int newWorldSize) {
            static IslandState fromBoard(Board board) {
                return new IslandState(
                    board.getOldWorldIslands().size(),
                    board.getNewWorldIslands().size()
                );
            }
        }
        
        /**
         * End phase tracking.
         * 
         * @param isActive True if end phase has been triggered
         * @param triggeredByPlayerIndex Index of the player who triggered end phase (-1 if not triggered)
         * @param triggeredInRound Round number when end phase was triggered (0 if not triggered)
         */
        public record EndPhaseState(
            boolean isActive,
            int triggeredByPlayerIndex,
            int triggeredInRound
        ) {
            static EndPhaseState fromBoard(Board board) {
                return new EndPhaseState(
                    board.isEndPhase(),
                    board.getEndPhaseTriggeredByPlayer(),
                    board.getEndPhaseTriggeredInRound()
                );
            }
        }
        
        /**
         * Shared resource pools (residents, gold, chips).
         */
        public record ResourcePoolState(
            int farmers,
            int workers,
            int artisans,
            int engineers,
            int investors,
            int gold,
            int tradeChips,
            int explorerChips
        ) {
            static ResourcePoolState fromBoard(Board board) {
                return new ResourcePoolState(
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
    }
    
    /**
     * Represents a single player's state.
     * Uses nested records for logical grouping instead of a flat 18-parameter structure.
     */
    public record PlayerState(
        String name,
        int position,
        TileState tiles,
        BuildingState buildings,
        PlayerShipState ships,
        PlayerResourceState resources,
        ResidentState residents,
        CardState cards,
        java.util.List<String> discoveredOldWorldIslands,
        java.util.List<String> discoveredNewWorldIslands
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
            
            // Count trade ships by level
            int tradeL1 = (int) board.getTradeShips().stream().filter(s -> s.getLevel() == 1).count();
            int tradeL2 = (int) board.getTradeShips().stream().filter(s -> s.getLevel() == 2).count();
            int tradeL3 = (int) board.getTradeShips().stream().filter(s -> s.getLevel() == 3).count();
            
            // Count explorer ships by level
            int explorerL1 = (int) board.getExplorerShips().stream().filter(s -> s.getLevel() == 1).count();
            int explorerL2 = (int) board.getExplorerShips().stream().filter(s -> s.getLevel() == 2).count();
            int explorerL3 = (int) board.getExplorerShips().stream().filter(s -> s.getLevel() == 3).count();
            
            return new PlayerState(
                player.getName(),
                player.getPosition(),
                new TileState(
                    java.util.Arrays.stream(board.getLandTileTypes() == null ? new String[0] : board.getLandTileTypes())
                        .map(s -> s == null ? "empty" : s)
                        .toList(),
                    java.util.Arrays.stream(board.getCoastTileTypes() == null ? new String[0] : board.getCoastTileTypes())
                        .map(s -> s == null ? "empty" : s)
                        .toList(),
                    java.util.Arrays.stream(board.getSeaTileTypes() == null ? new String[0] : board.getSeaTileTypes())
                        .map(s -> s == null ? "empty" : s)
                        .toList()
                ),
                new BuildingState(
                    board.getAllActiveFactories().size(),
                    board.getShipyards().size()
                ),
                new PlayerShipState(
                    new PlayerShipState.ShipCountWithLevels(
                        board.getTradeShips().size(),
                        new BoardState.ShipState.ShipLevelCounts(tradeL1, tradeL2, tradeL3)
                    ),
                    new PlayerShipState.ShipCountWithLevels(
                        board.getExplorerShips().size(),
                        new BoardState.ShipState.ShipLevelCounts(explorerL1, explorerL2, explorerL3)
                    )
                ),
                new PlayerResourceState(
                    board.getGold(),
                    board.getAvailableTradeChips(),
                    board.getAvailableExplorerChips()
                ),
                new ResidentState(
                    board.getResidents().size(),
                    residents
                ),
                new CardState(
                    board.getResidentCards().size()
                ),
                // discovered old/new world islands (serialize as short summaries)
                board.getOwnedOldWorldIslands().stream().map(isl -> {
                    StringBuilder s = new StringBuilder();
                    s.append("old[land=").append(isl.getFreeLandTiles())
                     .append(",coast=").append(isl.getFreeCoastTiles())
                     .append(",sea=").append(isl.getFreeSeaTiles())
                     .append(",reward=").append(isl.getReward() == null ? "none" : isl.getReward().toString());
                    if (isl.getFactories() != null && isl.getFactories().length > 0) {
                        s.append(",factories=");
                        for (int fi = 0; fi < isl.getFactories().length; fi++) {
                            if (fi > 0) s.append("|");
                            s.append(isl.getFactories()[fi].getType().name());
                        }
                    }
                    s.append("]");
                    return s.toString();
                }).toList(),
                board.getOwnedNewWorldIslands().stream().map(isl -> {
                    StringBuilder s = new StringBuilder();
                    s.append("new[plantations=");
                    var pls = isl.getPlantations();
                    for (int pi = 0; pi < pls.length; pi++) {
                        if (pi > 0) s.append("|");
                        s.append(pls[pi].getType().name());
                    }
                    s.append("]");
                    return s.toString();
                }).toList()
            );
        }
        
        /**
         * Player's tile state (free tiles on player board).
         */
        public record TileState(
            java.util.List<String> landtiles,
            java.util.List<String> coasttiles,
            java.util.List<String> seatiles
        ) {}
        
        /**
         * Player's building state (factories and shipyards).
         */
        public record BuildingState(
            int factoryCount,
            int shipyardCount
        ) {}
        
        /**
         * Player's ship state (trade and explorer ships with counts and levels).
         */
        public record PlayerShipState(
            ShipCountWithLevels tradeShips,
            ShipCountWithLevels explorerShips
        ) {
            /**
             * Ship count and level breakdown.
             */
            public record ShipCountWithLevels(
                int totalCount,
                BoardState.ShipState.ShipLevelCounts levels
            ) {}
        }
        
        /**
         * Player's resource state (gold and chips).
         */
        public record PlayerResourceState(
            int gold,
            int availableTradeChips,
            int availableExplorerChips
        ) {}
        
        /**
         * Player's resident state (count and detailed list).
         */
        public record ResidentState(
            int count,
            List<ResidentSummary> residents
        ) {}
        
        /**
         * Player's card state.
         */
        public record CardState(
            int residentCardCount
        ) {}
    }
    
    /**
     * Summary of a single resident.
     */
    public record ResidentSummary(
        int level,
        String status
    ) {}
}
