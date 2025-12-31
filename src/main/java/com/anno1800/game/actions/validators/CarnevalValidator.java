package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates if a Carnival is possible. Return always true. A Carnival can always be held, but is not useful in most cases.
 */
public class CarnevalValidator {

    public static boolean canCarneval(Action.Carneval action, Player player, Game game) {
        return true;
    }
}
