package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Handler for the UseExtraAction free action.
 * 
 * Rule from ObjectiveCard: "Bezahle 3 Gold und drei Expeditionschips 
 * um eine weitere Aktion in dieser Runde machen zu können. Geht nur 1x pro Zug"
 * 
 * This is a free action that:
 * - Deducts 3 Gold from the player
 * - Deducts 3 Explorer Chips from the player
 * - Allows the player to take an additional action this turn
 * - Marks the action as used for this turn
 */
public class UseExtraAction {

    /**
     * Executes the UseExtraAction.
     * 
     * @param player The player performing the action
     * @param game The current game state
     * @param action The UseExtraAction action
     */
    public static void useExtraAction(Player player, Game game, Action.UseExtraAction action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Deduct 3 Gold
        playerBoard.reduceGold(3);
        
        // Deduct 3 Explorer Chips
        playerBoard.reduceAvailableExplorerChips(3);
        
        // Mark as used this turn
        playerBoard.markExtraActionUsed();
        
        // The extra action is granted by the game logic - this action is a "free" action
        // that allows the player to take another regular action
        System.out.println(player.getName() + " used Extra Action: Paid 3 Gold + 3 Explorer Chips for an additional action!");
    }
}
