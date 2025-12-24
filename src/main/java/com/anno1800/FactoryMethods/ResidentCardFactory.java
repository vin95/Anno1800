package com.anno1800.FactoryMethods;

import com.anno1800.cards.ResidentCard;
import com.anno1800.cards.Reward;
import com.anno1800.data.Goods;
import static com.anno1800.data.Goods.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating predefined ResidentCard instances
 */
public class ResidentCardFactory {
    
    /**
     * Creates resident cards for stack 1 (early game)
     */
    public static List<ResidentCard> createStack1Cards() {
        List<ResidentCard> cards = new ArrayList<>();
        
        cards.add(new ResidentCard(2, new Goods[]{GLASS, BRASS, WORKFORCE_4}, new Reward.NewResidents(2, 3)));
        
        return cards;
    }
    
    /**
     * Creates resident cards for stack 2 (mid game)
     */
    public static List<ResidentCard> createStack2Cards() {
        List<ResidentCard> cards = new ArrayList<>();
        
        cards.add(new ResidentCard(5, new Goods[]{GLASS, BRASS, WORKFORCE_4}, new Reward.NewResidents(2, 3)));
        
        return cards;
    }
    
    /**
     * Creates resident cards for stack 3 (late game)
     */
    public static List<ResidentCard> createStack3Cards() {
        List<ResidentCard> cards = new ArrayList<>();
        
        cards.add(new ResidentCard(7, new Goods[]{GLASS, BRASS, WORKFORCE_4}, new Reward.NewResidents(2, 3)));
        
        return cards;
    }
}
