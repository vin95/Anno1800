package com.anno1800.game.actions;

import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Factory;
import com.anno1800.data.gamedata.Goods;

/**
 * Validates whether actions can be executed according to game rules.
 * Separation of Concerns: Validation logic is separate from execution logic.
 */
public class ActionValidator {

    /**
     * Check if an action can be executed by a player in the current game state.
     * 
     * @param action The action to validate
     * @param player The player attempting the action
     * @param game   The current game state
     * @return true if the action is valid and can be executed
     */
    public static boolean canExecute(Action action, Player player, Game game) {
        return switch (action) {
            case Action.BuildFactory buildFactory -> canBuildFactory(buildFactory, player, game);
            case Action.BuildShipyard buildShipyard -> canBuildShipyard(buildShipyard, player, game);
            case Action.BuildShips buildShips -> canBuildShips(buildShips, player, game);
            case Action.FulfillNeeds fulfillNeeds -> canFulfillNeeds(fulfillNeeds, player, game);
            case Action.SwapResidentCards swapResidentCards -> canSwapResidentCards(swapResidentCards, player, game);
            case Action.SettleResident settleResident -> canSettleResident(settleResident, player, game);
            case Action.UpgradeResident upgradeResident -> canUpgradeResident(upgradeResident, player, game);
            case Action.DiscoverOldWorldIsland discoverOldWorldIsland ->
                canDiscoverOldWorldIsland(discoverOldWorldIsland, player, game);
            case Action.DiscoverNewWorldIsland discoverNewWorldIsland ->
                canDiscoverNewWorldIsland(discoverNewWorldIsland, player, game);
            case Action.Expedition expedition -> canExpedition(expedition, player, game);
            case Action.Carneval carneval -> canCarneval(carneval, player, game);

            case Action.DoOvertime doOvertime -> canDoOvertime(doOvertime, player, game);
            case Action.ProduceGoods produceGoods -> canProduceGoods(produceGoods, player, game);
            case Action.TradeGoods tradeGoods -> canTradeGoods(tradeGoods, player, game);
            case Action.ActivateReward activateReward -> canActivateReward(activateReward, player, game);

            case Action.AssignWorker assignWorker -> canAssignWorker(assignWorker, player, game);
            case Action.ExhaustWorker exhaustWorker -> canExhaustWorker(exhaustWorker, player, game);
            case Action.DrawResidentCard drawCard -> canDrawResidentCard(drawCard, player, game);
            case Action.ImportGood importGood -> canImportGood(importGood, player, game);
        };
    }

    /**
     * Validates BuildFactory action.
     * Requirements:
     * - Must have free land or coast tile (depending on factory type)
     * - Factory type must be available on board
     * - Required goods must be producible (checked by agent/AI before action)
     * 
     * Note: This does NOT check if goods are available, because goods are produced
     * just-in-time and immediately consumed. The agent/AI must ensure that
     * ProduceGoods actions are executed before BuildFactory.
     */
    private static boolean canBuildFactory(Action.BuildFactory action, Player player, Game game) {
        Factory factory = action.factory();

        // Check if factory is available on the board
        if (!game.getBoard().hasFactory(factory.getType())) {
            return false;
        }

        // Check if player has free tiles (land or coast)
        PlayerBoard board = player.getPlayerBoard();
        if (board.getFreeLandTiles() <= 0 && board.getFreeCoastTiles() <= 0) {
            return false;
        }

        // Note: We do NOT check for goods availability here because:
        // 1. Goods are produced just-in-time (ProduceGoods action)
        // 2. They are immediately consumed, not stored
        // 3. The agent/ActionGenerator must ensure ProduceGoods actions come first

        return true;
    }

    /**
     * Validates BuildShipyard action.
     * Requirements:
     * - Must have free coast tiles (shipyards can only be built on coast)
     * - Shipyard of the specified level must be available on board
     * - Level must be valid (1-3)
     */
    private static boolean canBuildShipyard(Action.BuildShipyard action, Player player, Game game) {
        // Validate level
        if (action.level() < 1 || action.level() > 3) {
            return false;
        }

        // Check if player has free coast tiles
        if (player.getPlayerBoard().getFreeCoastTiles() <= 0) {
            return false;
        }

        // Check if shipyard is available on board
        if (!game.getBoard().hasShipyard(action.level())) {
            return false;
        }

        return true;
    }

