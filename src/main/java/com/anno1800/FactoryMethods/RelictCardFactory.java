package com.anno1800.FactoryMethods;

import com.anno1800.cards.RelictCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating predefined RelictCard instances
 */
public class RelictCardFactory {
    
    /**
     * Creates all relict cards for the game
     */
    public static List<RelictCard> createAllRelictCards() {
        List<RelictCard> cards = new ArrayList<>();
        
        cards.add(new RelictCard(3, 5, 3, 3));
        
        return cards;
    }
}
