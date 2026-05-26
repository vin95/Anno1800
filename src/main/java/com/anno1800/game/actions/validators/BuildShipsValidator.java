package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.ShipCosts;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.board.Board;

/**
 * Validates building-related actions (factories, shipyards, ships).
 */
public class BuildShipsValidator {

    /**
     * Validates BuildShips action.
     * Requirements:
     * - Must have free sea tiles
     * - Ship must be available on the board (deque not empty)
     * - Required chips must be available on the board
     * - Ship level must be valid (1-3)
     * - Required goods must be obtainable for each ship (production/trade/import)
     */
    public static boolean canBuildShips(Action.BuildShips action, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();

        // Check if player has enough free sea tiles for the amount of ships
        if (playerBoard.getFreeSeaTiles() < action.amount()) {
            return false;
        }

        // Validate level
        if (action.level() < 1 || action.level() > 3) {
            return false;
        }

        if (action.amount() < 1) {
            return false;
        }

        // Ship level must not exceed the available shipyard level.
        // A level 2 ship requires a level 2 or 3 shipyard, and level 3 ships require level 3 shipyards.
        int eligibleShipyards = countShipyardsAtOrAboveLevel(playerBoard, action.level());
        if (eligibleShipyards < action.amount()) {
            return false;
        }

        // Check chip availability for all ships.
        int requiredChips = action.level() * action.amount();
        boolean hasEnoughChips = switch (action.shipType()) {
            case TradeShip -> board.getTradeChips() >= requiredChips;
            case ExplorerShip -> board.getExplorerChips() >= requiredChips;
        };
        if (!hasEnoughChips) {
            return false;
        }

        // Check ship availability in the corresponding stack.
        int availableShips = switch (action.shipType()) {
            case TradeShip -> switch (action.level()) {
                case 1 -> board.getTradeShipLevel1().size();
                case 2 -> board.getTradeShipLevel2().size();
                case 3 -> board.getTradeShipLevel3().size();
                default -> 0;
            };
            case ExplorerShip -> switch (action.level()) {
                case 1 -> board.getExplorerShipLevel1().size();
                case 2 -> board.getExplorerShipLevel2().size();
                case 3 -> board.getExplorerShipLevel3().size();
                default -> 0;
            };
        };
        if (availableShips < action.amount()) {
            return false;
        }
        
        // PLANNING PHASE: Check if player can obtain required goods for all ships.
        Goods[] costPerShip = ShipCosts.getShipCost(action.level());
        
        // Plan sequentially against one planning context and rollback afterwards.
        for (int i = 0; i < action.amount(); i++) {
            if (!playerBoard.canObtainGoods(costPerShip, game)) {
                // Can't obtain goods for this ship - rollback
                playerBoard.clearStoredGoods();
                return false;
            }
        }
        
        // Clear storedGoods after validation (rollback)
        playerBoard.clearStoredGoods();

        return true;
    }

    private static int countShipyardsAtOrAboveLevel(PlayerBoard playerBoard, int requiredLevel) {
        int eligibleShipyards = 0;
        for (Shipyard shipyard : playerBoard.getShipyards()) {
            if (shipyard != null && shipyard.getLevel() >= requiredLevel) {
                eligibleShipyards++;
            }
        }
        return eligibleShipyards;
    }
}
