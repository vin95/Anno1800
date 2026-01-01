package com.anno1800.game.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.rewards.Reward;

/**
 * Represents the result of executing an action.
 * Most actions return NoResult, but some actions produce specific outputs.
 * Sealed interface ensures type-safe handling of all possible result types.
 */
public sealed interface ActionResult permits
        ActionResult.NoResult,
        ActionResult.GoodsResult,
        ActionResult.CardResult,
        ActionResult.CardsResult,
        ActionResult.RewardResult {

    /**
     * Result for actions that don't produce any output.
     * Examples: BuildFactory, BuildShipyard, SettleResident, etc.
     */
    record NoResult() implements ActionResult {
    }

    /**
     * Result for actions that produce goods.
     * Examples: ProduceGoods, TradeGoods, ImportGood
     */
    record GoodsResult(Goods goods) implements ActionResult {
    }

    /**
     * Result for actions that produce a resident card.
     * Examples: DrawResidentCard
     */
    record CardResult(ResidentCard card) implements ActionResult {
    }

    /**
     * Result for actions that produce a resident card.
     * Examples: swapResidentCards
     */
    record CardsResult(ResidentCard[] cards) implements ActionResult {
    }

    /**
     * Result for actions that produce a modified reward.
     * Examples: ChooseGoods (returns FreeGoodsChoice with chosen good)
     */
    record RewardResult(Reward reward) implements ActionResult {
    }
}
