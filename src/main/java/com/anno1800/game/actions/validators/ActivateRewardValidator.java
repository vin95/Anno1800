package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.rewards.Reward;

/**
 * Validates reward activation actions.
 */
public class ActivateRewardValidator {

    public static boolean canActivateReward(Action.ActivateReward action, Player player, Game game) {
        // Use pattern matching to access specific reward properties
        return switch (action.reward()) {
            case Reward.NewResidents newResidents -> {
                int populationLevel = newResidents.populationLevel();
                
                // Check if at least one resident is available on the board
                int availableResidents = switch (populationLevel) {
                    case 1 -> game.getBoard().getFarmers();
                    case 2 -> game.getBoard().getWorkers();
                    case 3 -> game.getBoard().getArtisans();
                    case 4 -> game.getBoard().getEngineers();
                    case 5 -> game.getBoard().getInvestors();
                    default -> 0;
                };
                
                if (availableResidents < 1) {
                    yield false; // Cannot settle any resident
                }
                
                // Check how many resident cards are available in the stack
                int availableCards = switch (populationLevel) {
                    case 1, 2 -> game.getBoard().getResidentStack1().size();
                    case 3, 4, 5 -> game.getBoard().getResidentStack2().size();
                    default -> 0;
                };
                
                int goldCostPerResident = (populationLevel <= 2) ? 1 : 3;
                int availableGold = player.getPlayerBoard().getGold();
                
                // Calculate how many residents can be paid with gold
                int affordableWithGold = availableGold / goldCostPerResident;
                
                // Total settleable residents = cards + residents affordable with gold
                // but limited by available residents on board
                int maxSettleable = Math.min(
                    availableCards + affordableWithGold,
                    availableResidents
                );
                
                // Allow partial success: true if at least 1 resident can be settled
                // TODO: Agent should know the actual max settleable amount: maxSettleable
                yield maxSettleable >= 1;
            }
            case Reward.UpgradeResidents upgradeResidents -> {
                int populationLevel1 = upgradeResidents.populationLevel1();
                int populationLevel2 = upgradeResidents.populationLevel2();
                
                // Count how many residents the player has with populationLevel1
                long countLevel1 = player.getPlayerBoard().getResidents().stream()
                    .filter(resident -> resident.getPopulationLevel() == populationLevel1)
                    .count();
                
                // Check if player has at least one resident to upgrade
                if (countLevel1 < 1) {
                    yield false;
                }
                
                // Check if residents with populationLevel2 are available on the game board
                int availableLevel2OnBoard = switch (populationLevel2) {
                    case 1 -> game.getBoard().getFarmers();
                    case 2 -> game.getBoard().getWorkers();
                    case 3 -> game.getBoard().getArtisans();
                    case 4 -> game.getBoard().getEngineers();
                    case 5 -> game.getBoard().getInvestors();
                    default -> 0;
                };
                
                // Check if at least one resident of level2 is available on board
                if (availableLevel2OnBoard < 1) {
                    yield false;
                }
                
                yield true;
            }
            case Reward.ExtraAction extraAction -> {
                yield true;
            }
            case Reward.ExpeditionCards expeditionCards -> {
                if (game.getBoard().getExpeditionStack().isEmpty()) {
                    yield false;
                }
                yield true;
            }
            case Reward.FreeGoodsChoice freeGoodsChoice -> {
                yield true;
            }
            case Reward.TradePoints tradePoints -> {
                if (game.getBoard().getTradeChips() <= 0) {
                    yield false;
                }
                yield true;
            }
            case Reward.ExplorationPoints explorationPoints -> {
                if (game.getBoard().getExplorerChips() <= 0) {
                    yield false;
                }
                yield true;
            }
            case Reward.Gold gold -> {
                if (game.getBoard().getGold() <= 0) {
                    yield false;
                }
                yield true;
            }
            case Reward.GoldAndTradePoints goldAndTradePoints -> {             
                if (game.getBoard().getGold() + game.getBoard().getTradeChips() <= 0) {
                    yield false;
                }
                yield true;
            }
            case Reward.DiscardResidentCard discardResidentCard -> {
                yield true;
            }
            case Reward.BuildFactory buildFactory -> {
                yield true;
            }
            default -> false;
        };
    }
}
