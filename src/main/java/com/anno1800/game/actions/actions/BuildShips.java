package com.anno1800.game.actions.actions;

import com.anno1800.game.player.Player;
import com.anno1800.game.engine.Game;
import com.anno1800.data.gamedata.ShipType;

/**
 * Build ships using the player's shipyards.
 * Each shipyard can build one ship per action.
 * Ships are built sequentially, so chips earned from one ship are available for
 * the next.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 */
public class BuildShips {
    public static void buildShips(Player player, ShipType shipType, int level, int amount, Game game) {
        // Build ships sequentially
        // Important: Ships are built one by one, so chips from previous ships
        // are available for building the next ship
        for (int i = 0; i < amount; i++) {
            // Take ship from board (also takes the required chips)
            Object ship = game.getBoard().takeShip(shipType, level);

            // Add ship to player's board (also adds the chips to player's available chips)
            player.getPlayerBoard().buildShip(ship, shipType, level);

            System.out.println("Built ship " + (i + 1) + "/" + amount + ": " + shipType + " level " + level);
        }

        System.out.println("Successfully built " + amount + " " + shipType + "(s) of level " + level);
    }
}
