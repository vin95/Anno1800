package com.anno1800.game.actions.actions;

import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import static com.anno1800.game.residents.ResidentStatus.*;

/**
 * Action to exhaust a worker (resident).
 */
public class ExhaustWorker {
    public static void exhaustWorker(Player player, Resident resident) {
        resident.setStatus(EXHAUSTED);
    }
}
