package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;

/**
 * Validates production and trade-related actions.
 */
public class TradeGoodsValidator {

    /**
     * Validates TradeGoods action.
     * Requirements:
     * - At least one different player (Mitspieler) must have a factory that
     * produces the requested good
     * - The trading player must have enough availableTradeChips to cover the trade
     * costs
     * - The trade costs are determined by the factory's tradeCosts value
     */
    public static boolean canTradeGoods(Action.TradeGoods action, Player player, Game game) {
        // Check all other players to find the one with the cheapest factory that produces the
        // requested good
        Factory cheapestFactory = null;
        int lowestTradeCosts = Integer.MAX_VALUE;

        for (Player otherPlayer : game.getPlayers()) {
            // Skip the current player (cannot trade with yourself)
            if (otherPlayer == player) {
                continue;
            }

            // Check if this player has a factory that produces the requested good
            for (Producer producer : otherPlayer.getPlayerBoard().getFactories()) {
                if (producer instanceof Factory factory) {
                    if (factory != null && factory.produces() == action.good()) {
                        int tradeCosts = factory.getTradeCosts();
                        // Keep track of the cheapest factory
                        if (tradeCosts < lowestTradeCosts) {
                            cheapestFactory = factory;
                            lowestTradeCosts = tradeCosts;
                        }
                    }
                }
            }
        }

        // At least one other player must have a factory that produces this good
        if (cheapestFactory == null) {
            return false;
        }

        // Check if player has enough trade chips to cover the trade costs of the cheapest option
        if (player.getPlayerBoard().getAvailableTradeChips() < lowestTradeCosts) {
            return false;
        }

        return true;
    }
}
