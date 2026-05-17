package com.anno1800.game.actions;

import java.util.ArrayList;
import java.util.List;

import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentStatus;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.FactoryData;
import com.anno1800.data.gamedata.Producers;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.data.gamedata.ShipType;

/**
 * Generates all possible valid actions for a player in the current game state.
 * Separation of Concerns: Action generation is separate from validation and execution.
 * 
 * For each action type, generates all possible instances and validates them.
 * Only returns actions that pass validation.
 */
public class ActionGenerator {
    
    /**
     * Generate all possible actions for a player.
     * 
     * @param player The player to generate actions for
     * @param game The current game state
     * @return List of all valid actions the player can take
     */
    public List<Action> getPossibleActions(Player player, Game game) {
        List<Action> actions = new ArrayList<>();
        
        // Factory actions
        actions.addAll(generateBuildFactoryActions(player, game));
        
        // Building actions
        actions.addAll(generateBuildShipyardActions(player, game));
        actions.addAll(generateBuildShipsActions(player, game));
        
        // Resident actions
        actions.addAll(generateSettleResidentActions(player, game));
        actions.addAll(generateUpgradeResidentActions(player, game));
        // actions.addAll(generateSwapResidentCardsActions(player, game));
        actions.addAll(generateFulfillNeedsActions(player, game));
        
        // Exploration actions
        actions.addAll(generateDiscoverOldWorldIslandActions(player, game));
        actions.addAll(generateDiscoverNewWorldIslandActions(player, game));
        actions.addAll(generateExpeditionActions(player, game));
        
        // Special actions
        actions.addAll(generateCarnevalActions(player, game));
        actions.addAll(generateActivateRewardActions(player, game));
        
        // Free actions (do not consume action points)
        actions.addAll(generateViewResidentCardsActions(player, game));
        
        // ObjectiveCard free actions (only available when the card is active in the game)
        actions.addAll(generateUseExtraActionActions(player, game));
        actions.addAll(generateDiscardResidentCardActions(player, game));
        actions.addAll(generateInvestorGoldActions(player, game));
        
        return actions;
    }
    
    // ========== Factory Actions ==========
    
