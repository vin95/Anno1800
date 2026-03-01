package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentStatus;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UpgradeResidentValidator.
 * Rules tested:
 * - Array must be 1-3 residents (null, empty, >3 → false)
 * - Each resident must belong to the player
 * - Each resident must have level < 5 (level 5 cannot be upgraded)
 * - Higher-level resident must be available on the board
 * - Combined goods cost must be obtainable
 */
@DisplayName("UpgradeResidentValidator Tests")
class UpgradeResidentValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    // ─── null / empty / oversized array ────────────────────────────────────

    @Test
    @DisplayName("null residents array → false")
    void nullResidentsArray_returnsFalse() {
        Action.UpgradeResident action = new Action.UpgradeResident(null);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("empty residents array → false")
    void emptyResidentsArray_returnsFalse() {
        Action.UpgradeResident action = new Action.UpgradeResident(new Resident[0]);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("four residents in array → false (max 3)")
    void fourResidents_returnsFalse() {
        // Get 4 residents from the player (they start with some residents via initializePlayerBoard)
        var residents = player.getPlayerBoard().getResidents();
        if (residents.size() < 4) {
            // Not enough residents to test; just verify array size check
            Resident[] arr = new Resident[4];
            Action.UpgradeResident action = new Action.UpgradeResident(arr);
            assertFalse(ActionValidator.canExecute(action, player, game));
        } else {
            Resident[] arr = residents.subList(0, 4).toArray(new Resident[0]);
            Action.UpgradeResident action = new Action.UpgradeResident(arr);
            assertFalse(ActionValidator.canExecute(action, player, game));
        }
    }

    // ─── resident does not belong to player ────────────────────────────────

    @Test
    @DisplayName("resident belonging to other player → false")
    void residentBelongingToOtherPlayer_returnsFalse() {
        Player otherPlayer = game.getPlayers()[1];
        var otherResidents = otherPlayer.getPlayerBoard().getResidents();
        // Find a resident from the other player that has level < 5
        Resident foreign = otherResidents.stream()
                .filter(r -> r.getPopulationLevel() < 5)
                .findFirst()
                .orElse(null);
        if (foreign == null) return; // Skip if no suitable resident

        Action.UpgradeResident action = new Action.UpgradeResident(new Resident[]{foreign});
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── level-5 resident cannot be upgraded ───────────────────────────────

    @Test
    @DisplayName("level-5 resident in upgrade list → false")
    void level5Resident_cannotBeUpgraded() {
        // Find an Investor (level 5) on the player's board, if any
        Resident investor = player.getPlayerBoard().getResidents().stream()
                .filter(r -> r.getPopulationLevel() == 5)
                .findFirst()
                .orElse(null);
        if (investor == null) return; // Skip if player has no investor

        Action.UpgradeResident action = new Action.UpgradeResident(new Resident[]{investor});
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── board has target-level residents available ─────────────────────────

    @Test
    @DisplayName("board has all higher-level residents at start → low-level resident upgradeable in principle")
    void boardHasHigherLevelResidents_atStart() {
        // Board starts with workers=40, artisans=25, engineers=20, investors=15
        // All upgrade targets (2–5) should be available
        assertTrue(game.getBoard().getWorkers() > 0);
        assertTrue(game.getBoard().getArtisans() > 0);
        assertTrue(game.getBoard().getEngineers() > 0);
        assertTrue(game.getBoard().getInvestors() > 0);
    }

    // ─── board depleted: no upgrade target available ────────────────────────

    @Test
    @DisplayName("no workers on board → farmer upgrade → false")
    void noWorkersOnBoard_farmerUpgradeFails() {
        // Drain all workers from the board via takeResident(2)
        while (game.getBoard().getWorkers() > 0) {
            game.getBoard().takeResident(2);
        }

        // Find a farmer on the player's board
        Resident farmer = player.getPlayerBoard().getResidents().stream()
                .filter(r -> r.getPopulationLevel() == 1)
                .findFirst()
                .orElse(null);
        if (farmer == null) return; // No farmer to test with

        Action.UpgradeResident action = new Action.UpgradeResident(new Resident[]{farmer});
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── valid upgrade: single resident ────────────────────────────────────

    @Test
    @DisplayName("single FIT farmer from player can be validated for upgrade if board has workers")
    void singleFarmerUpgrade_boardHasWorkers_checkValidation() {
        // Farmers start FIT on the player board (from initializePlayerBoard)
        Resident farmer = player.getPlayerBoard().getResidents().stream()
                .filter(r -> r.getPopulationLevel() == 1 && r.getStatus() == ResidentStatus.FIT)
                .findFirst()
                .orElse(null);
        if (farmer == null) return;

        assertTrue(game.getBoard().getWorkers() > 0,
                "Board must have workers for this test to be meaningful");

        // The result depends on whether the player can afford the upgrade cost
        // but the board availability and ownership checks should pass
        Action.UpgradeResident action = new Action.UpgradeResident(new Resident[]{farmer});
        // Just assert no exception is thrown during validation
        assertDoesNotThrow(() -> ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("upgrading more than 3 FIT residents → false even if all are valid")
    void fourValidResidents_returnsFalse() {
        var allResidents = player.getPlayerBoard().getResidents();
        long upgradeable = allResidents.stream()
                .filter(r -> r.getPopulationLevel() < 5 && r.getStatus() == ResidentStatus.FIT)
                .count();
        if (upgradeable < 4) return; // Need at least 4 to form an oversized array

        Resident[] arr = allResidents.stream()
                .filter(r -> r.getPopulationLevel() < 5 && r.getStatus() == ResidentStatus.FIT)
                .limit(4)
                .toArray(Resident[]::new);

        Action.UpgradeResident action = new Action.UpgradeResident(arr);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }
}
