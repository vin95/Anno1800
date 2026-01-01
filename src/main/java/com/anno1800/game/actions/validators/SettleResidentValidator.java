package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.actions.SettleResident;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates resident-related actions (settling, upgrading, fulfilling needs, swapping cards).
 */
public class SettleResidentValidator {

    /**
     * Validates SettleResident action.
     * Requirements:
     * - Must have a resident of the specified population level available on the board
     * - Must have a resident card in the corresponding stack on the game board OR
     * - Must have enough gold to pay:
     *   - Population level 1,2: 1 Gold
     *   - Population level 3,4,5: 3 Gold
     */
    public static boolean canSettleResident(Action.SettleResident action, Player player, Game game) {
        // Use the new SettleResident validation logic
        return SettleResident.canSettleResident(player, game, action.level());
    }
}
