package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.actions.SettleResident;
import com.anno1800.game.engine.Game;
import com.anno1800.game.engine.Rules;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.ResidentCosts;

/**
 * Validates resident-related actions (settling, upgrading, fulfilling needs, swapping cards).
 */
public class SettleResidentValidator {

    /**
     * Validates SettleResident action.
     * Requirements:
     * - Must have a resident of the specified population level available on the board
     * - Required goods must be obtainable (production/trade/import)
     */
    public static boolean canSettleResident(Action.SettleResident action, Player player, Game game) {
        // Check basic requirements (resident availability)
        if (!SettleResident.canSettleResident(player, game, action.level())) {
            return false;
        }

        // Check card-stack / gold fallback rule consistency with execution path.
        if (!Rules.canSettleResident(game.getBoard(), player, action.level())) {
            return false;
        }
        
        PlayerBoard board = player.getPlayerBoard();
        
        // PLANNING PHASE: Check if player can obtain required goods
        ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(action.level());
        boolean canObtain = board.canObtainGoods(cost.goods(), game);
        
        // Clear storedGoods after validation (rollback)
        board.clearStoredGoods();
        
        return canObtain;
    }
}
