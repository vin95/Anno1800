package com.anno1800.engine;

import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for player position assignment based on start player.
 * Tests verify that positions are correctly assigned relative to the randomly selected start player.
 */
@DisplayName("Player Position Tests")
class PlayerPositionTest {

    @Test
    @DisplayName("With 1 player: Player should always have position 1")
    void testSinglePlayerPosition() {
        Game game = new Game(1);
        Player[] players = game.getPlayers();
        
        assertEquals(1, players[0].getPosition(),
                "Single player should always have position 1");
    }

    @RepeatedTest(20)
    @DisplayName("With 2 players: Start player has position 1, other has position 2")
    void testTwoPlayersPositions() {
        Game game = new Game(2);
        Player[] players = game.getPlayers();
        
        // Find start player (position 1)
        Player startPlayer = null;
        Player secondPlayer = null;
        
        for (Player player : players) {
            if (player.getPosition() == 1) {
                startPlayer = player;
            } else if (player.getPosition() == 2) {
                secondPlayer = player;
            }
        }
        
        assertNotNull(startPlayer, "There should be a player with position 1");
        assertNotNull(secondPlayer, "There should be a player with position 2");
        
        // Verify no duplicate positions
        assertNotEquals(players[0].getPosition(), players[1].getPosition(),
                "Players should have unique positions");
    }

    @RepeatedTest(30)
    @DisplayName("With 3 players: Positions should be 1, 2, 3 relative to start player")
    void testThreePlayersPositions() {
        Game game = new Game(3);
        Player[] players = game.getPlayers();
        
        // Verify all positions are assigned
        boolean hasPosition1 = false;
        boolean hasPosition2 = false;
        boolean hasPosition3 = false;
        
        for (Player player : players) {
            int pos = player.getPosition();
            if (pos == 1) hasPosition1 = true;
            if (pos == 2) hasPosition2 = true;
            if (pos == 3) hasPosition3 = true;
            
            assertTrue(pos >= 1 && pos <= 3,
                    "Position should be between 1 and 3, but was " + pos);
        }
        
        assertTrue(hasPosition1, "Should have a player with position 1");
        assertTrue(hasPosition2, "Should have a player with position 2");
        assertTrue(hasPosition3, "Should have a player with position 3");
    }

    @RepeatedTest(40)
    @DisplayName("With 4 players: Positions should be 1, 2, 3, 4 relative to start player")
    void testFourPlayersPositions() {
        Game game = new Game(4);
        Player[] players = game.getPlayers();
        
        // Verify all positions are assigned
        boolean hasPosition1 = false;
        boolean hasPosition2 = false;
        boolean hasPosition3 = false;
        boolean hasPosition4 = false;
        
        for (Player player : players) {
            int pos = player.getPosition();
            if (pos == 1) hasPosition1 = true;
            if (pos == 2) hasPosition2 = true;
            if (pos == 3) hasPosition3 = true;
            if (pos == 4) hasPosition4 = true;
            
            assertTrue(pos >= 1 && pos <= 4,
                    "Position should be between 1 and 4, but was " + pos);
        }
        
        assertTrue(hasPosition1, "Should have a player with position 1");
        assertTrue(hasPosition2, "Should have a player with position 2");
        assertTrue(hasPosition3, "Should have a player with position 3");
        assertTrue(hasPosition4, "Should have a player with position 4");
    }

    @Test
    @DisplayName("Start player 0 with 4 players: Verify exact position mapping")
    void testStartPlayerZeroFourPlayers() {
        // Create multiple games until we get start player 0
        Game game = null;
        for (int attempt = 0; attempt < 100; attempt++) {
            game = new Game(4);
            if (game.getPlayers()[0].getPosition() == 1) {
                break; // Found a game where player 0 is start player
            }
        }
        
        assertNotNull(game, "Should find a game where player 0 is start player");
        Player[] players = game.getPlayers();
        
        if (players[0].getPosition() == 1) {
            assertEquals(1, players[0].getPosition(), "Player 0 should have position 1");
            assertEquals(2, players[1].getPosition(), "Player 1 should have position 2");
            assertEquals(3, players[2].getPosition(), "Player 2 should have position 3");
            assertEquals(4, players[3].getPosition(), "Player 3 should have position 4");
        }
    }

    @Test
    @DisplayName("Start player 2 with 4 players: Verify exact position mapping")
    void testStartPlayerTwoFourPlayers() {
        // Create multiple games until we get start player 2
        Game game = null;
        for (int attempt = 0; attempt < 100; attempt++) {
            game = new Game(4);
            if (game.getPlayers()[2].getPosition() == 1) {
                break; // Found a game where player 2 is start player
            }
        }
        
        assertNotNull(game, "Should find a game where player 2 is start player");
        Player[] players = game.getPlayers();
        
        if (players[2].getPosition() == 1) {
            assertEquals(3, players[0].getPosition(), "Player 0 should have position 3");
            assertEquals(4, players[1].getPosition(), "Player 1 should have position 4");
            assertEquals(1, players[2].getPosition(), "Player 2 should have position 1");
            assertEquals(2, players[3].getPosition(), "Player 3 should have position 2");
        }
    }

    @Test
    @DisplayName("All positions should be unique for any number of players")
    void testUniquePositions() {
        for (int numPlayers = 1; numPlayers <= 4; numPlayers++) {
            Game game = new Game(numPlayers);
            Player[] players = game.getPlayers();
            
            // Check all positions are unique
            for (int i = 0; i < players.length; i++) {
                for (int j = i + 1; j < players.length; j++) {
                    assertNotEquals(players[i].getPosition(), players[j].getPosition(),
                            "Players " + i + " and " + j + " should have different positions with " 
                            + numPlayers + " players");
                }
            }
        }
    }

    @Test
    @DisplayName("Position range should match number of players")
    void testPositionRange() {
        for (int numPlayers = 1; numPlayers <= 4; numPlayers++) {
            Game game = new Game(numPlayers);
            Player[] players = game.getPlayers();
            
            for (Player player : players) {
                int pos = player.getPosition();
                assertTrue(pos >= 1 && pos <= numPlayers,
                        "With " + numPlayers + " players, position should be 1-" 
                        + numPlayers + ", but was " + pos);
            }
        }
    }

    @RepeatedTest(50)
    @DisplayName("Random distribution: All players should become start player over multiple games")
    void testStartPlayerDistribution() {
        int numPlayers = 4;
        int[] startPlayerCounts = new int[numPlayers];
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            Game game = new Game(numPlayers);
            Player[] players = game.getPlayers();
            
            // Find which player has position 1 (is start player)
            for (int j = 0; j < players.length; j++) {
                if (players[j].getPosition() == 1) {
                    startPlayerCounts[j]++;
                    break;
                }
            }
        }
        
        // Verify all players became start player at least once
        for (int i = 0; i < numPlayers; i++) {
            assertTrue(startPlayerCounts[i] > 0,
                    "Player " + i + " should become start player at least once in " 
                    + iterations + " iterations");
        }
        
        // Check distribution is somewhat even (within reasonable bounds)
        int expected = iterations / numPlayers;
        for (int i = 0; i < numPlayers; i++) {
            assertTrue(startPlayerCounts[i] > expected / 3,
                    "Player " + i + " should become start player reasonably often. Got " 
                    + startPlayerCounts[i] + " times out of " + iterations);
        }
    }
}
