package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.engine.Game;
import com.anno1800.data.gamedata.ShipType;
import com.anno1800.game.tiles.ShipCosts;

/**
 * Build ships using the player's shipyards.
 * Each shipyard can build one ship per action.
 * Ships are built sequentially, so chips earned from one ship are available for
 * the next.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 * Goods are obtained and consumed for each ship during this action.
 */
public class BuildShips {
    public static void buildShips(Player player, ShipType shipType, int level, int amount, Game game) {
        PlayerBoard board = player.getPlayerBoard();
        
        // Get required goods per ship
        Goods[] costPerShip = ShipCosts.getShipCost(level);
        
        // PHASE 1: Validate and plan all ships (accumulate resource planning)
        if (costPerShip != null && costPerShip.length > 0) {
            for (int i = 0; i < amount; i++) {
                System.out.println("Planning ship " + (i + 1) + "/" + amount + ": " + shipType + " level " + level);
                System.out.println("  Requires: " + java.util.Arrays.toString(costPerShip));
                
                // PLANNING PHASE: Determine how to obtain goods (accumulates in storedGoods)
                if (!board.canObtainGoods(costPerShip)) {
                    throw new IllegalStateException("Cannot obtain required goods for ship " + (i + 1));
                }
            }
        }
        
        // PHASE 2: Execute all ships (consume planned resources and build ships)
        // Important: Ships are built one by one, so chips from previous ships
        // are available for building the next ship
        for (int i = 0; i < amount; i++) {
            System.out.println("Building ship " + (i + 1) + "/" + amount + ": " + shipType + " level " + level);
            
            if (costPerShip != null && costPerShip.length > 0) {
                // EXECUTION PHASE: Actually obtain and consume goods from storedGoods
                board.consumeGoods(costPerShip);
            }
            
            // Take ship from board (also takes the required chips)
            Object ship = game.getBoard().takeShip(shipType, level);

            // Add ship to player's board (also adds the chips to player's available chips)
            board.buildShip(ship, shipType, level);
        }

        System.out.println("Successfully built " + amount + " " + shipType + "(s) of level " + level);
    }
}
