package com.anno1800.game.player;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Factory;

/**
 * Represents a good that has been produced or acquired, including its source.
 * Used in planning phase to track how goods would be obtained without actually executing the actions.
 */
public record ProducedGood(
    Goods good,
    GoodSource source
) {
    /**
     * Sealed interface representing how a good was obtained.
     */
    public sealed interface GoodSource {
        
        /**
         * Good was produced in a factory by assigning a resident.
         * 
         * @param factory The factory that produces this good
         * @param resident The resident that would be assigned (exhausted)
         */
        record Produced(Factory factory, Resident resident) implements GoodSource {}
        
        /**
         * Good was traded from another player.
         * 
         * @param fromPlayer The player index to trade from
         * @param chipCost The cost in trade chips based on traded factory color.
         */
        record Traded(int fromPlayer, int chipCost) implements GoodSource {}

        /**
         * Good was traded from another player but paid via ExplorerTrader with explorer chips.
         *
         * @param fromPlayer The player index to trade from
         * @param tradeChipCost Equivalent trade-chip cost of the selected factory
         * @param explorerChipCost Actual explorer chips paid (usually tradeChipCost * 2)
         */
        record TradedWithExplorer(int fromPlayer, int tradeChipCost, int explorerChipCost) implements GoodSource {}
        
        /**
         * Good was imported from the new world using a plantation.
         * 
         * @param explorerChip The explorer chip that would be used
         */
        record Imported(int explorerChip) implements GoodSource {}
        
        /**
         * Good was obtained from a reward (already in pending rewards).
         * No resources need to be consumed.
         */
        record FromReward() implements GoodSource {}
        
        /**
         * Good was obtained through other means (e.g., initial goods, special actions).
         */
        record Other(String description) implements GoodSource {}
    }
    
    @Override
    public String toString() {
        return good + " (" + sourceDescription() + ")";
    }
    
    private String sourceDescription() {
        return switch (source) {
            case GoodSource.Produced(var factory, var resident) -> 
                "produced in " + factory.getType() + " by resident L" + resident.getPopulationLevel();
            case GoodSource.Traded(var player, var chip) -> 
                "traded from Player " + (player + 1) + " using chip " + chip;
            case GoodSource.TradedWithExplorer(var player, var tradeChipCost, var explorerChipCost) ->
                "traded from Player " + (player + 1) + " using " + explorerChipCost
                    + " explorer chips (trade cost " + tradeChipCost + ")";
            case GoodSource.Imported(var chip) -> 
                "imported using chip " + chip;
            case GoodSource.FromReward() -> 
                "from reward";
            case GoodSource.Other(var desc) -> 
                desc;
        };
    }
}
