package com.anno1800.actions;

import com.anno1800.Boardtiles.Factory;
import com.anno1800.Rewards.Reward;
import com.anno1800.cards.ResidentCard;
import com.anno1800.data.Goods;
import com.anno1800.residents.Resident;
import com.anno1800.Boardtiles.Island;

/**
 * Represents all possible player actions in the game.
 * Sealed interface ensures all action types are known at compile time.
 * All permitted subtypes are defined as nested records within this interface.
 */
public sealed interface Action permits 
    Action.BuildFactory, Action.BuildShipyard, Action.BuildShips,
    Action.FulfillNeeds, Action.FulfillNeed, Action.UpgradeResident, 
    Action.SettleResident, Action.DiscoverIsland, Action.Expedition, 
    Action.Carneval, Action.AssignWorker, Action.ProduceGoods, 
    Action.TradeGoods, Action.DrawResidentCard {
    
    /**
     * Action: Build a factory
     * 
     * @param factory The factory to build
     */
    record BuildFactory(Factory factory) implements Action { }

    /**
     * Action: Build Shipyard
     * 
     * @param level The Level of the Shipyard to build
     */
    record BuildShipyard(int level) implements Action { }

    /**
     * Action: Build ships
     * 
     * @param shipType The type of ship to build (ExplorerShip or TradeShip)
     * @param level The level of the ship to build (1-3)
     * @param amount The number of ships to build
     * Für jede Werft kann ein Schiff gebaut werden, welches jeweils <= der Stufe der Werft ist.
    */
   record BuildShips(com.anno1800.player.PlayerBoard.ShipType shipType, int level, int amount) implements Action { }
   
   /**
    * Action: Fulfill a resident needs
    * 
    * @param residentCard The residentCard whose need to fulfill
    * @param goods The goods to provide
   */
  record FulfillNeeds(ResidentCard residentCard, Goods[] goods) implements Action { }
  /**
   * Action: Activate Reward of a resident card
    * 
    * @param resident The Reward
    */
   record FulfillNeed(Reward reward) implements Action { }
   /**
    * Action: Upgrade up to 3 residents to the next level
    * 
    * @param resident The resident to upgrade
   */
  record UpgradeResident(int[] amount, int[] residentLevel) implements Action { }
  /**
   * Action: Settle a new resident
   * 
   * @param level The population level of the resident
   */
  record SettleResident(int level) implements Action { }

   /**
    * Action: Discover a new island (old world or new world)
    * 
    * @param type The type of island to discover
    */
   record DiscoverIsland(Island type) implements Action { }

   /**
    * Action: Do an Expedition
    */
   record Expedition() implements Action { }

   /**
    * Action: Have a Carneval
    */
   record Carneval() implements Action { }
   
    /**
     * Action: Assign a resident to work in a factory
     * 
     * @param factory The factory to assign the worker to
     * @param resident The resident to assign
     * @param slot The slot number (1 or 2)
     */
    record AssignWorker(Factory factory, Resident resident, int slot) implements Action { }
    
    /**
     * Action: Produce goods in a factory
     * 
     * @param factory The factory to produce in
     */
    record ProduceGoods(Factory factory) implements Action { }
    
    /**
     * Action: Trade a good from a player
     * 
     * @param good The good to trade
     * @param player The Player to trade from
     */
    record TradeGoods(Goods good, int player) implements Action { }

    /**
     * Action: End the current phase
     */
    record EndCurrentPhase() implements Action { }

    /**
     * Action: Draw a resident card
     */
    record DrawResidentCard() implements Action { }
}
