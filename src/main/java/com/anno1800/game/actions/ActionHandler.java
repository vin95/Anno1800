package com.anno1800.game.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.rewards.Reward;

import static com.anno1800.game.residents.ResidentStatus.*;

/**
 * Handles the execution of player actions.
 * Translates action requests into concrete game state changes.
 */
public class ActionHandler {
    private final Game game;

    public ActionHandler(Game game) {
        this.game = game;
    }

    /**
     * Execute a player action.
     * 
     * @param action The action to execute
     * @param player The player performing the action
     * @return true if the action was executed successfully, false otherwise
     */
    public boolean execute(Action action, Player player) {
        return switch (action) {
            case Action.BuildFactory(Factory factory) ->
                buildFactory(player, factory);
            case Action.BuildShipyard(int level) ->
                buildShipyard(player, level);
            case Action.BuildShips(com.anno1800.game.player.PlayerBoard.ShipType shipType, int level, int amount) ->
                buildShips(player, shipType, level, amount);
            case Action.FulfillNeeds(ResidentCard residentCard, Goods[] good) ->
                fulfillNeeds(player, residentCard, good);
            case Action.SwapResidentCards(ResidentCard[] cardsToSwap) ->
                swapResidentCards(player, cardsToSwap);
            case Action.SettleResident(int level) ->
                settleResident(player, level);
            case Action.UpgradeResident(int[] amount, int[] residentLevel) ->
                upgradeResident(player, amount, residentLevel);
            case Action.DiscoverOldWorldIsland() ->
                discoverOldWorldIsland(player);
            case Action.DiscoverNewWorldIsland() ->
                discoverNewWorldIsland(player);
            case Action.Expedition() ->
                expedition(player);
            case Action.Carneval() ->
                carneval();
            case Action.DoOvertime(int populationLevel) ->
                doOvertime(player, populationLevel);
            case Action.ProduceGoods(Factory factory) ->
                produceGoods(player, factory);
            case Action.TradeGoods(Goods good, int playerId) ->
                tradeGoods(player, good, playerId);
            case Action.ActivateReward(Reward reward) ->
                activateReward(player, reward);
            case Action.AssignWorker(Factory factory, Resident resident, int slot) ->
                assignWorker(player, factory, resident, slot);
            case Action.ExhaustWorker(Resident resident) ->
                exhaustWorker(player, resident);
            case Action.DrawResidentCard() ->
                drawResidentCard(player);
            case Action.ImportGood(Goods good) ->
                importGood(player, good);
            default -> throw new IllegalArgumentException("Unknown action type: " + action);
        };
    }

    /**
     * Build a factory on a free land tile.
     * 
     * PRECONDITION: ActionValidator has verified all requirements.
     * All required goods must have been produced in previous actions.
     */
    private boolean buildFactory(Player player, Factory factory) {
        // Goods verification (informational only)
        Goods[] costs = factory.costs();
        if (costs != null && costs.length > 0) {
            System.out.println("Building factory requires: " + java.util.Arrays.toString(costs));
            System.out.println("Assuming all goods were produced in previous ProduceGoods actions.");
        }

        // Take factory from board
        Factory factoryFromBoard = game.getBoard().takeFactory(factory.getType());

        // Add factory to player's board
        player.getPlayerBoard().addFactory(factoryFromBoard);

        System.out.println("Successfully built factory: " + factory.getType());
        return true;
    }

    /**
     * Build a shipyard on a free coast tile.
     * 
     * PRECONDITION: ActionValidator has verified all requirements.
     */
    private boolean buildShipyard(Player player, int level) {
        // Take shipyard from board
        var shipyard = game.getBoard().takeShipyard(level);

        // Add shipyard to player's board
        player.getPlayerBoard().addShipyard(shipyard);

        System.out.println("Successfully built shipyard level " + level);
        return true;
    }

    /**
     * Build ships using the player's shipyards.
     * Each shipyard can build one ship per action.
     * Ships are built sequentially, so chips earned from one ship are available for
     * the next.
     * 
     * PRECONDITION: ActionValidator has verified all requirements.
     */
    private boolean buildShips(Player player, com.anno1800.game.player.PlayerBoard.ShipType shipType, int level,
            int amount) {
        // Build ships sequentially
        // Important: Ships are built one by one, so chips from previous ships
        // are available for building the next ship
        for (int i = 0; i < amount; i++) {
            // Take ship from board (also takes the required chips)
            Object ship = game.getBoard().takeShip(shipType, level);

            // Add ship to player's board (also adds the chips to player's available chips)
            player.getPlayerBoard().addShip(ship, shipType, level);

            System.out.println("Built ship " + (i + 1) + "/" + amount + ": " + shipType + " level " + level);
        }

        System.out.println("Successfully built " + amount + " " + shipType + "(s) of level " + level);
        return true;
    }