    /**
     * Validates BuildShips action.
     * Requirements:
     * - Must have free sea tiles
     * - Ship must be available on the board (deque not empty)
     * - Required chips must be available on the board
     * - Ship level must be valid (1-3)
     */
    private static boolean canBuildShips(Action.BuildShips action, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();

        // Check if player has enough free sea tiles for the amount of ships
        if (playerBoard.getFreeSeaTiles() < action.amount()) {
            return false;
        }

        // Validate level
        if (action.level() < 1 || action.level() > 3) {
            return false;
        }

        // Check board-side constraints for each ship
        for (int i = 0; i < action.amount(); i++) {
            if (!game.getBoard().canTakeShip(action.shipType(), action.level())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates FulfillNeeds action.
     * Requirements:
     * - Player must own the resident card
     * - The provided goods array must match or cover the card's needs
     * - All goods must be either producible or tradeable
     */
    private static boolean canFulfillNeeds(Action.FulfillNeeds action, Player player, ResidentCard residentCard) {
        // Check if player owns the resident card
        if (!player.getPlayerBoard().getResidentCards().contains(residentCard)) {
            return false;
        }
        
        Goods[] needs = residentCard.needs();
        Goods[] providedGoods = action.goods();
        
        // Check that provided goods match the needs
        if (providedGoods == null || providedGoods.length != needs.length) {
            return false;
        }
        
        // Check each provided good can be fulfilled (either produced or traded)
        for (Goods good : providedGoods) {
            boolean canFulfill = false;
            
            // Check if player can produce this good
            for (Factory factory : player.getPlayerBoard().getFactories()) {
                if (factory != null && factory.produces() == good) {
                    // Check if this factory can produce (has empty slot and FIT resident)
                    if (factory.getSlot1() == null || factory.getSlot2() == null) {
                        for (Resident resident : player.getPlayerBoard().getResidents()) {
                            if (resident.getPopulationLevel() == factory.populationLevel() &&
                                resident.getStatus() == com.anno1800.game.residents.ResidentStatus.FIT) {
                                canFulfill = true;
                                break;
                            }
                        }
                    }
                    if (canFulfill) break;
                }
            }
            
            // If can't produce, check if can trade
            if (!canFulfill) {
                // Check if any other player can produce this good
                for (Player otherPlayer : game.getPlayers()) {
                    if (otherPlayer == player) continue;
                    
                    for (Factory factory : otherPlayer.getPlayerBoard().getFactories()) {
                        if (factory != null && factory.produces() == good) {
                            // Check if player has enough trade chips
                            if (player.getPlayerBoard().getAvailableTradeChips() >= factory.getTradeCosts()) {
                                canFulfill = true;
                                break;
                            }
                        }
                    }
                    if (canFulfill) break;
                }
            }
            
            // If this good cannot be fulfilled, return false
            if (!canFulfill) {
                return false;
            }
        }
        
        return true;
    }
    }

    private static boolean canSwapResidentCards(Action.SwapResidentCards action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    private static boolean canSettleResident(Action.SettleResident action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    private static boolean canUpgradeResident(Action.UpgradeResident action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    /**
     * Validates DiscoverOldWorldIsland action.
     * Requirements:
     * - Must have at least one available explorer chip
     * - Old World islands must be available on the board
     */
    private static boolean canDiscoverOldWorldIsland(Action.DiscoverOldWorldIsland action, Player player, Game game) {
        // Check if player has at least numOldWorldIslands + 1 available explorer chips
        if (player.getPlayerBoard().getAvailableExplorerChips() < player.getPlayerBoard().getNumOldWorldIslands() + 1) {
            return false;
        }

        // Check if there are Old World islands available on the board
        if (game.getBoard().getOldWorldIslands().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Validates DiscoverNewWorldIsland action.
     * Requirements:
     * - Must have at least numNewWorldIslands + 1 available explorer chips
     * - New World islands must be available on the board
     */
    private static boolean canDiscoverNewWorldIsland(Action.DiscoverNewWorldIsland action, Player player, Game game) {
        // Check if player has at least numNewWorldIslands + 1 available explorer chips
        if (player.getPlayerBoard().getAvailableExplorerChips() < player.getPlayerBoard().getNumNewWorldIslands() + 1) {
            return false;
        }

        // Check if there are New World islands available on the board
        if (game.getBoard().getNewWorldIslands().isEmpty()) {
            return false;
        }

        return true;
    }

    private static boolean canExpedition(Action.Expedition action, Player player, Game game) {
        // Check if player has at least 2 available explorer chips
        if (player.getPlayerBoard().getAvailableExplorerChips() < 2) {
            return false;
        }

        // Check if there are expedition cards available on the board
        if (game.getBoard().getExpeditionStack().isEmpty()) {
            return false;
        }

        return true;
    }

    private static boolean canCarneval(Action.Carneval action, Player player, Game game) {
        return true;
    }

    /**
     * Validates DoOvertime action.
     * Requirements:
     * - Player must have a resident with the given population level and status
     * EXHAUSTED or AT_WORK
     * - Player must have enough gold (populationLevel * 1 gold)
     */
    private static boolean canDoOvertime(Action.DoOvertime action, Player player, Game game) {
        int populationLevel = action.populationLevel();

        // Check if player has a resident with the given population level and status
        // EXHAUSTED or AT_WORK
        boolean hasEligibleResident = false;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == populationLevel &&
                    (resident.getStatus() == com.anno1800.game.residents.ResidentStatus.EXHAUSTED ||
                            resident.getStatus() == com.anno1800.game.residents.ResidentStatus.AT_WORK)) {
                hasEligibleResident = true;
                break;
            }
        }

        if (!hasEligibleResident) {
            return false;
        }

        // Check if player has enough gold (1 gold per population level)
        int requiredGold = populationLevel;
        if (player.getPlayerBoard().getGold() < requiredGold) {
            return false;
        }

        return true;
    }

    private static boolean canProduceGoods(Action.ProduceGoods action, Player player, Game game) {
        Factory factory = action.factory();

        // Check if factory belongs to player
        boolean factoryBelongsToPlayer = false;
        for (Factory f : player.getPlayerBoard().getFactories()) {
            if (f == factory) {
                factoryBelongsToPlayer = true;
                break;
            }
        }

        if (!factoryBelongsToPlayer) {
            return false;
        }

        // Check if at least one slot is empty
        if (factory.getSlot1() != null && factory.getSlot2() != null) {
            return false; // Both slots occupied
        }

        // Check if player has a FIT resident with the correct population level
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == factory.populationLevel() &&
                    resident.getStatus() == com.anno1800.game.residents.ResidentStatus.FIT) {
                return true;
            }
        }

        return false; // No suitable resident found
    }

    /**
     * Validates TradeGoods action.
     * Requirements:
     * - At least one different player (Mitspieler) must have a factory that
     * produces the requested good
     * - The trading player must have enough availableTradeChips to cover the trade
     * costs
     * - The trade costs are determined by the factory's tradeCosts value
     */

    private static boolean canTradeGoods(Action.TradeGoods action, Player player, Game game) {
        // Check all other players to find one with a factory that produces the
        // requested good
        Factory producingFactory = null;

        for (Player otherPlayer : game.getPlayers()) {
            // Skip the current player (cannot trade with yourself)
            if (otherPlayer == player) {
                continue;
            }

            // Check if this player has a factory that produces the requested good
            for (Factory factory : otherPlayer.getPlayerBoard().getFactories()) {
                if (factory != null && factory.produces() == action.good()) {
                    producingFactory = factory;
                    break;
                }
            }

            // If we found a factory, no need to check other players
            if (producingFactory != null) {
                break;
            }
        }

        // At least one other player must have a factory that produces this good
        if (producingFactory == null) {
            return false;
        }

        // Check if player has enough trade chips to cover the trade costs
        int tradeCosts = producingFactory.getTradeCosts();
        if (player.getPlayerBoard().getAvailableTradeChips() < tradeCosts) {
            return false;
        }

        return true;
    }

    private static boolean canActivateReward(Action.ActivateReward action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    private static boolean canAssignWorker(Action.AssignWorker action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    private static boolean canExhaustWorker(Action.ExhaustWorker action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    private static boolean canDrawResidentCard(Action.DrawResidentCard action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

    private static boolean canImportGood(Action.ImportGood action, Player player, Game game) {
        // TODO: Implement validation
        return false;
    }

}
