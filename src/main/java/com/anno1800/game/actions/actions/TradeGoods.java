package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Trade goods: Acquire a good from another player's factory.
 * 
 * Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden."
 * Each good can only be traded once per turn.
 */
public class TradeGoods {
    public static Goods tradeGoods(Player player, Goods good, int playerId, Game game) {
        Player[] players = game.getPlayers();
        if (playerId < 0 || playerId >= players.length) {
            throw new IllegalArgumentException("Invalid trade partner index: " + playerId);
        }

        Player tradePartner = players[playerId];
        if (tradePartner == player) {
            throw new IllegalArgumentException("Cannot trade with self");
        }

        // Use only the selected trade partner and find the cheapest matching factory there.
        Factory cheapestFactory = null;
        int lowestTradeCosts = Integer.MAX_VALUE;

        for (Factory factory : tradePartner.getPlayerBoard().getAllActiveFactories()) {
            if (factory != null && factory.produces() == good) {
                int tradeCosts = factory.getTradeCosts();
                if (tradeCosts < lowestTradeCosts) {
                    cheapestFactory = factory;
                    lowestTradeCosts = tradeCosts;
                }
            }
        }

        if (cheapestFactory == null) {
            throw new IllegalStateException(
                "Selected trade partner cannot provide good: " + good + " (partner=" + tradePartner.getName() + ")"
            );
        }
        
        // Execute the trade
        tradePartner.getPlayerBoard().earnGold(lowestTradeCosts);
        player.getPlayerBoard().reduceAvailableTradeChips(lowestTradeCosts);
        
        // Register the traded good so it cannot be traded again this turn
        // Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden."
        player.getPlayerBoard().registerTradedGood(good);
        
        System.out.println("Traded " + good + " from " + tradePartner.getName() + 
            " for " + lowestTradeCosts + " trade chips. (Cannot trade " + good + " again this turn)");
        
        return cheapestFactory.produces();
    }
}
