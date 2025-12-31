package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates resident-related actions (settling, upgrading, fulfilling needs, swapping cards).
 */
public class DrawResidentCardValidator {

    public static boolean canDrawResidentCard(Action.DrawResidentCard action, Player player, Game game) {
        // Validate population level
        int populationLevel = action.populationLevel();
        
        // Check if board has available resident cards for this population level
        // Population levels 1-2 use residentStack1
        // Population levels 3-5 use residentStack2
        // Population levels 6-7 use residentStack3
        boolean hasCards = switch (populationLevel) {
            case 1, 2 -> !game.getBoard().getResidentStack1().isEmpty();
            case 3, 4, 5 -> !game.getBoard().getResidentStack2().isEmpty();
            case 6, 7 -> !game.getBoard().getResidentStack3().isEmpty();
            default -> false; // Invalid population level
        };
        
        return hasCards;
    }
}
