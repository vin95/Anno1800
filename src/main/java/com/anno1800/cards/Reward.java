package com.anno1800.cards;

import com.anno1800.data.Goods;

/**
 * Represents different types of rewards from resident cards.
 * Sealed interface ensures all reward types are known at compile time.
 */
public sealed interface Reward {
    
    /**
     * Reward: Additional residents
     */
    record NewResidents(int amount, int populationLevel) implements Reward { }
    
    /**
     * Reward: Extra action in this turn
     */
    record ExtraAction() implements Reward { }
    
    /**
     * Reward: Free goods
     */
    record FreeGoods(Goods good, int amount) implements Reward {
        public FreeGoods(Goods good) {
            this(good, 1);
        }
    }
    
    /**
     * Reward: Trade points
     */
    record TradePoints(int points) implements Reward { }
    
    /**
     * Reward: Expedition points
     */
    record ExpeditionPoints(int points) implements Reward { }

    /**
     * Reward: Gold
     */
    record Gold(int amount) implements Reward { }

    /**
     * Reward: Gold und Tradepoints
     */
    record GoldAndTradePoints(int goldAmount, int tradePoints) implements Reward { }

    /**
     * Reward: discard a residentCard
     */
    record DiscardResidentCard(int amount) implements Reward { 
        public DiscardResidentCard() {
            this(2);
        }
    }
}
