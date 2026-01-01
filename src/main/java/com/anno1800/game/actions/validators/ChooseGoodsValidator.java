package com.anno1800.game.actions.validators;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.rewards.Reward;

/**
 * Validates ChooseGoods actions.
 */
public class ChooseGoodsValidator {

    /**
     * Validates if a ChooseGoods action can be performed.
     * 
     * @param action The ChooseGoods action to validate
     * @param player The player performing the action
     * @param game The current game state
     * @return true if the action is valid, false otherwise
     */
    public static boolean canChooseGoods(Action.ChooseGoods action, Player player, Game game) {
        Reward.FreeGoodsChoice reward = action.reward();
        Goods chosenGood = action.chosenGood();
        
        // Prüfe ob bereits eine Wahl getroffen wurde
        if (reward.hasChoice()) {
            return false;
        }
        
        // Prüfe ob die gewählte Ware zu den verfügbaren Optionen gehört
        for (Goods option : reward.options()) {
            if (option.equals(chosenGood)) {
                return true;
            }
        }
        
        return false;
    }
}