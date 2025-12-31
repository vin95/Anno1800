package com.anno1800.data.gamedata;

import com.anno1800.game.rewards.Reward;
import com.anno1800.game.cards.ResidentCard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.anno1800.data.gamedata.Goods.*;

/**
 * Central data repository for all resident card configurations.
 * Defines population level requirements, needed goods, and rewards for each resident card.
 */
public class ResidentCardData {
    
    /**
     * Get all resident cards of a specific population level.
     * 
     * @param populationLevel The population level (2, 5, or 7)
     * @return List of all resident cards for this level
     * @throws IllegalArgumentException if level is not 2, 5, or 7
     */
    public static Deque<ResidentCard> getCardsForLevel(int populationLevel) {
        return switch (populationLevel) {
            case 2 -> getLevel2Cards();
            case 5 -> getLevel5Cards();
            case 7 -> getLevel7Cards();
            default -> throw new IllegalArgumentException("Invalid population level: " + populationLevel + ". Must be 2, 5, or 7.");
        };
    }
    
    /**
     * Level 2 Resident Cards (Workers)
     */
    private static Deque<ResidentCard> getLevel2Cards() {
        return new ArrayDeque<>(List.of(
            new ResidentCard(2, new Goods[]{WORK_CLOTHES, BREAD}, new Reward.NewResidents(2, 1)),
            new ResidentCard(2, new Goods[]{BRICKS, BEER}, new Reward.NewResidents(2, 1)),
            new ResidentCard(2, new Goods[]{SOAP, WORK_CLOTHES}, new Reward.NewResidents(2, 1)),

            new ResidentCard(2, new Goods[]{BRICKS, BREAD}, new Reward.NewResidents(2,2)),
            new ResidentCard(2, new Goods[]{SNAPS, CANNED_MEAT}, new Reward.NewResidents(2, 2)),
            new ResidentCard(2, new Goods[]{SAUSAGE, CANNED_MEAT}, new Reward.NewResidents(2, 2)),
            new ResidentCard(2, new Goods[]{SNAPS, BREAD}, new Reward.NewResidents(2, 2)),
            new ResidentCard(2, new Goods[]{BEER, SNAPS}, new Reward.NewResidents(2, 2)),
            new ResidentCard(2, new Goods[]{CANNED_MEAT, SOAP}, new Reward.NewResidents(2, 2)),

            new ResidentCard(2, new Goods[]{WORK_CLOTHES, GOODS}, new Reward.NewResidents(1, 3)),
            new ResidentCard(2, new Goods[]{CANNED_MEAT, BEER}, new Reward.NewResidents(1, 3)),
            new ResidentCard(2, new Goods[]{BEER, SAUSAGE}, new Reward.NewResidents(1, 3)),
            new ResidentCard(2, new Goods[]{GOODS, BEER}, new Reward.NewResidents(1, 3)),
            new ResidentCard(2, new Goods[]{CANNED_MEAT, SAUSAGE}, new Reward.NewResidents(1, 3)),
            new ResidentCard(2, new Goods[]{WORK_CLOTHES, CANNED_MEAT}, new Reward.NewResidents(1, 3)),
            new ResidentCard(2, new Goods[]{SAUSAGE, SNAPS}, new Reward.NewResidents(1, 3)),

            new ResidentCard(2, new Goods[]{SOAP, COTTON_FABRIC}, new Reward.NewResidents(1, 4)),
            new ResidentCard(2, new Goods[]{BREAD, BEER}, new Reward.NewResidents(1, 4)),
            new ResidentCard(2, new Goods[]{SNAPS, CANNED_MEAT}, new Reward.NewResidents(1, 4)),
            new ResidentCard(2, new Goods[]{BEER, COTTON_FABRIC}, new Reward.NewResidents(1, 4)),
            new ResidentCard(2, new Goods[]{BREAD, BEER}, new Reward.NewResidents(1, 4)),
            new ResidentCard(2, new Goods[]{BRICKS, SAUSAGE}, new Reward.NewResidents(1, 4)),
            new ResidentCard(2, new Goods[]{CANNED_MEAT, COTTON_FABRIC}, new Reward.NewResidents(1, 4)),

            new ResidentCard(2, new Goods[]{BREAD, SOAP}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{BREAD, CANNED_MEAT}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{SAUSAGE, BREAD}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{SAUSAGE, GOODS}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{SAUSAGE, SOAP}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{SNAPS, SOAP}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{COAL, SOAP}, new Reward.UpgradeResidents(3, 1, 2)),
            new ResidentCard(2, new Goods[]{WORK_CLOTHES, SAUSAGE}, new Reward.UpgradeResidents(3, 1, 2)),

            new ResidentCard(2, new Goods[]{BEER, BREAD}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{STEELBARS, SNAPS}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{SNAPS, COAL}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{WORK_CLOTHES, SNAPS}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.TradePoints(3)),
            new ResidentCard(2, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.TradePoints(3)),

            new ResidentCard(2, new Goods[]{STEELBARS, WORK_CLOTHES}, new Reward.Gold(4)),
            new ResidentCard(2, new Goods[]{WORK_CLOTHES, SNAPS}, new Reward.Gold(4)),
            new ResidentCard(2, new Goods[]{BEER, WORK_CLOTHES}, new Reward.Gold(4)),
            new ResidentCard(2, new Goods[]{STEELBARS, CANNED_MEAT}, new Reward.Gold(4)),
            new ResidentCard(2, new Goods[]{SOAP, BEER}, new Reward.Gold(4)),
            new ResidentCard(2, new Goods[]{SOAP, WORK_CLOTHES}, new Reward.Gold(4))            
        ));
    }
    
