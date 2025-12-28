package com.anno1800.engine;

import com.anno1800.board.Board;
import com.anno1800.player.Player;
import com.anno1800.player.PlayerBoard;
import com.anno1800.player.PlayerBoard.ShipType;
import com.anno1800.Boardtiles.Producers;

/**
 * Central rules engine for Anno 1800 board game.
 * Contains all game rule validations and constraints.
 * 
 * This class provides a clean separation between game state (Board, PlayerBoard)
 * and game rules (what moves are legal). This architecture enables:
 * - AI agents to query valid moves
 * - Centralized rule enforcement
 * - Easy rule modifications and testing
 * - Clear documentation of game mechanics
 */
public class Rules {
    
    // ==================== SHIP BUILDING ====================
    
    /**
     * Checks if a player can build a ship of the given type and level.
     * 
     * Requirements:
     * - Player must have at least one free sea tile
     * - Ship must be available on the board (deque not empty)
     * - Required chips must be available on the board (chips >= level)
     * - Level must be valid (1-3)
     * 
     * @param board The game board
     * @param player The player attempting to build
     * @param type The type of ship (ExplorerShip or TradeShip)
     * @param level The level of the ship (1-3)
     * @return true if the ship can be built
     */
    public static boolean canBuildShip(Board board, Player player, ShipType type, int level) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Validate level
        if (level < 1 || level > 3) {
            return false;
        }
        
        // Check if player has free sea tiles
        if (playerBoard.getFreeSeaTiles() < 1) {
            return false;
        }
        
