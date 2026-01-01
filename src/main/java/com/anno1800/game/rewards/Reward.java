package com.anno1800.game.rewards;

import com.anno1800.game.tiles.Factory;
import com.anno1800.data.gamedata.Goods;

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
     * Reward: Upgrade residents
     */
    record UpgradeResidents(int amount, int populationLevel1, int populationLevel2) implements Reward { }
    
    /**
     * Reward: Extra action in this turn
     */
    record ExtraAction() implements Reward { }

    /**
     * Reward: 2 ExpeditionCards
     */
    record ExpeditionCards() implements Reward { }
    
    /**
     * Reward: Free goods - player can choose 1 out of N options
     * The actual choice is made when the reward is executed.
     * 
     * @param options Array of goods to choose from
     * @param chosenGood The good chosen by the player/agent (null if not chosen yet)
     */
    record FreeGoodsChoice(Goods[] options, Goods chosenGood) implements Reward {
        // Konstruktor für initiale Erstellung (ohne Wahl)
        public FreeGoodsChoice(Goods[] options) {
            this(options, null);
        }
        
        // Methode um eine Wahl zu treffen
        public FreeGoodsChoice withChoice(Goods chosen) {
            if (chosenGood != null) {
                throw new IllegalStateException("Goods already chosen: " + chosenGood);
            }
            for (Goods option : options) {
                if (option.equals(chosen)) {
                    return new FreeGoodsChoice(options, chosen);
                }
            }
            throw new IllegalArgumentException("Invalid choice: " + chosen + ". Must be one of: " + java.util.Arrays.toString(options));
        }
        
        // Prüft ob eine Wahl getroffen wurde
        public boolean hasChoice() {
            return chosenGood != null;
        }
    }
    
    /**
     * Reward: Trade points
     */
    record TradePoints(int points) implements Reward { }
    
    /**
     * Reward: Expedition points
     */
    record ExplorationPoints(int points) implements Reward { }

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

    /**
     * Reward: build a factory on a free land tile.
     * This reward only occurs when a player has drawn an "Alte Welt Tile" card immediately before.
     * The factory will be built on one of the player's free land tiles.
     * 
     * @param factoryType The type of factory to build (null if player can choose)
     */
    record BuildFactory(Factory factoryType) implements Reward { }
}
