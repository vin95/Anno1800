package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.player.Player;
import com.anno1800.game.rewards.Reward;

/**
 * Action: Choose a good from available options (for FreeGoodsChoice reward)
 */
public class ChooseGoods {
    
    /**
     * Creates a new FreeGoodsChoice reward with the chosen good.
     * This method validates that the chosen good is one of the available options.
     * 
     * @param player The player making the choice (for potential future validation)
     * @param reward The original FreeGoodsChoice reward 
     * @param chosenGood The good chosen by the player/agent
     * @return A new FreeGoodsChoice reward with the chosen good
     * @throws IllegalArgumentException if the chosen good is not in the options
     * @throws IllegalStateException if a choice was already made
     */
    public static Reward.FreeGoodsChoice chooseGoods(Player player, Reward.FreeGoodsChoice reward, Goods chosenGood) {
        return reward.withChoice(chosenGood);
    }
}