        // Check if ship and chips are available on board
        return board.canTakeShip(type, level);
    }
    
    /**
     * Checks if a player can build multiple ships of the same type and level.
     * 
     * @param board The game board
     * @param player The player attempting to build
     * @param type The type of ship
     * @param level The level of the ship
     * @param amount The number of ships to build
     * @return true if all ships can be built
     */
    public static boolean canBuildShips(Board board, Player player, ShipType type, int level, int amount) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Validate level
        if (level < 1 || level > 3) {
            return false;
        }
        
        // Check if player has enough free sea tiles
        if (playerBoard.getFreeSeaTiles() < amount) {
            return false;
        }
        
        // Check if enough ships and chips are available on board
        // Note: This is a simplified check. In reality, we'd need to simulate
        // taking ships to account for depleting stocks
        int requiredChips = level * amount;
        boolean hasChips = switch (type) {
            case ExplorerShip -> board.getExplorerChips() >= requiredChips;
            case TradeShip -> board.getTradeChips() >= requiredChips;
        };
        
        if (!hasChips) {
            return false;
        }
        
        // Check if ships are available (simplified - assumes all at same level)
        for (int i = 0; i < amount; i++) {
            if (!board.canTakeShip(type, level)) {
                return false;
            }
        }
        
        return true;
    }
    
    // ==================== RESIDENT BUILDING ====================
    
    /**
     * Checks if a player can settle a new resident of the given population level.
     * 
     * Requirements:
     * - Resident must be available on the board
     * - Either resident cards must be available OR player must have enough gold:
     *   - Level 1-2: 1 Gold if no cards available
     *   - Level 3-5: 3 Gold if no cards available
     * 
     * @param board The game board
     * @param player The player attempting to settle
     * @param populationLevel The population level (1-5: Farmer, Worker, Artisan, Engineer, Investor)
     * @return true if the resident can be settled
     */
    public static boolean canSettleResident(Board board, Player player, int populationLevel) {
        // Validate population level
        if (populationLevel < 1 || populationLevel > 5) {
            return false;
        }
        
        // Check if residents are available on board
        boolean hasResident = switch (populationLevel) {
            case 1 -> board.getFarmers() > 0;
            case 2 -> board.getWorkers() > 0;
            case 3 -> board.getArtisans() > 0;
            case 4 -> board.getEngineers() > 0;
            case 5 -> board.getInvestors() > 0;
            default -> false;
        };
        
        if (!hasResident) {
            return false;
        }
        
        // Check if resident cards are available or player has enough gold
        boolean hasResidentCard = hasResidentCardAvailable(board, populationLevel);
        
        if (hasResidentCard) {
            return true;
        }
        
        // No resident card available, check if player has enough gold
        int requiredGold = getResidentGoldCost(populationLevel);
        return hasEnoughGold(player, requiredGold);
    }
    
    /**
     * Checks if a resident card is available for the given population level.
     * 
     * Card stacks:
     * - Stack 1 (level 2): For population levels 1-2 (Farmers, Workers)
     * - Stack 2 (level 5): For population levels 3-5 (Artisans, Engineers, Investors)
     * - Stack 3 (level 7): Special cards (currently not used for basic residents)
     * 
     * @param board The game board
     * @param populationLevel The population level
     * @return true if a resident card is available
     */
    public static boolean hasResidentCardAvailable(Board board, int populationLevel) {
        return switch (populationLevel) {
            case 1, 2 -> !board.getResidentStack1().isEmpty();
            case 3, 4, 5 -> !board.getResidentStack2().isEmpty();
            default -> false;
        };
    }
    
    /**
     * Gets the gold cost for settling a resident when no cards are available.
     * 
     * @param populationLevel The population level (1-5)
     * @return The gold cost (1 for levels 1-2, 3 for levels 3-5)
     */
    public static int getResidentGoldCost(int populationLevel) {
        if (populationLevel >= 1 && populationLevel <= 2) {
            return 1;
        } else if (populationLevel >= 3 && populationLevel <= 5) {
            return 3;
        }
        return 0; // Invalid level
    }
    
    // ==================== FACTORY BUILDING ====================
    
    /**
     * Checks if a player can build a factory of the given type.
     * 
     * Requirements:
     * - Player must have a free land or coast tile (depending on factory type)
     * - Factory must be available on the board
     * - Player must have required resources/gold
     * 
     * @param board The game board
     * @param player The player attempting to build
     * @param factoryType The type of factory to build
     * @return true if the factory can be built
     */
    public static boolean canBuildFactory(Board board, Player player, Producers factoryType) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if player has free tiles
        // This is simplified - would need to check factory-specific requirements
        // (land vs coast tiles)
        if (playerBoard.getFreeLandTiles() < 1 && playerBoard.getFreeCoastTiles() < 1) {
            return false;
        }
        
        // TODO: Check if factory is available on board
        // TODO: Check if player has required resources/gold
        
        return true;
    }
    
    // ==================== SHIPYARD BUILDING ====================
    
    /**
     * Checks if a player can build a shipyard of the given level.
     * 
     * Requirements:
     * - Player must have a free coast tile
     * - Shipyard must be available on the board
     * - Player must have required resources
     * 
     * @param board The game board
     * @param player The player attempting to build
     * @param level The level of the shipyard (1-3)
     * @return true if the shipyard can be built
     */
    public static boolean canBuildShipyard(Board board, Player player, int level) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Validate level
        if (level < 1 || level > 3) {
            return false;
        }
        
        // Check if player has free coast tiles
        if (playerBoard.getFreeCoastTiles() < 1) {
            return false;
        }
        
        // Check if shipyard is available on board
        boolean hasShipyard = switch (level) {
            case 1 -> !board.getShipyardLevel1().isEmpty();
            case 2 -> !board.getShipyardLevel2().isEmpty();
            case 3 -> !board.getShipyardLevel3().isEmpty();
            default -> false;
        };
        
        return hasShipyard;
    }
    
    // ==================== RESOURCE MANAGEMENT ====================
    
    /**
     * Checks if a player has enough gold for a purchase.
     * 
     * @param player The player
     * @param cost The cost in gold
     * @return true if the player has enough gold
     */
    public static boolean hasEnoughGold(Player player, int cost) {
        return player.getPlayerBoard().getGold() >= cost;
    }
    
    /**
     * Checks if enough gold is available on the board.
     * 
     * @param board The game board
     * @param amount The amount of gold needed
     * @return true if enough gold is available
     */
    public static boolean isBoardGoldAvailable(Board board, int amount) {
        return board.getGold() >= amount;
    }
}