    /**
     * Level 5 Resident Cards (Artisans)
     */
    private static Deque<ResidentCard> getLevel5Cards() {
        return new ArrayDeque<>(List.of(
            new ResidentCard(5, new Goods[]{STEELBARS, CIGARS, HIGHBIKES}, new Reward.ExpeditionCards()),
            new ResidentCard(5, new Goods[]{CANNED_MEAT, POCKETWATCHES, GLASSES}, new Reward.ExpeditionCards()),
            new ResidentCard(5, new Goods[]{SNAPS, CHOCOLATE, COFFEE}, new Reward.ExpeditionCards()),

            new ResidentCard(5, new Goods[]{CIGARS, COATS, CARS}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{COATS, RUM, CARS}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{SEWING_MACHINES, COFFEE, LIGHT_BULBS}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{RUM, LIGHT_BULBS, GRAMOPHONES}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{CHOCOLATE, COATS, GLASSES}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{CANNED_MEAT, GLASSES, CARS}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{BREAD, CHOCOLATE, GRAMOPHONES}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{BEER, LIGHT_BULBS, SEWING_MACHINES}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.ExtraAction()),
            new ResidentCard(5, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.ExtraAction()),
            
            new ResidentCard(5, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(5, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(5, new Goods[]{CHAMPAGNE, COFFEE, GRAMOPHONES}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(5, new Goods[]{SNAPS, POCKETWATCHES, HIGHBIKES}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(5, new Goods[]{CIGARS, CHAMPAGNE, GLASSES}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(5, new Goods[]{COTTON_FABRIC, POCKETWATCHES, CARS}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(5, new Goods[]{CIGARS, HIGHBIKES, RUM}, new Reward.DiscardResidentCard(2)),
            
            new ResidentCard(5, new Goods[]{WORK_CLOTHES, COFFEE, BIG_BERTA}, new Reward.ExplorationPoints(3)),
            new ResidentCard(5, new Goods[]{SOAP, POCKETWATCHES, BIG_BERTA}, new Reward.ExplorationPoints(3)),
            new ResidentCard(5, new Goods[]{GOODS, SEWING_MACHINES, CARS}, new Reward.ExplorationPoints(3)),
            
            new ResidentCard(5, new Goods[]{BEER, CIGARS, CARS}, new Reward.UpgradeResidents(3, 2, 3)),
            new ResidentCard(5, new Goods[]{SOAP, COFFEE, CARS}, new Reward.UpgradeResidents(3, 2, 3)),
            new ResidentCard(5, new Goods[]{BREAD, CIGARS, CHAMPAGNE}, new Reward.UpgradeResidents(3, 2, 3)),
            new ResidentCard(5, new Goods[]{SAUSAGE, CHAMPAGNE, CHOCOLATE}, new Reward.UpgradeResidents(3, 2, 3)),

            new ResidentCard(5, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.FreeGoodsChoice(new Goods[]{CACAO, COFFEE_BEANS, RUBBER, SUGARCANE, TOBACCO}, 1)),
            new ResidentCard(5, new Goods[]{COAL, CHAMPAGNE, LIGHT_BULBS}, new Reward.FreeGoodsChoice(new Goods[]{CACAO, COFFEE_BEANS, RUBBER, SUGARCANE, TOBACCO}, 1)),

            new ResidentCard(5, new Goods[]{SAUSAGE, COFFEE, COATS}, new Reward.TradePoints(6)),
            new ResidentCard(5, new Goods[]{BRICKS, GRAMOPHONES, COFFEE}, new Reward.TradePoints(6)),
            new ResidentCard(5, new Goods[]{WORK_CLOTHES, RUM, COATS}, new Reward.TradePoints(6))
        ));
    }
    
    /**
     * Level 7 Resident Cards (Engineers)
     */
    private static Deque<ResidentCard> getLevel7Cards() {
        return new ArrayDeque<>(List.of(
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, SNAPS, TRADECHIP}, new Reward.ExtraAction()),
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, SAUSAGE, TRADECHIP}, new Reward.ExtraAction()),
            new ResidentCard(7, new Goods[]{HIGHBIKES, SNAPS, TRADECHIP}, new Reward.ExtraAction()),
            new ResidentCard(7, new Goods[]{CANNED_MEAT, SEWING_MACHINES, TRADECHIP}, new Reward.ExtraAction()),
            new ResidentCard(7, new Goods[]{CANNED_MEAT, COTTON_FABRIC, TRADECHIP}, new Reward.ExtraAction()),
            new ResidentCard(7, new Goods[]{WORK_CLOTHES, POCKETWATCHES, TRADECHIP}, new Reward.ExtraAction()),

            new ResidentCard(7, new Goods[]{BREAD, COTTON_FABRIC, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, SOAP, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, CANNED_MEAT, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{SOAP, COTTON_FABRIC, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{GLASSES, COTTON_FABRIC, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{GRAMOPHONES, COTTON_FABRIC, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, BEER, TRADECHIP}, new Reward.ExpeditionCards()),
            new ResidentCard(7, new Goods[]{COATS, SAUSAGE, TRADECHIP}, new Reward.ExpeditionCards()),

            new ResidentCard(7, new Goods[]{CARS, COTTON_FABRIC, TRADECHIP}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(7, new Goods[]{POCKETWATCHES, COTTON_FABRIC, TRADECHIP}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, SOAP, TRADECHIP}, new Reward.DiscardResidentCard(2)),
            new ResidentCard(7, new Goods[]{COTTON_FABRIC, BREAD, TRADECHIP}, new Reward.DiscardResidentCard(2)),

            new ResidentCard(7, new Goods[]{WORK_CLOTHES, BIG_BERTA, TRADECHIP}, new Reward.ExplorationPoints(3)),

            new ResidentCard(7, new Goods[]{BEER, COTTON_FABRIC, TRADECHIP}, new Reward.TradePoints(6)),

            new ResidentCard(7, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.GoldAndTradePoints(2, 4)),
            new ResidentCard(7, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.GoldAndTradePoints(2, 4)),
            new ResidentCard(7, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.GoldAndTradePoints(2, 4)),
            new ResidentCard(7, new Goods[]{EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP, EXPLORATIONCHIP}, new Reward.GoldAndTradePoints(2, 4))
        ));
    }
    
    /**
     * Get the total number of cards for a specific level.
     * 
     * @param populationLevel The population level (2, 5, or 7)
     * @return Number of cards for this level
     */
    public static int getCardCount(int populationLevel) {
        return getCardsForLevel(populationLevel).size();
    }
}
