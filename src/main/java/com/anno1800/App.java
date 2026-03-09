package com.anno1800;

import com.anno1800.agents.AgentImpl.WeightedScoringAgent;
import com.anno1800.game.engine.Game;

public class App {
    public static void main(String[] args) {
        int numPlayers = 3;
        Game game = new Game(numPlayers);
        
        // Assign different WeightedScoringAgents to test various strategies
        game.setAgent(0, WeightedScoringAgent.adaptiveResidentStrategy("AdaptivePlayer"));
        game.setAgent(1, WeightedScoringAgent.balanced("BalancedPlayer"));
        game.setAgent(2, WeightedScoringAgent.factoryVariety("FactoryBuilder"));
        
        // Start the game
        game.start();
    }
}
