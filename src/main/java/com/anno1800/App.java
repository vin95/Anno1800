package com.anno1800;

import com.anno1800.agents.AgentImpl.AgentRandom;
import com.anno1800.game.engine.Game;

public class App {
    public static void main(String[] args) {
        int numPlayers = 3;
        Game game = new Game(numPlayers);
        
        // Assign RandomAgents to all players
        for (int i = 0; i < numPlayers; i++) {
            game.setAgent(i, new AgentRandom("RandomAgent-" + (i + 1)));
        }
        
        // Start the game
        game.start();
    }
}
