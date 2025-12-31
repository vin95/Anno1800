package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Trade goods: export one good and import another.
 */
public class TradeGoods {
    public static Goods tradeGoods(Player player, Goods good, int playerId, Game game) {
        // Check all other players to find the one with the cheapest factory that
        // produces the
        // requested good
        Factory cheapestFactory = null;
        int lowestTradeCosts = Integer.MAX_VALUE;
        Player tradePartner = game.getPlayers()[0]; // Placeholder initialization

        for (Player otherPlayer : game.getPlayers()) {
            // Skip the current player (cannot trade with yourself)
            if (otherPlayer == player) {
                continue;
            }

            // Check if this player has a factory that produces the requested good
            for (Producer producer : otherPlayer.getPlayerBoard().getFactories()) {
                if (producer instanceof Factory factory) {
                    if (factory != null && factory.produces() == good) {
                        int tradeCosts = factory.getTradeCosts();
                        // Keep track of the cheapest factory
                        if (tradeCosts < lowestTradeCosts) {
                            cheapestFactory = factory;
                            lowestTradeCosts = tradeCosts;
                            tradePartner = otherPlayer;
                        }
                    }
                }
            }
        }
        tradePartner.getPlayerBoard().earnGold(lowestTradeCosts);
        player.getPlayerBoard().reduceAvailableTradeChips(lowestTradeCosts);
        return cheapestFactory.produces();
    }
}
