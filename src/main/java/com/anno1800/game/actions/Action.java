package com.anno1800.game.actions;

import com.anno1800.game.tiles.Factory;
import com.anno1800.game.rewards.Reward;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.residents.Resident;
import com.anno1800.data.gamedata.ShipType;

/**
 * Represents all possible player actions in the game.
 * Sealed interface ensures all action types are known at compile time.
 * All permitted subtypes are defined as nested records within this interface.
 */
public sealed interface Action permits
        Action.BuildFactory, Action.BuildShipyard, Action.BuildShips,
        Action.FulfillNeeds, Action.SwapResidentCards, Action.SettleResident,
        Action.UpgradeResident, Action.DiscoverOldWorldIsland, Action.DiscoverNewWorldIsland,
        Action.Expedition, Action.Carneval, Action.DoOvertime,
        Action.ProduceGoods, Action.TradeGoods, Action.ActivateReward,
        Action.AssignWorker, Action.ExhaustWorker, Action.DrawResidentCard, Action.ImportGood, Action.ChooseGoods,
        Action.DemolishFactory, Action.OverbuildDefaultFactory {

    /**
     * Action: Build a factory
     * 
     * @param factory The factory to build
     */
    record BuildFactory(Factory factory) implements Action {
    }

    /**
     * Action: Demolish a factory
     * 
     * @param factory The factory to demolish
     */
    record DemolishFactory(Factory factory) implements Action {
    }

    /**
     * Action: Overbuild a default factory with a new factory
     * 
     * @param defaultFactory The default factory to overbuild
     * @param newFactory The new factory to place
     */
    record OverbuildDefaultFactory(Factory defaultFactory, Factory newFactory) implements Action {
    }

    /**
     * Action: Build Shipyard
     * 
     * @param level The Level of the Shipyard to build
     */
    record BuildShipyard(int level) implements Action {
    }

    /**
     * Action: Build ships
     * 
     * @param shipType The type of ship to build (ExplorerShip or TradeShip)
     * @param level    The level of the ship to build (1-3)
     * @param amount   The number of ships to build
     *                 Für jede Werft kann ein Schiff gebaut werden, welches jeweils
     *                 <= der Stufe der Werft ist.
     */
    record BuildShips(ShipType shipType, int level, int amount) implements Action {
    }

    /**
     * Action: Fulfill a resident needs
     * 
     * @param residentCard The residentCard whose need to fulfill
     * @param goods        The goods the player provides to fulfill the needs
     */
    record FulfillNeeds(ResidentCard residentCard, Goods[] goods) implements Action {
    }

    /**
     * Action: swap resident cards
     */
    record SwapResidentCards(ResidentCard[] cardsToSwap) implements Action {
    }

    /**
     * Action: Settle up to 3 new resident
     * 
     * @param level The population level of the resident
     */
    record SettleResident(int level) implements Action {
    }

    /**
     * Action: Upgrade up to 3 residents to the next level
     * 
     * @param resident The resident to upgrade
     */
    record UpgradeResident(int[] amount, int[] residentLevel) implements Action {
    }

    /**
     * Action: Discover a new island of the old world
     */
    record DiscoverOldWorldIsland() implements Action {
    }

    /**
     * Action: Discover a new island of the new world
     */
    record DiscoverNewWorldIsland() implements Action {
    }

    /**
     * Action: Do an Expedition and take up to 3 Expedition cards
     */
    record Expedition() implements Action {
    }

    /**
     * Action: Have a Carneval
     */
    record Carneval() implements Action {
    }

    /**
     * Action: Do overtime with a resident
     * 
     * @param populationLevel The population level of the resident to do overtime
     */
    record DoOvertime(int populationLevel) implements Action {
    }

    /**
     * Action: Produce goods in a factory
     * 
     * @param factory The factory to produce in
     */
    record ProduceGoods(Factory factory) implements Action {
    }

    /**
     * Action: Trade a good from a player
     * 
     * @param good   The good to trade
     * @param player The Player to trade from
     */
    record TradeGoods(Goods good, int player) implements Action {
    }

    /**
     * Action: Activate Reward of a resident card
     * 
     * @param resident The Reward
     */
    record ActivateReward(Reward reward) implements Action {
    }

    /**
     * Action: Assign a resident to work in a factory
     * 
     * @param factory  The factory to assign the worker to
     * @param resident The resident to assign
     * @param slot     The slot number (1 or 2)
     */
    record AssignWorker(Factory factory, Resident resident, int slot) implements Action {
    }

    /**
     * Action: Exhaust a resident to provide the workforce
     * 
     * @param resident The resident to exhaust
     */
    record ExhaustWorker(Resident resident) implements Action {
    }

    /**
     * Action: Draw a resident card
     */
    record DrawResidentCard(int populationLevel) implements Action {
    }

    /**
     * Action: import a good from the new World
     */
    record ImportGood(Goods good) implements Action {
    }

    /**
     * Action: Choose a good from available options (for FreeGoodsChoice reward)
     * 
     * @param reward The FreeGoodsChoice reward to make a choice for
     * @param chosenGood The good chosen by the player/agent
     */
    record ChooseGoods(Reward.FreeGoodsChoice reward, Goods chosenGood) implements Action {
    }
}
