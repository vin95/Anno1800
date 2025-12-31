package com.anno1800.game.actions.actions;

import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.rewards.Reward;

/**
 * Activate a reward.
 */
public class ActivateReward {
    public static void activateReward(Player player, Reward reward, Game game) {
        switch (reward) {
            case Reward.NewResidents r -> {
                // TODO: Neue Residents hinzufügen
            }
            case Reward.UpgradeResidents r -> {
                // TODO: Residents upgraden
            }
            case Reward.ExtraAction r -> {
                // TODO: Extra Aktion gewähren
            }
            case Reward.ExpeditionCards r -> {
                // TODO: ExpeditionCards geben
            }
            case Reward.FreeGoodsChoice r -> {
                // TODO: Spieler wählt ein Good aus r.options(), erhält r.amount()
            }
            case Reward.TradePoints r -> {
                game.getBoard().takeTradeChip(r.points());
                player.getPlayerBoard().increaseAvailableTradeChips(r.points());
            }
            case Reward.ExplorationPoints r -> {
                game.getBoard().takeExplorerChip(r.points());
                player.getPlayerBoard().increaseAvailableExplorerChips(r.points());
            }
            case Reward.Gold r -> {
                player.getPlayerBoard().earnGold(r.amount());
            }
            case Reward.GoldAndTradePoints r -> {
                player.getPlayerBoard().earnGold(r.goldAmount());
                player.getPlayerBoard().increaseAvailableTradeChips(r.tradePoints());
            }
            case Reward.DiscardResidentCard r -> {
                PlayerBoard board = player.getPlayerBoard();
                for (int i = 0; i < r.amount(); i++) {
                    ResidentCard cardToDiscard = board.getResidentCards().get(0);
                    board.discardResidentCard(cardToDiscard, game.getBoard());
                }
            }
            case Reward.BuildFactory r -> {
                player.getPlayerBoard().buildFactoryAsReward(r.factoryType());
            }
            default -> throw new IllegalArgumentException("Unbekannter Reward-Typ: " + reward);
        }
    }
}