    /**
     * Fulfill a resident's need with a specific good.
     */
    private boolean fulfillNeeds(Player player, ResidentCard residentCard, Goods[] goods) {
        // TODO: Implement need fulfillment logic
        // 1. Check if resident belongs to player
        // 2. Check if player has the required good
        // 3. Deduct good from inventory
        // 4. Grant reward based on resident level and good type
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Swap resident cards.
     */
    private boolean swapResidentCards(Player player, ResidentCard[] cardsToSwap) {
        // TODO: Implement resident card swapping logic
        // 1. Check if player has the cards to swap
        // 2. Remove old cards
        // 3. Add new cards
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Settle a new resident or recover an exhausted one.
     */
    private boolean settleResident(Player player, int level) {
        // TODO: Implement resident settlement logic
        // 1. Get settlement cost for the level (ResidentCosts.getSettlementCost(level))
        // 2. Check if player has required resources
        // 3. Deduct resources
        // 4. Add new resident with FIT status to player's residents
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Upgrade a resident to the next population level.
     */
    private boolean upgradeResident(Player player, int[] amount, int[] residentLevel) {
        // TODO: Implement resident upgrade logic
        // 1. Check if resident belongs to player
        // 2. Check if resident can be upgraded (level < 5)
        // 3. Get upgrade cost (ResidentCosts.getUpgradeCost(currentLevel + 1))
        // 4. Check if player has required resources
        // 5. Deduct resources
        // 6. Create new resident with higher level
        // 7. Replace old resident with new one
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Discover an Old World Island.
     */
    private boolean discoverOldWorldIsland(Player player) {
        // TODO: Implement Old World island discovery logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Discover a New World Island.
     */
    private boolean discoverNewWorldIsland(Player player) {
        // TODO: Implement New World island discovery logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Go on an expedition.
     */
    private boolean expedition(Player player) {
        // TODO: Implement expedition logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Celebrate carnival.
     */
    private boolean carneval() {
        // TODO: Implement carnival logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Do overtime with a resident.
     * Changes the resident status from EXHAUSTED or AT_WORK to FIT.
     * Costs gold equal to the population level.
     * 
     * PRECONDITION: ActionValidator has verified:
     * - Player has a resident with the given population level and status EXHAUSTED
     * or AT_WORK
     * - Player has enough gold
     */
    private boolean doOvertime(Player player, int populationLevel) {
        // Find the first resident with the given population level and status EXHAUSTED
        // or AT_WORK
        Resident targetResident = null;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == populationLevel &&
                    (resident.getStatus() == EXHAUSTED || resident.getStatus() == AT_WORK)) {
                targetResident = resident;
                break;
            }
        }

        // Change status to FIT
        targetResident.setStatus(FIT);

        // Deduct gold (1 gold per population level)
        // TODO: Implement gold deduction when PlayerBoard has a method for this

        return true;
    }

    /**
     * Produce goods in a factory by assigning a FIT resident to an empty slot.
     * The good is considered produced immediately and must be consumed by another
     * action
     * (e.g., BuildFactory, BuildShipyard) in the same turn.
     * 
     * PRECONDITION: ActionValidator has verified all requirements.
     * 
     * IMPORTANT: ProduceGoods is always coupled with another action that consumes
     * the goods.
     * It cannot be executed standalone - the goods must be consumed immediately.
     */
    private boolean produceGoods(Player player, Factory factory) {
        // Find a FIT resident with the correct population level
        Resident residentToAssign = null;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == factory.populationLevel() &&
                    resident.getStatus() == FIT) {
                residentToAssign = resident;
                break;
            }
        }

        // Assign resident to first empty slot
        if (factory.getSlot1() == null) {
            factory.setSlot1(residentToAssign);
        } else {
            factory.setSlot2(residentToAssign);
        }

        // Update resident status to AT_WORK
        residentToAssign.setStatus(AT_WORK);

        System.out.println("Resident (level " + residentToAssign.getPopulationLevel() +
                ") assigned to factory " + factory.getType() +
                ". Produced: " + factory.produces());

        return true;
    }

    /**
     * Trade goods: export one good and import another.
     */
    private boolean tradeGoods(Player player, Goods good, int playerId) {
        // TODO: Implement trading logic
        // 1. Check if player has trade ship available
        // 2. Check if player has the export good
        // 3. Check trade point costs
        // 4. Deduct export good and trade points
        // 5. Add import good to inventory
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Activate a reward.
     */
    private boolean activateReward(Player player, Reward reward) {
        // TODO: Implement reward activation logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Assign a resident worker to a factory slot.
     */
    private boolean assignWorker(Player player, Factory factory, Resident resident, int slot) {
        // TODO: Implement worker assignment logic
        // 1. Check if resident belongs to player
        // 2. Check if resident has correct population level for factory
        // 3. Check if resident status is FIT
        // 4. Check if slot is valid (1 or 2) and empty
        // 5. Assign resident to factory slot
        // 6. Update resident status to AT_WORK
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Exhaust a worker resident.
     */
    private boolean exhaustWorker(Player player, Resident resident) {
        // TODO: Implement worker exhaustion logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Draw a resident card from the deck.
     */
    private boolean drawResidentCard(Player player) {
        // TODO: Implement card drawing logic
        // 1. Check if there are cards in the deck
        // 2. Draw top card
        // 3. Apply card effects (rewards, actions, etc.)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Import a good.
     */
    private boolean importGood(Player player, Goods good) {
        // TODO: Implement import logic
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
