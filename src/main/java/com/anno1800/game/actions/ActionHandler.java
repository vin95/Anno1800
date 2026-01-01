package com.anno1800.game.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.ShipType;
import static com.anno1800.game.actions.actions.ImportGood.*;
import static com.anno1800.game.actions.actions.DrawResidentCard.*;
import static com.anno1800.game.actions.actions.ExhaustWorker.*;
import static com.anno1800.game.actions.actions.AssignWorker.*;
import static com.anno1800.game.actions.actions.ActivateReward.*;
import static com.anno1800.game.actions.actions.TradeGoods.*;
import static com.anno1800.game.actions.actions.ProduceGoods.*;
import static com.anno1800.game.actions.actions.DoOvertime.*;
import static com.anno1800.game.actions.actions.Carneval.*;
import static com.anno1800.game.actions.actions.Expedition.*;
import static com.anno1800.game.actions.actions.DiscoverNewWorldIsland.*;
import static com.anno1800.game.actions.actions.DiscoverOldWorldIsland.*;
import static com.anno1800.game.actions.actions.UpgradeResident.*;
import static com.anno1800.game.actions.actions.SettleResident.*;
import com.anno1800.game.actions.actions.FulfillNeeds;
import com.anno1800.game.actions.actions.Expedition;
import static com.anno1800.game.actions.actions.SwapResidentCards.*;
import static com.anno1800.game.actions.actions.FulfillNeeds.*;
import static com.anno1800.game.actions.actions.BuildShips.*;
import static com.anno1800.game.actions.actions.BuildShipyard.*;
import static com.anno1800.game.actions.actions.BuildFactory.*;
import static com.anno1800.game.actions.actions.ChooseGoods.*;

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
     * @return ActionResult containing the result of the action execution
     */
    public ActionResult execute(Action action, Player player) {
        return switch (action) {
            case Action.BuildFactory(Factory factory) -> {
                buildFactory(player, factory, game);
                yield new ActionResult.NoResult();
            }
            case Action.BuildShipyard(int level) -> {
                buildShipyard(player, level, game);
                yield new ActionResult.NoResult();
            }
            case Action.BuildShips(ShipType shipType, int level, int amount) -> {
                buildShips(player, shipType, level, amount, game);
                yield new ActionResult.NoResult();
            }
            case Action.FulfillNeeds(ResidentCard residentCard, Goods[] good) -> {
                // Create the action record and call the FulfillNeeds class
                var fulfillNeedsAction = new Action.FulfillNeeds(residentCard, good);
                FulfillNeeds.fulfillNeeds(player, game, fulfillNeedsAction);
                yield new ActionResult.NoResult();
            }
            case Action.SwapResidentCards(ResidentCard[] cardsToSwap) -> {
                swapResidentCards(player, cardsToSwap);
                yield new ActionResult.NoResult();
            }
            case Action.SettleResident(int level) -> {
                settleResident(player, level);
                yield new ActionResult.NoResult();
            }
            case Action.UpgradeResident(int[] amount, int[] residentLevel) -> {
                upgradeResident(player, amount, residentLevel);
                yield new ActionResult.NoResult();
            }
            case Action.DiscoverOldWorldIsland() -> {
                discoverOldWorldIsland(player, game);
                yield new ActionResult.NoResult();
            }
            case Action.DiscoverNewWorldIsland() -> {
                discoverNewWorldIsland(player, game);
                yield new ActionResult.NoResult();
            }
            case Action.Expedition() -> {
                // Create the action record and call the Expedition class
                var expeditionAction = new Action.Expedition();
                Expedition.expedition(player, game, expeditionAction);
                yield new ActionResult.NoResult();
            }
            case Action.Carneval() -> {
                carneval(player);
                yield new ActionResult.NoResult();
            }
            case Action.DoOvertime(int populationLevel) -> {
                doOvertime(player, populationLevel);
                yield new ActionResult.NoResult();
            }
            case Action.ProduceGoods(Factory factory) ->
                new ActionResult.GoodsResult(produceGoods(player, factory));
            case Action.TradeGoods(Goods good, int playerId) ->
                new ActionResult.GoodsResult(tradeGoods(player, good, playerId, game));
            case Action.ActivateReward(Reward reward) -> {
                activateReward(player, reward, game);
                yield new ActionResult.NoResult();
            }
            case Action.AssignWorker(Factory factory, Resident resident, int slot) -> {
                assignWorker(player, factory, resident, slot);
                yield new ActionResult.NoResult();
            }
            case Action.ExhaustWorker(Resident resident) -> {
                exhaustWorker(player, resident);
                yield new ActionResult.NoResult();
            }
            case Action.DrawResidentCard(int populationLevel) -> {
                drawResidentCard(player, populationLevel, game);
                yield new ActionResult.NoResult();
            }
            case Action.ImportGood(Goods good) ->
                new ActionResult.GoodsResult(importGood(player, good));
            case Action.ChooseGoods(Reward.FreeGoodsChoice reward, Goods chosenGood) ->
                new ActionResult.RewardResult(chooseGoods(player, reward, chosenGood));
            default -> throw new IllegalArgumentException("Unknown action type: " + action);
        };
    }
}