    /**
     * Generate all possible BuildFactory actions.
     * Creates an action for each available factory type that can be built.
     */
    private List<Action> generateBuildFactoryActions(Player player, Game game) {
        List<Action> buildActions = new ArrayList<>();
        
        for (Producers producerType : Producers.values()) {
            try {
                var producer = FactoryData.getProducer(producerType);
                
                // Only allow building of regular Factories, not StartFactories
                // StartFactories are default factories and cannot be built - only overbuilt/demolished
                if (producer instanceof Factory factory && !(producer instanceof com.anno1800.game.tiles.StartFactory)) {
                    Action.BuildFactory buildAction = new Action.BuildFactory(factory);
                    
                    if (ActionValidator.canExecute(buildAction, player, game)) {
                        buildActions.add(buildAction);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Producer type not configured, skip
            }
        }
        
        return buildActions;
    }
    
    /**
     * Generate all possible DemolishFactory actions.
     * Creates an action for each factory owned by the player.
     */
    private List<Action> generateDemolishFactoryActions(Player player, Game game) {
        List<Action> demolishActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        // Check only BUILT factories (only iterate up to numFactories)
        Factory[] allFactories = board.getFactories();
        int numFactories = board.getNumFactories();
        
        for (int i = 0; i < numFactories; i++) {
            Factory factory = allFactories[i];
            if (factory != null) {
                Action.DemolishFactory demolishAction = new Action.DemolishFactory(factory);
                
                if (ActionValidator.canExecute(demolishAction, player, game)) {
                    demolishActions.add(demolishAction);
                }
            }
        }
        
        return demolishActions;
    }
    
    /**
     * Generate all possible OverbuildDefaultFactory actions.
     * For each default factory, try to overbuild with each available factory type.
     * Only regular Factories can be used to overbuild - not other StartFactories.
     */
    private List<Action> generateOverbuildDefaultFactoryActions(Player player, Game game) {
        List<Action> overbuildActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        // Get all default factories
        List<Factory> defaultFactories = board.getDefaultFactories();
        
        // For each default factory, try all available factory types
        for (Factory defaultFactory : defaultFactories) {
            for (Producers producerType : Producers.values()) {
                try {
                    var producer = FactoryData.getProducer(producerType);
                    
                    // Only allow overbuilding with regular Factories, not StartFactories
                    if (producer instanceof Factory newFactory && !(producer instanceof com.anno1800.game.tiles.StartFactory)) {
                        Action.OverbuildDefaultFactory overbuildAction = 
                            new Action.OverbuildDefaultFactory(defaultFactory, newFactory);
                        
                        if (ActionValidator.canExecute(overbuildAction, player, game)) {
                            overbuildActions.add(overbuildAction);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Producer type not configured, skip
                }
            }
        }
        
        return overbuildActions;
    }
    
    // ========== Building Actions ==========
    
    /**
     * Generate all possible BuildShipyard actions.
     * Tries to build shipyards of level 1-3.
     */
    private List<Action> generateBuildShipyardActions(Player player, Game game) {
        List<Action> shipyardActions = new ArrayList<>();
        
        for (int level = 1; level <= 3; level++) {
            Action.BuildShipyard buildAction = new Action.BuildShipyard(level);
            
            if (ActionValidator.canExecute(buildAction, player, game)) {
                shipyardActions.add(buildAction);
            }
        }
        
        return shipyardActions;
    }
    
    /**
     * Generate all possible BuildShips actions.
     * For each shipyard level, generates build actions for both ship types.
     */
    private List<Action> generateBuildShipsActions(Player player, Game game) {
        List<Action> shipActions = new ArrayList<>();
        
        // For each shipyard level
        for (int level = 1; level <= 3; level++) {
            for (ShipType shipType : ShipType.values()) {
                // Try building 1-3 ships
                for (int amount = 1; amount <= 3; amount++) {
                    Action.BuildShips buildAction = new Action.BuildShips(shipType, level, amount);
                    
                    if (ActionValidator.canExecute(buildAction, player, game)) {
                        shipActions.add(buildAction);
                    }
                }
            }
        }
        
        return shipActions;
    }
    
    // ========== Resident Actions ==========
    
    /**
     * Generate all possible SettleResident actions.
     * Tries to settle residents of level 1-5.
     */
    private List<Action> generateSettleResidentActions(Player player, Game game) {
        List<Action> settleActions = new ArrayList<>();
        
        for (int level = 1; level <= 5; level++) {
            Action.SettleResident settleAction = new Action.SettleResident(level);
            
            if (ActionValidator.canExecute(settleAction, player, game)) {
                settleActions.add(settleAction);
            }
        }
        
        return settleActions;
    }
    
    /**
     * Generate all possible UpgradeResident actions.
     * Tries all combinations of 1-3 residents that can be upgraded together.
     */
    private List<Action> generateUpgradeResidentActions(Player player, Game game) {
        List<Action> upgradeActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        List<Resident> residents = board.getResidents();
        
        // Get all residents that could potentially be upgraded (level 1-4, FIT status)
        List<Resident> upgradableResidents = new ArrayList<>();
        for (Resident resident : residents) {
            if (resident.getPopulationLevel() < 5 && 
                resident.getStatus() == ResidentStatus.FIT) {
                upgradableResidents.add(resident);
            }
        }
        
        // Generate actions for 1, 2, or 3 residents
        for (int count = 1; count <= 3 && count <= upgradableResidents.size(); count++) {
            generateUpgradeCombinations(upgradableResidents, count, 0, new ArrayList<>(), 
                upgradeActions, player, game);
        }
        
        return upgradeActions;
    }
    
    /**
     * Helper method to generate all combinations of residents to upgrade.
     */
    private void generateUpgradeCombinations(List<Resident> allResidents, int count, 
            int start, List<Resident> current, List<Action> actions, Player player, Game game) {
        if (current.size() == count) {
            Resident[] residentArray = current.toArray(new Resident[0]);
            Action.UpgradeResident upgradeAction = new Action.UpgradeResident(residentArray);
            
            if (ActionValidator.canExecute(upgradeAction, player, game)) {
                actions.add(upgradeAction);
            }
            return;
        }
        
        for (int i = start; i < allResidents.size(); i++) {
            current.add(allResidents.get(i));
            generateUpgradeCombinations(allResidents, count, i + 1, current, actions, player, game);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Generate all possible SwapResidentCards actions.
     * Tries all combinations of 1-3 resident cards.
     */
    private List<Action> generateSwapResidentCardsActions(Player player, Game game) {
        List<Action> swapActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        List<ResidentCard> cards = board.getResidentCards();
        
        // Generate actions for swapping 1, 2, or 3 cards
        for (int count = 1; count <= 3 && count <= cards.size(); count++) {
            generateSwapCombinations(cards, count, 0, new ArrayList<>(), swapActions, player, game);
        }
        
        return swapActions;
    }
    
    /**
     * Helper method to generate all combinations of cards to swap.
     */
    private void generateSwapCombinations(List<ResidentCard> allCards, int count, 
            int start, List<ResidentCard> current, List<Action> actions, Player player, Game game) {
        if (current.size() == count) {
            ResidentCard[] cardArray = current.toArray(new ResidentCard[0]);
            Action.SwapResidentCards swapAction = new Action.SwapResidentCards(cardArray);
            
            if (ActionValidator.canExecute(swapAction, player, game)) {
                actions.add(swapAction);
            }
            return;
        }
        
        for (int i = start; i < allCards.size(); i++) {
            current.add(allCards.get(i));
            generateSwapCombinations(allCards, count, i + 1, current, actions, player, game);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Generate all possible FulfillNeeds actions.
     * For each resident card, check if needs can be fulfilled.
     */
    private List<Action> generateFulfillNeedsActions(Player player, Game game) {
        List<Action> fulfillActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        for (ResidentCard card : board.getResidentCards()) {
            // Get the required goods from the card
            Goods[] requiredGoods = card.needs();
            Action.FulfillNeeds fulfillAction = new Action.FulfillNeeds(card, requiredGoods);
            
            if (ActionValidator.canExecute(fulfillAction, player, game)) {
                fulfillActions.add(fulfillAction);
            }
        }
        
        return fulfillActions;
    }
    
    // ========== Worker Actions ==========
    
    /**
     * Generate all possible AssignWorker actions.
     * For each factory with empty slots and each available resident.
     */
    private List<Action> generateAssignWorkerActions(Player player, Game game) {
        List<Action> assignActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        // Get all FIT residents
        List<Resident> fitResidents = new ArrayList<>();
        for (Resident resident : board.getResidents()) {
            if (resident.getStatus() == ResidentStatus.FIT) {
                fitResidents.add(resident);
            }
        }
        
        // For each BUILT factory with empty slots (only iterate up to numFactories)
        Factory[] allFactories = board.getFactories();
        int numFactories = board.getNumFactories();
        
        for (int i = 0; i < numFactories; i++) {
            Factory factory = allFactories[i];
            if (factory == null) continue;
            
            // Check slot 1
            if (factory.getSlot1() == null) {
                for (Resident resident : fitResidents) {
                    Action.AssignWorker assignAction = new Action.AssignWorker(factory, resident, 1);
                    
                    if (ActionValidator.canExecute(assignAction, player, game)) {
                        assignActions.add(assignAction);
                    }
                }
            }
            
            // Check slot 2
            if (factory.getSlot2() == null) {
                for (Resident resident : fitResidents) {
                    Action.AssignWorker assignAction = new Action.AssignWorker(factory, resident, 2);
                    
                    if (ActionValidator.canExecute(assignAction, player, game)) {
                        assignActions.add(assignAction);
                    }
                }
            }
        }
        
        return assignActions;
    }
    
    /**
     * Generate all possible ExhaustWorker actions.
     * For each FIT resident.
     */
    private List<Action> generateExhaustWorkerActions(Player player, Game game) {
        List<Action> exhaustActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        for (Resident resident : board.getResidents()) {
            if (resident.getStatus() == ResidentStatus.FIT) {
                Action.ExhaustWorker exhaustAction = new Action.ExhaustWorker(resident);
                
                if (ActionValidator.canExecute(exhaustAction, player, game)) {
                    exhaustActions.add(exhaustAction);
                }
            }
        }
        
        return exhaustActions;
    }
    
    /**
     * Generate all possible DoOvertime actions.
     * For each EXHAUSTED or AT_WORK resident.
     */
    private List<Action> generateDoOvertimeActions(Player player, Game game) {
        List<Action> overtimeActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        for (Resident resident : board.getResidents()) {
            if (resident.getStatus() == ResidentStatus.EXHAUSTED || 
                resident.getStatus() == ResidentStatus.AT_WORK) {
                Action.DoOvertime overtimeAction = new Action.DoOvertime(resident.getPopulationLevel());
                
                if (ActionValidator.canExecute(overtimeAction, player, game)) {
                    overtimeActions.add(overtimeAction);
                    break; // Only add once per level
                }
            }
        }
        
        return overtimeActions;
    }
    
    // ========== Production Actions ==========
    
    /**
     * Generate all possible ProduceGoods actions.
     * For each factory with assigned workers.
     */
    private List<Action> generateProduceGoodsActions(Player player, Game game) {
        List<Action> produceActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        // Only check BUILT factories (only iterate up to numFactories)
        Factory[] allFactories = board.getFactories();
        int numFactories = board.getNumFactories();
        
        for (int i = 0; i < numFactories; i++) {
            Factory factory = allFactories[i];
            if (factory == null) continue;
            
            // Check if factory has at least one worker
            if (factory.getSlot1() != null || factory.getSlot2() != null) {
                Action.ProduceGoods produceAction = new Action.ProduceGoods(factory);
                
                if (ActionValidator.canExecute(produceAction, player, game)) {
                    produceActions.add(produceAction);
                }
            }
        }
        
        return produceActions;
    }
    
    /**
     * Generate all possible TradeGoods actions.
     * For each good that can be traded from other players.
     */
    private List<Action> generateTradeGoodsActions(Player player, Game game) {
        List<Action> tradeActions = new ArrayList<>();
        
        // For each other player
        Player[] allPlayers = game.getPlayers();
        for (int i = 0; i < allPlayers.length; i++) {
            if (allPlayers[i] == player) continue; // Skip self
            
            // Try trading each type of good
            for (Goods good : Goods.values()) {
                Action.TradeGoods tradeAction = new Action.TradeGoods(good, i);
                
                if (ActionValidator.canExecute(tradeAction, player, game)) {
                    tradeActions.add(tradeAction);
                }
            }
        }
        
        return tradeActions;
    }
    
    /**
     * Generate all possible ImportGood actions.
     * For each good that can be imported from New World.
     */
    private List<Action> generateImportGoodActions(Player player, Game game) {
        List<Action> importActions = new ArrayList<>();
        
        // New World goods: CACAO, SUGARCANE, TOBACCO, COFFEE_BEANS, COTTON, RUBBER
        Goods[] newWorldGoods = {
            Goods.CACAO, Goods.SUGARCANE, Goods.TOBACCO, 
            Goods.COFFEE_BEANS, Goods.COTTON, Goods.RUBBER
        };
        
        for (Goods good : newWorldGoods) {
            Action.ImportGood importAction = new Action.ImportGood(good);
            
            if (ActionValidator.canExecute(importAction, player, game)) {
                importActions.add(importAction);
            }
        }
        
        return importActions;
    }
    
    // ========== Exploration Actions ==========
    
    /**
     * Generate DiscoverOldWorldIsland action.
     */
    private List<Action> generateDiscoverOldWorldIslandActions(Player player, Game game) {
        List<Action> discoverActions = new ArrayList<>();
        
        Action.DiscoverOldWorldIsland discoverAction = new Action.DiscoverOldWorldIsland();
        
        if (ActionValidator.canExecute(discoverAction, player, game)) {
            discoverActions.add(discoverAction);
        }
        
        return discoverActions;
    }
    
    /**
     * Generate DiscoverNewWorldIsland action.
     */
    private List<Action> generateDiscoverNewWorldIslandActions(Player player, Game game) {
        List<Action> discoverActions = new ArrayList<>();
        
        Action.DiscoverNewWorldIsland discoverAction = new Action.DiscoverNewWorldIsland();
        
        if (ActionValidator.canExecute(discoverAction, player, game)) {
            discoverActions.add(discoverAction);
        }
        
        return discoverActions;
    }
    
    /**
     * Generate Expedition action.
     */
    private List<Action> generateExpeditionActions(Player player, Game game) {
        List<Action> expeditionActions = new ArrayList<>();
        
        Action.Expedition expeditionAction = new Action.Expedition();
        
        if (ActionValidator.canExecute(expeditionAction, player, game)) {
            expeditionActions.add(expeditionAction);
        }
        
        return expeditionActions;
    }
    
    // ========== Special Actions ==========
    
    /**
     * Generate Carneval action.
     */
    private List<Action> generateCarnevalActions(Player player, Game game) {
        List<Action> carnevalActions = new ArrayList<>();
        
        Action.Carneval carnevalAction = new Action.Carneval();
        
        if (ActionValidator.canExecute(carnevalAction, player, game)) {
            carnevalActions.add(carnevalAction);
        }
        
        return carnevalActions;
    }
    
    /**
     * Generate all possible DrawResidentCard actions.
     * For levels 1-5.
     */
    private List<Action> generateDrawResidentCardActions(Player player, Game game) {
        List<Action> drawActions = new ArrayList<>();
        
        for (int level = 1; level <= 5; level++) {
            Action.DrawResidentCard drawAction = new Action.DrawResidentCard(level);
            
            if (ActionValidator.canExecute(drawAction, player, game)) {
                drawActions.add(drawAction);
            }
        }
        
        return drawActions;
    }
    
    /**
     * Generate all possible ActivateReward actions.
     * For each pending reward the player has.
     */
    private List<Action> generateActivateRewardActions(Player player, Game game) {
        List<Action> rewardActions = new ArrayList<>();
        PlayerBoard board = player.getPlayerBoard();
        
        for (Reward reward : board.getPendingRewards()) {
            Action.ActivateReward activateAction = new Action.ActivateReward(reward);
            
            if (ActionValidator.canExecute(activateAction, player, game)) {
                rewardActions.add(activateAction);
            }
            
            // If reward is FreeGoodsChoice, also generate ChooseGoods actions
            if (reward instanceof Reward.FreeGoodsChoice freeGoodsChoice) {
                Goods[] availableGoods = freeGoodsChoice.options();
                for (Goods good : availableGoods) {
                    Action.ChooseGoods chooseAction = new Action.ChooseGoods(freeGoodsChoice, good);
                    
                    if (ActionValidator.canExecute(chooseAction, player, game)) {
                        rewardActions.add(chooseAction);
                    }
                }
            }
        }
        
        return rewardActions;
    }
    
    /**
     * Generate ViewResidentCards action.
     * This is always available as a free action.
     */
    private List<Action> generateViewResidentCardsActions(Player player, Game game) {
        List<Action> viewActions = new ArrayList<>();
        
        Action.ViewResidentCards viewAction = new Action.ViewResidentCards();
        
        if (ActionValidator.canExecute(viewAction, player, game)) {
            viewActions.add(viewAction);
        }
        
        return viewActions;
    }

    // ========== ObjectiveCard Free Actions ==========

    /**
     * Generate UseExtraAction if the ExtraAction ObjectiveCard is active and requirements are met.
     */
    private List<Action> generateUseExtraActionActions(Player player, Game game) {
        List<Action> actions = new ArrayList<>();
        Action.UseExtraAction action = new Action.UseExtraAction();
        if (ActionValidator.canExecute(action, player, game)) {
            actions.add(action);
        }
        return actions;
    }

    /**
     * Generate DiscardResidentCardAction for each card in hand,
     * if the DiscardResidentCard ObjectiveCard is active and requirements are met.
     */
    private List<Action> generateDiscardResidentCardActions(Player player, Game game) {
        List<Action> actions = new ArrayList<>();
        for (com.anno1800.game.cards.ResidentCard card : player.getPlayerBoard().getResidentCards()) {
            Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(card);
            if (ActionValidator.canExecute(action, player, game)) {
                actions.add(action);
            }
        }
        return actions;
    }

    /**
     * Generate InvestorGoldAction for each eligible (FIT) Investor,
     * if the InvestorExhaustForGold ObjectiveCard is active and requirements are met.
     */
    private List<Action> generateInvestorGoldActions(Player player, Game game) {
        List<Action> actions = new ArrayList<>();
        for (com.anno1800.game.residents.Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == 5) {
                Action.InvestorGoldAction action = new Action.InvestorGoldAction(resident);
                if (ActionValidator.canExecute(action, player, game)) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }
}
