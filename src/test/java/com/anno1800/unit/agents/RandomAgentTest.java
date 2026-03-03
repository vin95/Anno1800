package com.anno1800.unit.agents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.anno1800.agents.AgentImpl.AgentRandom;
import com.anno1800.game.actions.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test suite for AgentRandom.
 */
@DisplayName("AgentRandom Tests")
class RandomAgentTest {

    private AgentRandom agent;
    private AgentRandom namedAgent;

    @BeforeEach
    void setUp() {
        agent = new AgentRandom();
        namedAgent = new AgentRandom("TestAgent");
    }

    /**
     * Tests that the default constructor creates an agent with the default name.
     */
    @Test
    @DisplayName("Default constructor should create agent with default name")
    void testDefaultConstructor() {
        AgentRandom defaultAgent = new AgentRandom();
        assertNotNull(defaultAgent, "Agent should not be null");
        assertEquals("AgentRandom", defaultAgent.getName(), "Default name should be 'AgentRandom'");
    }

    /**
     * Tests that the parameterized constructor creates an agent with the specified name.
     */
    @Test
    @DisplayName("Parameterized constructor should create agent with custom name")
    void testParameterizedConstructor() {
        assertEquals("TestAgent", namedAgent.getName(), "Agent name should match constructor parameter");
    }

    /**
     * Tests that selectAction returns null when the action list is empty.
     */
    @Test
    @DisplayName("selectAction should return null for empty action list")
    void testSelectActionWithEmptyList() {
        List<Action> emptyList = new ArrayList<>();
        Action result = agent.selectAction(null, emptyList, null);
        
        assertNull(result, "Should return null when action list is empty");
    }

    /**
     * Tests that selectAction returns null when the action list is null.
     */
    @Test
    @DisplayName("selectAction should return null for null action list")
    void testSelectActionWithNullList() {
        Action result = agent.selectAction(null, null, null);
        
        assertNull(result, "Should return null when action list is null");
    }

    /**
     * Tests that selectAction returns a valid action from the provided list.
     * The returned action should be one of the actions in the list.
     */
    @Test
    @DisplayName("selectAction should return an action from the provided list")
    void testSelectActionReturnsValidAction() {
        List<Action> actions = createMockActions(5);
        
        Action selectedAction = agent.selectAction(null, actions, null);
        
        assertNotNull(selectedAction, "Should return an action");
        assertTrue(actions.contains(selectedAction), "Returned action should be from the provided list");
    }

    /**
     * Tests that selectAction works with a single action in the list.
     */
    @Test
    @DisplayName("selectAction should return the only action when list has one element")
    void testSelectActionWithSingleAction() {
        List<Action> actions = createMockActions(1);
        Action expectedAction = actions.get(0);
        
        Action selectedAction = agent.selectAction(null, actions, null);
        
        assertNotNull(selectedAction, "Should return an action");
        assertEquals(expectedAction, selectedAction, "Should return the only available action");
    }

    /**
     * Tests that over many selections, all actions are eventually selected.
     * This verifies that the random selection has proper distribution.
     */
    @Test
    @DisplayName("selectAction should eventually select all actions over many iterations")
    void testRandomDistribution() {
        List<Action> actions = createMockActions(5);
        Map<Action, Integer> selectionCount = new HashMap<>();
        
        // Initialize counts
        for (Action action : actions) {
            selectionCount.put(action, 0);
        }
        
        // Perform many selections
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            Action selected = agent.selectAction(null, actions, null);
            selectionCount.put(selected, selectionCount.get(selected) + 1);
        }
        
        // Verify all actions were selected at least once
        for (Action action : actions) {
            int count = selectionCount.get(action);
            assertTrue(count > 0, "Action " + action + " should be selected at least once in " + iterations + " iterations");
            
            // Very loose distribution check - each action should get roughly 1/5 of selections
            // Allow for significant variance due to randomness (at least 10% of expected)
            int expected = iterations / actions.size();
            assertTrue(count > expected / 10, 
                "Action " + action + " should be selected more than " + (expected/10) + " times, but was " + count);
        }
    }

    /**
     * Tests that getName returns the correct name.
     */
    @Test
    @DisplayName("getName should return the agent's name")
    void testGetName() {
        assertEquals("AgentRandom", agent.getName(), "Default agent name should be 'AgentRandom'");
        assertEquals("TestAgent", namedAgent.getName(), "Named agent should return custom name");
    }

    /**
     * Tests that different AgentRandom instances make independent random choices.
     */
    @Test
    @DisplayName("Different AgentRandom instances should be independent")
    void testIndependence() {
        AgentRandom agent1 = new AgentRandom();
        AgentRandom agent2 = new AgentRandom();
        List<Action> actions = createMockActions(10);
        
        // It's very unlikely that two agents will make the same 10 choices
        int sameChoices = 0;
        for (int i = 0; i < 10; i++) {
            Action choice1 = agent1.selectAction(null, actions, null);
            Action choice2 = agent2.selectAction(null, actions, null);
            if (choice1 == choice2) {
                sameChoices++;
            }
        }
        
        // With 10 actions, probability of all 10 being the same is (1/10)^10 which is extremely low
        assertTrue(sameChoices < 10, "Two random agents should not make all the same choices");
    }

    /**
     * Helper method to create a list of mock actions for testing.
     * Uses simple parameterless Action types to create unique action instances.
     * Note: Since Actions are records, instances with same parameters are equal.
     * We cycle through different action types to ensure uniqueness.
     */
    private List<Action> createMockActions(int count) {
        List<Action> actions = new ArrayList<>();
        
        // Array of simple action types without parameters
        Action[] actionTypes = {
            new Action.Carneval(),
            new Action.ViewResidentCards(),
            new Action.UseExtraAction(),
            new Action.Expedition(),
            new Action.DiscoverOldWorldIsland(),
            new Action.DiscoverNewWorldIsland()
        };
        
        // Cycle through action types to fill the list
        for (int i = 0; i < count; i++) {
            actions.add(actionTypes[i % actionTypes.length]);
        }
        
        return actions;
    }
}
