package com.anno1800.game.actions.actions;

import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Build a shipyard on a free coast tile.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 */
public class BuildShipyard {
    public static void buildShipyard(Player player, int level, Game game) {
        // Take shipyard from board
        var shipyard = game.getBoard().takeShipyard(level);

        // Add shipyard to player's board
        player.getPlayerBoard().buildShipyard(shipyard);

        System.out.println("Successfully built shipyard level " + level);
    }
}
