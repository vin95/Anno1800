package com.anno1800.cards;

import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ResidentCard record.
 * Verifies that all record components are stored and accessed correctly.
 */
@DisplayName("ResidentCard Tests")
class ResidentCardTest {

    @Test
    @DisplayName("populationLevel() gibt den korrekten Wert zurück")
    void testPopulationLevel() {
        ResidentCard card = new ResidentCard(2, new Goods[]{Goods.PLANKS, Goods.BRICKS}, new Reward.Gold(1));
        assertEquals(2, card.populationLevel());
    }

    @Test
    @DisplayName("needs() gibt das korrekte Güter-Array zurück")
    void testNeeds() {
        Goods[] needs = {Goods.PLANKS, Goods.BRICKS};
        ResidentCard card = new ResidentCard(2, needs, new Reward.Gold(1));
        assertArrayEquals(needs, card.needs());
    }

    @Test
    @DisplayName("reward() gibt das korrekte Reward zurück")
    void testReward() {
        Reward reward = new Reward.Gold(5);
        ResidentCard card = new ResidentCard(1, new Goods[]{Goods.PLANKS}, reward);
        assertEquals(reward, card.reward());
    }

    @Test
    @DisplayName("ResidentCard Level 1 mit PLANKS-Bedarf")
    void testLevel1CardWithPlanks() {
        ResidentCard card = new ResidentCard(1, new Goods[]{Goods.PLANKS}, new Reward.ExtraAction());
        assertEquals(1, card.populationLevel());
        assertEquals(1, card.needs().length);
        assertEquals(Goods.PLANKS, card.needs()[0]);
    }

    @Test
    @DisplayName("ResidentCard Level 5 mit mehreren Bedürfnissen")
    void testLevel5CardWithMultipleNeeds() {
        Goods[] needs = {Goods.STEELBARS, Goods.GOODS, Goods.COAL, Goods.WINDOWS, Goods.COATS};
        ResidentCard card = new ResidentCard(5, needs, new Reward.Gold(3));
        assertEquals(5, card.populationLevel());
        assertEquals(5, card.needs().length);
    }

    @Test
    @DisplayName("Zwei identische ResidentCards sind gleich (Record-Semantik)")
    void testEqualityWithSameComponents() {
        Goods[] needs = {Goods.PLANKS};
        Reward reward = new Reward.Gold(1);
        ResidentCard card1 = new ResidentCard(1, needs, reward);
        ResidentCard card2 = new ResidentCard(1, needs, reward);
        assertEquals(card1, card2);
    }

    @Test
    @DisplayName("ResidentCards mit verschiedenen Leveln sind ungleich")
    void testInequalityDifferentLevels() {
        Goods[] needs = {Goods.PLANKS};
        Reward reward = new Reward.Gold(1);
        ResidentCard card1 = new ResidentCard(1, needs, reward);
        ResidentCard card2 = new ResidentCard(2, needs, reward);
        assertNotEquals(card1, card2);
    }

    @Test
    @DisplayName("ResidentCard mit leerem Bedürfnis-Array ist gültig")
    void testCardWithEmptyNeeds() {
        ResidentCard card = new ResidentCard(1, new Goods[]{}, new Reward.Gold(1));
        assertNotNull(card);
        assertEquals(0, card.needs().length);
    }

    @Test
    @DisplayName("Verschiedene Reward-Typen können einer Karte zugewiesen werden")
    void testVariousRewardTypes() {
        assertDoesNotThrow(() -> new ResidentCard(1, new Goods[]{}, new Reward.ExtraAction()));
        assertDoesNotThrow(() -> new ResidentCard(2, new Goods[]{}, new Reward.ExpeditionCards()));
        assertDoesNotThrow(() -> new ResidentCard(3, new Goods[]{}, new Reward.TradePoints(2)));
        assertDoesNotThrow(() -> new ResidentCard(4, new Goods[]{}, new Reward.ExplorationPoints(3)));
        assertDoesNotThrow(() -> new ResidentCard(5, new Goods[]{},
            new Reward.NewResidents(2, 1)));
    }
}
