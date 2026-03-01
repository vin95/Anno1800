package com.anno1800.agents;

import com.anno1800.game.state.GameState;
import com.anno1800.game.state.GameState.PlayerState;
import com.anno1800.game.player.Player;

import java.util.List;

/**
 * Immutable snapshot of situation-relevant facts for agent decision-making.
 * Computed once per turn from the GameState before any action is scored.
 *
 * Captures two dimensions:
 * <ol>
 *   <li><b>Game length estimate</b> – how many turns are left?
 *       The end phase is triggered when any player plays their last ResidentCard.
 *       Fewer cards in hand → game is closer to ending.</li>
 *   <li><b>Resource state</b> – what does the current player have available?</li>
 * </ol>
 */
public record GameContext(

        // === Game length estimate ===

        /** Cards in the current player's hand. */
        int myCardCount,

        /** Minimum ResidentCard count across all opponents (→ who is closest to ending?). */
        int minOpponentCardCount,

        /** Average ResidentCard count across all opponents. */
        double avgOpponentCardCount,

        /** Current round number. */
        int currentRound,

        /**
         * True when the end phase is imminent:
         * any player (including self) has ≤ 1 card left.
         * Agents should deprioritize long-term investments when this is true.
         */
        boolean isEndPhaseLikely,

        // === Current player's resource state ===

        int gold,
        int tradeChips,
        int explorerChips,
        int freeLandTiles,
        int freeCoastTiles,
        int freeSeaTiles,
        int tradeShipCount,
        int explorerShipCount,
        int residentCount,
        int factoryCount

) {

    // =========================================================================
    // Factory
    // =========================================================================

    /**
     * Computes the GameContext for the given player from an immutable GameState snapshot.
     */
    public static GameContext compute(GameState gameState, Player player) {
        PlayerState my = gameState.players().stream()
                .filter(ps -> ps.name().equals(player.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found in GameState"));

        List<PlayerState> opponents = gameState.players().stream()
                .filter(ps -> !ps.name().equals(player.getName()))
                .toList();

        int minOpponentCards = opponents.stream()
                .mapToInt(PlayerState::residentCardCount)
                .min()
                .orElse(Integer.MAX_VALUE);

        double avgOpponentCards = opponents.stream()
                .mapToDouble(PlayerState::residentCardCount)
                .average()
                .orElse(Double.MAX_VALUE);

        boolean endPhaseLikely = my.residentCardCount() <= 1
                || minOpponentCards <= 1
                || gameState.players().stream().anyMatch(ps -> ps.residentCardCount() == 0);

        return new GameContext(
                my.residentCardCount(),
                minOpponentCards,
                avgOpponentCards,
                gameState.round(),
                endPhaseLikely,
                my.gold(),
                my.availableTradeChips(),
                my.availableExplorerChips(),
                my.freeLandTiles(),
                my.freeCoastTiles(),
                my.freeSeaTiles(),
                my.tradeShipCount(),
                my.explorerShipCount(),
                my.residentCount(),
                my.factoryCount()
        );
    }

    // =========================================================================
    // Convenience
    // =========================================================================

    /** True if the player has sufficient gold and trade chips to be flexible. */
    public boolean isResourceRich() {
        return gold >= 5 && tradeChips >= 2;
    }

    /** True if the player can afford exploration (has ships/chips). */
    public boolean canExplore() {
        return explorerShipCount >= 1;
    }

    /**
     * Estimated rounds remaining before the end phase is triggered.
     * Rough approximation: min opponent cards (each turn one card can be played).
     * Returns 0 when end phase is already likely.
     */
    public int estimatedRoundsLeft() {
        if (isEndPhaseLikely) return 0;
        return Math.min(myCardCount, minOpponentCardCount);
    }
}
