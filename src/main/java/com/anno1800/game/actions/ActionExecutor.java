package com.anno1800.game.actions;

import com.anno1800.game.actions.actions.*;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Central executor for all game actions.
 * Routes actions to their appropriate implementation classes.
 */
public class ActionExecutor {

    /**
     * Executes the given action for the specified player in the game context.
     * 
     * @param action The action to execute
     * @param player The player performing the action
     * @param game The current game state
     */
    public static void executeAction(Action action, Player player, Game game) {
        switch (action) {
            case Action.Expedition expedition -> 
                Expedition.expedition(player, game, expedition);
                
            case Action.FulfillNeeds fulfillNeeds -> 
                FulfillNeeds.fulfillNeeds(player, game, fulfillNeeds);
                
            case Action.SettleResident settleResident -> 
                SettleResident.settleResident(player, game, settleResident);
                
            case Action.SwapResidentCards swapResidentCards -> 
                SwapResidentCards.swapResidentCards(player, swapResidentCards.cardsToSwap());
                
            case Action.UpgradeResident upgradeResident -> 
                UpgradeResident.upgradeResident(player, upgradeResident.amount(), upgradeResident.residentLevel());
                
            case Action.DiscoverOldWorldIsland discoverOldWorldIsland -> 
                DiscoverOldWorldIsland.discoverOldWorldIsland(player, game);
                
            case Action.DiscoverNewWorldIsland discoverNewWorldIsland -> 
                DiscoverNewWorldIsland.discoverNewWorldIsland(player, game);
                
            case Action.DemolishFactory demolishFactory -> 
                DemolishFactory.demolishFactory(player, game.getBoard(), demolishFactory.factory());
                
            case Action.OverbuildDefaultFactory overbuildDefaultFactory -> 
                OverbuildDefaultFactory.overbuildDefaultFactory(player, game.getBoard(), 
                    overbuildDefaultFactory.defaultFactory(), overbuildDefaultFactory.newFactory());
            
            // Add other actions as needed
            default -> throw new UnsupportedOperationException("Action not implemented: " + action.getClass().getSimpleName());
        }
    }
}