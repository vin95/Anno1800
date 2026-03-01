package com.anno1800.actions;

import com.anno1800.game.actions.ActionResult;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all ActionResult sealed-interface subtypes.
 * Verifies that each result type correctly stores and returns its data.
 */
@DisplayName("ActionResult Tests")
class ActionResultTest {

    @Test
    @DisplayName("NoResult kann instanziiert werden und ist vom richtigen Typ")
    void testNoResultInstantiation() {
        ActionResult result = new ActionResult.NoResult();
        assertNotNull(result);
        assertInstanceOf(ActionResult.NoResult.class, result);
    }

    @Test
    @DisplayName("GoodsResult speichert das korrekte Gut")
    void testGoodsResultStoresGood() {
        ActionResult result = new ActionResult.GoodsResult(Goods.PLANKS);
        assertInstanceOf(ActionResult.GoodsResult.class, result);
        assertEquals(Goods.PLANKS, ((ActionResult.GoodsResult) result).goods());
    }

    @Test
    @DisplayName("GoodsResult mit verschiedenen Gütern speichert korrekt")
    void testGoodsResultWithDifferentGoods() {
        for (Goods good : new Goods[]{Goods.COAL, Goods.BEER, Goods.STEELBARS, Goods.CACAO}) {
            ActionResult result = new ActionResult.GoodsResult(good);
            assertEquals(good, ((ActionResult.GoodsResult) result).goods(),
                "GoodsResult sollte " + good + " korrekt speichern");
        }
    }

    @Test
    @DisplayName("CardResult speichert die korrekte ResidentCard")
    void testCardResultStoresCard() {
        ResidentCard card = new ResidentCard(1, new Goods[]{Goods.PLANKS}, new Reward.Gold(1));
        ActionResult result = new ActionResult.CardResult(card);
        assertInstanceOf(ActionResult.CardResult.class, result);
        assertEquals(card, ((ActionResult.CardResult) result).card());
    }

    @Test
    @DisplayName("CardsResult speichert ein Array von ResidentCards")
    void testCardsResultStoresArray() {
        ResidentCard card1 = new ResidentCard(1, new Goods[]{Goods.PLANKS}, new Reward.Gold(1));
        ResidentCard card2 = new ResidentCard(2, new Goods[]{Goods.PLANKS, Goods.BRICKS}, new Reward.Gold(2));
        ResidentCard[] cards = {card1, card2};

        ActionResult result = new ActionResult.CardsResult(cards);
        assertInstanceOf(ActionResult.CardsResult.class, result);
        assertEquals(2, ((ActionResult.CardsResult) result).cards().length);
        assertSame(card1, ((ActionResult.CardsResult) result).cards()[0]);
        assertSame(card2, ((ActionResult.CardsResult) result).cards()[1]);
    }

    @Test
    @DisplayName("RewardResult speichert das korrekte Reward")
    void testRewardResultStoresReward() {
        Reward reward = new Reward.Gold(5);
        ActionResult result = new ActionResult.RewardResult(reward);
        assertInstanceOf(ActionResult.RewardResult.class, result);
        assertEquals(reward, ((ActionResult.RewardResult) result).reward());
    }

    @Test
    @DisplayName("Verschiedene ActionResult-Typen sind unterschiedliche Instanzen")
    void testDifferentResultTypesAreNotEqual() {
        ActionResult noResult = new ActionResult.NoResult();
        ActionResult goodsResult = new ActionResult.GoodsResult(Goods.PLANKS);
        assertNotEquals(noResult, goodsResult);
    }

    @Test
    @DisplayName("Zwei identische NoResults sind gleich (Record-Semantik)")
    void testTwoNoResultsAreEqual() {
        ActionResult result1 = new ActionResult.NoResult();
        ActionResult result2 = new ActionResult.NoResult();
        assertEquals(result1, result2, "Zwei NoResult-Instanzen sollten gleich sein");
    }

    @Test
    @DisplayName("Zwei GoodsResults mit gleichem Gut sind gleich")
    void testTwoGoodsResultsWithSameGoodAreEqual() {
        ActionResult result1 = new ActionResult.GoodsResult(Goods.PLANKS);
        ActionResult result2 = new ActionResult.GoodsResult(Goods.PLANKS);
        assertEquals(result1, result2);
    }
}
