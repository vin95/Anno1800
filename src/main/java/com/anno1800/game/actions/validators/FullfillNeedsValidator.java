package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;
import com.anno1800.data.gamedata.Goods;

/**
 * Validates resident-related actions (settling, upgrading, fulfilling needs, swapping cards).
 */
public class FullfillNeedsValidator {

    /**
     * Validates FulfillNeeds action.
     * Requirements:
     * - Player must own the resident card
     * - The provided goods array must match or cover the card's needs
     * - All goods must be either producible or tradeable
     */
    public static boolean canFullfillNeeds(Action.FulfillNeeds action, Player player, ResidentCard residentCard, Game game) {
        // Check if player owns the resident card
        if (!player.getPlayerBoard().getResidentCards().contains(residentCard)) {
            return false;
        }
        
        Goods[] needs = residentCard.needs();
        Goods[] providedGoods = action.goods();
        
        // Check that provided goods match the needs
        if (providedGoods == null || providedGoods.length != needs.length) {
            return false;
        }
        
        // Check each provided good can be fulfilled (either produced or traded)
        for (Goods good : providedGoods) {
            boolean canFulfill = false;
            
            // Check if player can produce this good
            for (Producer producer : player.getPlayerBoard().getFactories()) {
                if (producer instanceof Factory factory) {
                    if (factory != null && factory.produces() == good) {
                        // Check if this factory can produce (has empty slot and FIT resident)
                        if (factory.getSlot1() == null || factory.getSlot2() == null) {
                            for (Resident resident : player.getPlayerBoard().getResidents()) {
                                if (resident.getPopulationLevel() == factory.populationLevel() &&
                                    resident.getStatus() == com.anno1800.game.residents.ResidentStatus.FIT) {
                                    canFulfill = true;
                                    break;
                                }
                            }
                        }
                        if (canFulfill) break;
                    }
                }
            }
            
            // If can't produce, check if can trade
            if (!canFulfill) {
                // Check if any other player can produce this good
                for (Player otherPlayer : game.getPlayers()) {
                    if (otherPlayer == player) continue;
                    
                    for (Producer producer : otherPlayer.getPlayerBoard().getFactories()) {
                        if (producer instanceof Factory factory) {
                            if (factory != null && factory.produces() == good) {
                                // Check if player has enough trade chips
                                if (player.getPlayerBoard().getAvailableTradeChips() >= factory.getTradeCosts()) {
                                    canFulfill = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (canFulfill) break;
                }
            }
            
            // If this good cannot be fulfilled, return false
            if (!canFulfill) {
                return false;
            }
        }
        
        return true;
    }
}
