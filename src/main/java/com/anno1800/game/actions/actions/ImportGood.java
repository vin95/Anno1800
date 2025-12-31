package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.player.Player;
/**
 * Action to import a good using trade chips.
 */
public class ImportGood {
    public static Goods importGood(Player player, Goods good) {
        player.getPlayerBoard().reduceAvailableTradeChips(1);
        return good;
    }
}
