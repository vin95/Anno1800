package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.board.Board;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentCosts;
import com.anno1800.data.gamedata.Goods;
import java.util.ArrayList;

/**
 * Upgrade a resident to the next population level.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 * Goods are obtained and consumed during this action.
 */
public class UpgradeResident {
    
    /**
     * Upgrades up to 3 residents to the next level.
     * For each resident: removes from PlayerBoard, obtains required goods,
     * and adds a new higher-level resident from GameBoard.
     * 
     * @param player The player upgrading the residents
     * @param game The current game state
     * @param action The upgrade resident action (with 1-3 residents)
     */
    public static void upgradeResident(Player player, Game game, Action.UpgradeResident action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board gameBoard = game.getBoard();
        Resident[] residents = action.residents();
        
        // Validate array
        if (residents == null || residents.length == 0 || residents.length > 3) {
            throw new IllegalArgumentException("Must upgrade 1-3 residents, got: " + 
                (residents == null ? "null" : residents.length));
        }
        
        System.out.println("Upgrading " + residents.length + " resident(s)");

        // --- Validate residents and collect ALL costs into one combined array ---
        // This is necessary because canObtainGoods + consumeGoods must be called
        // exactly once each: consumeGoods clears storedGoods at the end, so calling
        // it in a per-resident loop would wipe planned goods before later residents
        // can consume theirs.
        ArrayList<Goods> allCosts = new ArrayList<>();
        for (Resident oldResident : residents) {
            int currentLevel = oldResident.getPopulationLevel();
            int targetLevel = currentLevel + 1;

            if (currentLevel >= 5) {
                throw new IllegalStateException("Cannot upgrade level 5 resident");
            }
            if (!playerBoard.getResidents().contains(oldResident)) {
                throw new IllegalStateException("Resident does not belong to player");
            }

            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(targetLevel);
            if (cost.goods() != null) {
                for (Goods g : cost.goods()) {
                    allCosts.add(g);
                }
            }
        }

        // PHASE 1: Single combined planning pass
        Goods[] combinedCosts = allCosts.toArray(new Goods[0]);
        if (combinedCosts.length > 0) {
            System.out.println("  Planning upgrade, requires: " + java.util.Arrays.toString(combinedCosts));
            if (!playerBoard.canObtainGoods(combinedCosts, game)) {
                throw new IllegalStateException("Cannot obtain required goods for upgrade");
            }
        }

        // PHASE 2: Single combined execution pass (consumes storedGoods once)
        if (combinedCosts.length > 0) {
            playerBoard.consumeGoods(combinedCosts, game);
        }

        // PHASE 3: Transform residents (no resource changes, just board manipulation)
        for (Resident oldResident : residents) {
            int currentLevel = oldResident.getPopulationLevel();
            int targetLevel = currentLevel + 1;

            playerBoard.getResidents().remove(oldResident);
            System.out.println("    Removed resident level " + currentLevel);

            Resident newResident = gameBoard.takeResident(targetLevel);
            newResident.setStatus(oldResident.getStatus());
            playerBoard.getResidents().add(newResident);
            System.out.println("    Added resident level " + targetLevel + " with status " + newResident.getStatus());

            System.out.println("  Successfully upgraded resident from level " + currentLevel + " to " + targetLevel);
        }

        System.out.println("All " + residents.length + " resident(s) upgraded successfully");
    }
}
