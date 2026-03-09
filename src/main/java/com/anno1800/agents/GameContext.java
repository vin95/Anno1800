package com.anno1800.agents;

import com.anno1800.game.state.GameState;
import com.anno1800.game.state.GameState.PlayerState;
import com.anno1800.game.player.Player;

import java.util.List;

/**
 * Immutable snapshot of situation-relevant facts for agent decision-making.
 * Computed once per turn from the GameState before any action is scored.
 *
 * <p>Captures flexible parameters that change every turn:
 * <ul>
 *   <li><b>Game length estimate</b> – how many turns are left?</li>
 *   <li><b>Resource state</b> – what does the current player have available?</li>
 *   <li><b>Hand card bonuses</b> – bonuses from cards currently in hand</li>
 *   <li><b>Opponent potential</b> – bonuses opponents can play</li>
 * </ul>
 *
 * <p>For fixed parameters (e.g., ObjectiveCard evaluation), see {@link ObjectiveContext},
 * which is computed once at game start and reused throughout the game.
 */
public record GameContext(

        // === Game length estimate (flexible - changes each turn) ===

        /** Cards in the current player's hand. */
        int myCardCount,

        /** Minimum ResidentCard count across all opponents (→ who is closest to ending?). */
        int minOpponentCardCount,

        /** Average ResidentCard count across all opponents. */
        double avgOpponentCardCount,
        
        /** List of individual opponent card counts for detailed analysis. */
        List<Integer> opponentCardCounts,

        /** Current round number. */
        int currentRound,

        /**
         * True when the end phase is active:
         * any player (including self) has ≤ 1 card left.
         * Agents should deprioritize long-term investments when this is true.
         */
        boolean isEndPhase,
        
        /**
         * Number of remaining turns for the current player (INCLUDING the current turn).
         * <ul>
         *   <li><b>-1</b>: End phase not active (unknown/not relevant)</li>
         *   <li><b>1</b>: Only 1 turn left (this player already had their turn this round)</li>
         *   <li><b>2</b>: 2 turns left (this player has not yet played this round)</li>
         * </ul>
         * 
         * <p>Calculation logic:
         * If this player's turn order is AFTER the player who triggered end phase,
         * they get 2 turns (current + final round). Otherwise, 1 turn (final round only).
         */
        int remainingTurns,

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
                .mapToInt(ps -> ps.cards().residentCardCount())
                .min()
                .orElse(Integer.MAX_VALUE);

        double avgOpponentCards = opponents.stream()
                .mapToDouble(ps -> ps.cards().residentCardCount())
                .average()
                .orElse(Double.MAX_VALUE);
        
        List<Integer> opponentCardCounts = opponents.stream()
                .map(ps -> ps.cards().residentCardCount())
                .toList();

        boolean endPhase = my.cards().residentCardCount() <= 1
                || minOpponentCards <= 1
                || gameState.players().stream().anyMatch(ps -> ps.cards().residentCardCount() == 0);

        // Calculate remaining turns in end phase
        int remainingTurns = calculateRemainingTurns(
            gameState.boardState().endPhase(),
            gameState.currentPlayerIndex(),
            gameState.startingPlayerIndex(),
            gameState.players().size()
        );

        return new GameContext(
                my.cards().residentCardCount(),
                minOpponentCards,
                avgOpponentCards,
                opponentCardCounts,
                gameState.round(),
                endPhase,
                remainingTurns,
                my.resources().gold(),
                my.resources().availableTradeChips(),
                my.resources().availableExplorerChips(),
                my.tiles().freeLandTiles(),
                my.tiles().freeCoastTiles(),
                my.tiles().freeSeaTiles(),
                my.ships().tradeShips().totalCount(),
                my.ships().explorerShips().totalCount(),
                my.residents().count(),
                my.buildings().factoryCount()
        );
    }
    
    /**
     * Calculates how many turns the current player has left (including current turn).
     * 
     * <p>Logic:
     * <ul>
     *   <li>If end phase not active: return -1 (unknown)</li>
     *   <li>Players play in order starting from startingPlayerIndex</li>
     *   <li>If current player's turn order is AFTER the trigger player: 2 turns (this + final round)</li>
     *   <li>If current player's turn order is BEFORE or EQUAL to trigger player: 1 turn (final round only)</li>
     * </ul>
     * 
     * @param endPhase End phase state from board
     * @param currentPlayerIndex Index of current player
     * @param startingPlayerIndex Index of starting player
     * @param numPlayers Total number of players
     * @return Remaining turns (-1 if end phase not active, 1-2 otherwise)
     */
    private static int calculateRemainingTurns(
        GameState.BoardState.EndPhaseState endPhase,
        int currentPlayerIndex,
        int startingPlayerIndex,
        int numPlayers
    ) {
        if (!endPhase.isActive()) {
            return -1; // End phase not active
        }
        
        int triggerPlayerIndex = endPhase.triggeredByPlayerIndex();
        
        // Calculate turn order positions (0 = first to play, numPlayers-1 = last)
        int myTurnOrder = (currentPlayerIndex - startingPlayerIndex + numPlayers) % numPlayers;
        int triggerTurnOrder = (triggerPlayerIndex - startingPlayerIndex + numPlayers) % numPlayers;
        
        // If my turn order is AFTER the trigger player, I haven't played yet this round
        // → I get 2 turns (current + final round)
        // Otherwise, I already played this round → only 1 turn left (final round)
        if (myTurnOrder > triggerTurnOrder) {
            return 2; // Current turn + final round
        } else {
            return 1; // Only final round
        }
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
     * Returns 0 when end phase is already active.
     */
    public int estimatedRoundsLeft() {
        if (isEndPhase) return 0;
        return Math.min(myCardCount, minOpponentCardCount);
    }
    
    /**
     * True if this is the player's last turn in the game.
     * Use this to maximize immediate victory points and avoid long-term investments.
     */
    public boolean isLastTurn() {
        return remainingTurns == 1;
    }
    
    /**
     * True if the player has exactly 2 turns left (current + one final).
     * The player should focus on actions that can be completed and scored in 2 turns.
     */
    public boolean hasTwoTurnsLeft() {
        return remainingTurns == 2;
    }
}
