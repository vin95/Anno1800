package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;

import java.util.Set;

/**
 * Validates production and trade-related actions.
 */
public class TradeGoodsValidator {

    /**
     * New World resources that cannot be traded from other players.
     * Rule: "Neue-Welt-Ressourcen von Mitspielern können nicht erhandelt werden."
     */
    private static final Set<Goods> NEW_WORLD_RESOURCES = Set.of(
        Goods.CACAO,
        Goods.SUGARCANE,
        Goods.TOBACCO,
        Goods.COFFEE_BEANS,
        Goods.COTTON,
        Goods.RUBBER
    );

    /**
     * Validates TradeGoods action.
     * Requirements:
     * - The good must NOT be a New World resource 
     *   (Rule: "Neue-Welt-Ressourcen von Mitspielern können nicht erhandelt werden.")
     * - The good must not have been traded already this turn
     *   (Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden.")
     * - At least one different player (Mitspieler) must have a factory that
     *   produces the requested good
     * - The trading player must have enough availableTradeChips to cover the trade
     *   costs
     * - The trade costs are determined by the factory's tradeCosts value
     */
    public static boolean canTradeGoods(Action.TradeGoods action, Player player, Game game) {
        // Check if this is a New World resource (cannot be traded)
        // Rule: "Neue-Welt-Ressourcen von Mitspielern können nicht erhandelt werden."
        if (NEW_WORLD_RESOURCES.contains(action.good())) {
            return false;
        }
        
        // Check if this good has already been traded this turn
        // Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden."
        if (player.getPlayerBoard().hasAlreadyTradedThisTurn(action.good())) {
            return false;
        }

        int partnerIndex = action.player();
        Player[] players = game.getPlayers();
        if (partnerIndex < 0 || partnerIndex >= players.length) {
            return false;
        }

        Player selectedPartner = players[partnerIndex];
        if (selectedPartner == player) {
            return false;
        }

        // Check only the selected partner and find the cheapest matching factory there.
        Factory cheapestFactory = null;
        int lowestTradeCosts = Integer.MAX_VALUE;
        for (Factory factory : selectedPartner.getPlayerBoard().getAllActiveFactories()) {
            if (factory != null && factory.produces() == action.good()) {
                int tradeCosts = factory.getTradeCosts();
                if (tradeCosts < lowestTradeCosts) {
                    cheapestFactory = factory;
                    lowestTradeCosts = tradeCosts;
                }
            }
        }

        // At least one other player must have a factory that produces this good
        if (cheapestFactory == null) {
            return false;
        }

        // Check if player has enough trade chips to cover the trade costs
        int availableTradeChips = player.getPlayerBoard().getAvailableTradeChips();
        
        // Check if ExplorerTrader objective card is active
        boolean explorerTraderActive = game.getBoard().getActiveObjectiveCards().stream()
            .anyMatch(card -> card instanceof ObjectiveCard.ExplorerTrader);
        
        if (availableTradeChips >= lowestTradeCosts) {
            return true; // Can use regular trade chips
        }
        
        // If ExplorerTrader is active, check if we can use explorer chips instead
        if (explorerTraderActive) {
            int availableExplorerChips = player.getPlayerBoard().getAvailableExplorerChips();
            int requiredExplorerChips = lowestTradeCosts * 2; // 2 explorer chips = 1 trade chip
            return availableExplorerChips >= requiredExplorerChips;
        }

        return false;
    }
}
