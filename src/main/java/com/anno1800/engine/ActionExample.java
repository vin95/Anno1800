package com.anno1800.engine;

import com.anno1800.actions.Action;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Producers;
import com.anno1800.data.FactoryData;

/**
 * Example showing how to execute actions through the Game class.
 */
public class ActionExample {
    
    public static void main(String[] args) {
        // 1. Create game
        Game game = new Game(2);
        
        System.out.println("=== Action Execution Demo ===\n");
        System.out.printf("Current Player: %s%n", game.getCurrentPlayer().getName());
        System.out.printf("Round: %d, Phase: %s%n%n", 
            game.getCurrentRound(), 
            game.getCurrentPhase());
        
        // 2. Create an action
        Factory factory = (Factory) FactoryData.getProducer(Producers.SAWMILL_GREEN);
        Action buildAction = new Action.BuildFactory(factory);
        
        // 3. Execute action through Game
        System.out.println("Executing BuildFactory action...");
        boolean success = game.executeAction(buildAction);
        
        if (success) {
            System.out.println("✓ Action executed successfully!");
        } else {
            System.out.println("✗ Action failed (not implemented yet)");
        }
        
        // 4. More action examples
        System.out.println("\n--- More Action Examples ---");
        
        // Settle a resident
        Action settleAction = new Action.SettleResident(1);
        System.out.println("\nExecuting SettleResident...");
        try {
            game.executeAction(settleAction);
        } catch (UnsupportedOperationException e) {
            System.out.println("Not implemented yet: " + e.getMessage());
        }
        
        // End phase
        Action endPhaseAction = new Action.EndCurrentPhase();
        System.out.println("\nExecuting EndCurrentPhase...");
        try {
            game.executeAction(endPhaseAction);
        } catch (UnsupportedOperationException e) {
            System.out.println("Not implemented yet: " + e.getMessage());
        }
        
        System.out.println("\n=== Demo Complete ===");
        System.out.println("Implement the action logic in ActionHandler.java!");
    }
}
