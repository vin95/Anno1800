package com.anno1800.game.actions;

import com.anno1800.game.actions.validators.*;

import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Main entry point for action validation.
 * Delegates to specialized validator classes for better organization.
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
            case Action.BuildFactory buildFactory -> BuildFactoryValidator.canBuildFactory(buildFactory, player, game);
            case Action.BuildShipyard buildShipyard -> BuildShipyardValidator.canBuildShipyard(buildShipyard, player, game);
            case Action.BuildShips buildShips -> BuildShipsValidator.canBuildShips(buildShips, player, game);
            case Action.FulfillNeeds fulfillNeeds -> FullfillNeedsValidator.canFullfillNeeds(fulfillNeeds, player, fulfillNeeds.residentCard(), game);
            case Action.SwapResidentCards swapResidentCards -> SwapResidentCardsValidator.canSwapResidentCards(swapResidentCards, swapResidentCards.cardsToSwap(), player, game);
            case Action.SettleResident settleResident -> SettleResidentValidator.canSettleResident(settleResident, player, game);
            case Action.UpgradeResident upgradeResident -> UpgradeResidentValidator.canUpgradeResident(upgradeResident, player, game);
            case Action.DiscoverOldWorldIsland discoverOldWorldIsland ->
                DiscoverOldWorldIslandValidator.canDiscoverOldWorldIsland(discoverOldWorldIsland, player, game);
            case Action.DiscoverNewWorldIsland discoverNewWorldIsland ->
                DiscoverNewWorldIslandValidator.canDiscoverNewWorldIsland(discoverNewWorldIsland, player, game);
            case Action.Expedition expedition -> ExpeditionValidator.canExpedition(expedition, player, game);
            case Action.Carneval carneval -> CarnevalValidator.canCarneval(carneval, player, game);

            case Action.DoOvertime doOvertime -> DoOvertimeValidator.canDoOvertime(doOvertime, player, game);
            case Action.ProduceGoods produceGoods -> ProduceGoodsValidator.canProduceGoods(produceGoods, player, game);
            case Action.TradeGoods tradeGoods -> TradeGoodsValidator.canTradeGoods(tradeGoods, player, game);
            case Action.ActivateReward activateReward -> ActivateRewardValidator.canActivateReward(activateReward, player, game);

            case Action.AssignWorker assignWorker -> AssignWorkerValidator.canAssignWorker(assignWorker, player, game);
            case Action.ExhaustWorker exhaustWorker -> ExhaustWorkerValidator.canExhaustWorker(exhaustWorker, player, game);
            case Action.DrawResidentCard drawCard -> DrawResidentCardValidator.canDrawResidentCard(drawCard, player, game);
            case Action.ImportGood importGood -> ImportGoodValidator.canImportGood(importGood, player, game);
        };
    }
}
