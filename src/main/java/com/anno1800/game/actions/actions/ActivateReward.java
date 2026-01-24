package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.rewards.Reward;
import com.anno1800.game.residents.Resident;
import static com.anno1800.game.residents.ResidentStatus.FIT;

/**
 * Activate a reward.
 */
public class ActivateReward {
    @SuppressWarnings("deprecation")
    public static void activateReward(Player player, Reward reward, Game game) {
        switch (reward) {
            case Reward.NewResidents r -> {
                // Calculate how many residents can actually be settled
                int maxSettleable = com.anno1800.game.actions.validators.ActivateRewardValidator
                    .getMaxSettleableResidents(r, player, game);
                int toSettle = Math.min(r.amount(), maxSettleable);
                
                System.out.println("NewResidents reward: Requested=" + r.amount() + 
                    ", Max possible=" + maxSettleable + ", Settling=" + toSettle);
                
                // Settle the residents
                for (int i = 0; i < toSettle; i++) {
                    Resident resident = game.getBoard().takeResident(r.populationLevel());
                    resident.setStatus(FIT);
                    player.getPlayerBoard().getResidents().add(resident);
                }
            }
            case Reward.UpgradeResidents r -> {
                // Find residents of the specified level to upgrade
                PlayerBoard board = player.getPlayerBoard();
                int fromLevel = r.populationLevel1();
                int amount = r.amount();
                
                // Find residents matching the from level
                java.util.List<Resident> candidateResidents = board.getResidents().stream()
                    .filter(resident -> resident.getPopulationLevel() == fromLevel)
                    .limit(amount)
                    .toList();
                
                if (candidateResidents.isEmpty()) {
                    throw new IllegalStateException(
                        "Cannot upgrade: No residents of level " + fromLevel + " found on player board"
                    );
                }
                
                // Create array for UpgradeResident action
                Resident[] residentsToUpgrade = candidateResidents.toArray(new Resident[0]);
                
                // Execute the upgrade
                Action.UpgradeResident action = new Action.UpgradeResident(residentsToUpgrade);
                UpgradeResident.upgradeResident(player, game, action);
            }
            case Reward.ExtraAction r -> {
                // Mark that the player has an extra action available this turn
                // The extra action flag is now handled through the UseExtraAction ObjectiveCard
                // For regular ExtraAction rewards from ResidentCards, we simply allow them
                player.getPlayerBoard().markExtraActionUsed(); // Prevents stacking multiple extra actions
                System.out.println(player.getName() + " earned an Extra Action from reward!");
            }
            case Reward.ExpeditionCards r -> {
                player.getPlayerBoard().earnExpeditionCard(2, game.getBoard());
            }
            case Reward.FreeGoodsChoice r -> {
                // Prüfe ob bereits eine Wahl getroffen wurde
                if (!r.hasChoice()) {
                    throw new IllegalStateException(
                        "FreeGoodsChoice reward requires a choice to be made first. "
                        + "Use ChooseGoods action to select from: " 
                        + java.util.Arrays.toString(r.options())
                    );
                }
                // Füge die gewählte Ware dem PlayerBoard hinzu
                player.getPlayerBoard().addGoodToStoredGoods(r.chosenGood());
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