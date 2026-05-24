package com.anno1800.agents.AgentImpl;

import com.anno1800.agents.GameContext;
import com.anno1800.agents.ObjectiveContext;
import com.anno1800.agents.StrategyWeights;
import com.anno1800.data.gamedata.Producers;
import com.anno1800.data.gamedata.ShipType;
import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.state.GameState;

/**
 * Agent that scores actions using configurable {@link StrategyWeights} combined
 * with context-sensitive bonuses derived from {@link GameContext}.
 *
 * <pre>
 *   score(action) = basicRules(action, context)
 *                 + baseWeight(actionType)          ← from StrategyWeights
 *                 + contextBonus(action, context)   ← situation-specific
 *                 + objectiveBonus(action)          ← from ObjectiveCards
 * </pre>
 *
 * Create agents with predefined strategies using the static factory methods:
 * <pre>
 *   WeightedScoringAgent.vpRush("Alice")
 *   WeightedScoringAgent.productionFocus("Bob")
 *   WeightedScoringAgent.expansionFocus("Carol")
 *   WeightedScoringAgent.balanced("Dave")
 * </pre>
 *
 * Or supply custom weights for fine-tuned experiments:
 * <pre>
 *   new WeightedScoringAgent("Custom", myWeights)
 * </pre>
 */
public class WeightedScoringAgent extends ScoringAgent {

    private final StrategyWeights baseWeights;  // Original strategy weights
    private StrategyWeights weights;             // Current weights (adjusted for objectives)

    public WeightedScoringAgent(String name, StrategyWeights weights, long seed) {
        super(name, seed);
        this.baseWeights = weights;
        this.weights = weights;  // Start with base weights
    }

    public WeightedScoringAgent(String name, StrategyWeights weights) {
        super(name);
        this.baseWeights = weights;
        this.weights = weights;  // Start with base weights
    }

    /**
     * Override to adjust strategy weights based on active ObjectiveCards.
     * This allows the agent to dynamically adapt its strategy to the game's scoring opportunities.
     */
    @Override
    public void setObjectiveContext(ObjectiveContext objectiveContext) {
        super.setObjectiveContext(objectiveContext);
        
        // Adjust weights based on objectives
        this.weights = baseWeights.adjustForObjectives(objectiveContext);
        
        System.out.println("[" + getName() + "] Strategy weights adjusted for ObjectiveCards:");
        System.out.println("  buildFactory: " + baseWeights.buildFactory() + " → " + weights.buildFactory());
        System.out.println("  upgradeResident: " + baseWeights.upgradeResident() + " → " + weights.upgradeResident());
        System.out.println("  fulfillNeeds: " + baseWeights.fulfillNeeds() + " → " + weights.fulfillNeeds());
    }

    // =========================================================================
    // Static factory methods
    // =========================================================================

    /** Ship Building: Viele Schiffe bauen. */
    public static WeightedScoringAgent shipBuilding(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.shipBuilding());
    }

    /** Trade Ship Focus: Viele Handelsschiffe bauen und viel handeln. */
    public static WeightedScoringAgent tradeShipFocus(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.tradeShipFocus());
    }

    /** Exploration Focus: Viele Expeditionschiffe / viele Inseln / viele Expeditionen. */
    public static WeightedScoringAgent explorationFocus(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.explorationFocus());
    }

    /** Resident Mass Production: Viele Residents / viele Baumaterial-Fabriken. */
    public static WeightedScoringAgent residentMassProduction(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.residentMassProduction());
    }

    /** Elite Resident Production: Hohe Resident-Levels / viele Baumaterial-Fabriken. */
    public static WeightedScoringAgent eliteResidentProduction(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.eliteResidentProduction());
    }

    /** Adaptive Resident Strategy: Angemessene Levelverteilung je nach Spielphase. */
    public static WeightedScoringAgent adaptiveResidentStrategy(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.adaptiveResidentStrategy());
    }

    /** Factory Variety: Große Variety an Factories. */
    public static WeightedScoringAgent factoryVariety(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.factoryVariety());
    }

    /** Balanced: Kein klarer Fokus – Entscheidung hauptsächlich durch Kontext. */
    public static WeightedScoringAgent balanced(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.balanced());
    }

    // =========================================================================
    // Core scoring
    // =========================================================================

    @Override
    protected double scoreAction(Action action, GameState gameState, Player player, 
                                GameContext context, ObjectiveContext objectiveContext) {
        return getBaseWeight(action)
             + getContextBonus(action, context, objectiveContext)
             + getObjectiveBonus(action, objectiveContext)
             + applySafetyRules(action, gameState, player);
    }

    /**
     * Hard safety rules that keep the agent from breaking its own production base.
     * These rules intentionally override the normal strategy weights.
     */
    private double applySafetyRules(Action action, GameState gameState, Player player) {
        if (player == null) {
            return 0.0;
        }

        PlayerBoard playerBoard = player.getPlayerBoard();
        long farmerCount = countResidentsAtLevel(playerBoard, 1);
        long workerCount = countResidentsAtLevel(playerBoard, 2);
        boolean hasGreenSawmill = hasFactoryType(playerBoard, Producers.SAWMILL_GREEN);
        boolean hasBlueSawmill = hasFactoryType(playerBoard, Producers.SAWMILL_BLUE);

        return switch (action) {
            case Action.BuildShips buildShips -> {
                int currentCount = shipCount(gameState, player, buildShips.shipType());
                int desiredLevel = buildShips.level();
                int currentHighestLevel = highestShipLevel(gameState, player, buildShips.shipType());

                if (currentHighestLevel >= desiredLevel && currentCount > 0) {
                    yield -1000.0;
                }

                if (buildShips.shipType() == ShipType.TradeShip && currentCount + buildShips.amount() < 2) {
                    yield -1000.0;
                }

                yield 0.0;
            }

            case Action.UpgradeResident upgradeResident -> {
                boolean upgradesFarmers = false;
                boolean upgradesWorkers = false;
                for (Resident resident : upgradeResident.residents()) {
                    if (resident == null) {
                        continue;
                    }
                    if (resident.getPopulationLevel() == 1) {
                        upgradesFarmers = true;
                    }
                    if (resident.getPopulationLevel() == 2) {
                        upgradesWorkers = true;
                    }
                }

                if (upgradesFarmers && farmerCount <= 2) {
                    yield -1000.0;
                }

                if (upgradesWorkers && workerCount <= 2) {
                    yield -1000.0;
                }

                if (upgradesWorkers && !hasGreenSawmill && !hasBlueSawmill) {
                    yield -1000.0;
                }

                yield 0.0;
            }

            case Action.OverbuildDefaultFactory overbuildDefaultFactory -> {
                Factory defaultFactory = overbuildDefaultFactory.defaultFactory();
                if (defaultFactory != null
                    && defaultFactory.getType() == Producers.SAWMILL_GREEN
                    && !hasBlueSawmill) {
                    yield -1000.0;
                }

                if (defaultFactory != null
                    && defaultFactory.getType() == Producers.SAWMILL_GREEN
                    && hasBlueSawmill
                    && workerCount <= 2) {
                    yield -1000.0;
                }

                yield 0.0;
            }

            default -> 0.0;
        };
    }

    private long countResidentsAtLevel(PlayerBoard playerBoard, int populationLevel) {
        return playerBoard.getResidents().stream()
            .filter(resident -> resident.getPopulationLevel() == populationLevel)
            .count();
    }

    private boolean hasFactoryType(PlayerBoard playerBoard, Producers type) {
        for (Factory factory : playerBoard.getFactories()) {
            if (factory != null && factory.getType() == type) {
                return true;
            }
        }
        return false;
    }

    private int shipCount(GameState gameState, Player player, ShipType shipType) {
        var playerState = findPlayerState(gameState, player);
        if (playerState == null) {
            return 0;
        }

        return switch (shipType) {
            case TradeShip -> playerState.ships().tradeShips().totalCount();
            case ExplorerShip -> playerState.ships().explorerShips().totalCount();
        };
    }

    private int highestShipLevel(GameState gameState, Player player, ShipType shipType) {
        var playerState = findPlayerState(gameState, player);
        if (playerState == null) {
            return 0;
        }

        return switch (shipType) {
            case TradeShip -> highestLevelFromCounts(playerState.ships().tradeShips().levels());
            case ExplorerShip -> highestLevelFromCounts(playerState.ships().explorerShips().levels());
        };
    }

    private int highestLevelFromCounts(GameState.BoardState.ShipState.ShipLevelCounts levels) {
        if (levels.level3() > 0) {
            return 3;
        }
        if (levels.level2() > 0) {
            return 2;
        }
        if (levels.level1() > 0) {
            return 1;
        }
        return 0;
    }

    private GameState.PlayerState findPlayerState(GameState gameState, Player player) {
        if (gameState == null || player == null) {
            return null;
        }

        for (GameState.PlayerState playerState : gameState.players()) {
            if (player.getName().equals(playerState.name())) {
                return playerState;
            }
        }

        return null;
    }

    // =========================================================================
    // Base weights: map each action type to its configured weight
    // =========================================================================

    /**
     * Returns the base weight for an action from the strategy weights.
     * 
     * <p>Only the 11 main actions that the agent can directly choose from are weighted:
     * <ol>
     *   <li>FulfillNeeds</li>
     *   <li>SettleResident</li>
     *   <li>UpgradeResident</li>
     *   <li>BuildFactory</li>
     *   <li>BuildShipyard</li>
     *   <li>BuildShips</li>
     *   <li>DiscoverOldWorldIsland</li>
     *   <li>DiscoverNewWorldIsland</li>
     *   <li>Expedition</li>
     *   <li>Carneval</li>
     *   <li>SwapResidentCards</li>
     * </ol>
     * 
     * <p>All other action types are sub-actions that are executed as part of the main actions
     * and should never appear in the agent's choice list. If they do, it indicates a bug
     * in the action generation logic.
     * 
     * @param action The action to get the weight for
     * @return The base weight from the strategy
     * @throws IllegalStateException if a sub-action is passed (should never happen)
     */
    private double getBaseWeight(Action action) {
        return switch (action) {
            // === 11 Main Actions: Direct agent choices ===
            case Action.FulfillNeeds ignored              -> weights.fulfillNeeds();
            case Action.SettleResident ignored            -> weights.settleResident();
            case Action.UpgradeResident ignored           -> weights.upgradeResident();
            case Action.BuildFactory ignored              -> weights.buildFactory();
            case Action.BuildShipyard ignored             -> weights.buildShipyard();
            case Action.BuildShips ignored                -> weights.buildShips();
            case Action.DiscoverOldWorldIsland ignored    -> weights.discoverOldWorldIsland();
            case Action.DiscoverNewWorldIsland ignored    -> weights.discoverNewWorldIsland();
            case Action.Expedition ignored                -> weights.expedition();
            case Action.Carneval ignored                  -> weights.carneval();
            case Action.SwapResidentCards ignored         -> weights.swapResidentCards();
            
            // === Sub-Actions: If they appear, heavily penalize instead of crashing ===
            // This keeps simulations robust even when utility/free actions are surfaced.
            default -> -1000.0;
        };
    }

    // =========================================================================
    // Context bonuses: situation-specific additions on top of the base weight
    // =========================================================================

    private double getContextBonus(Action action, GameContext context, ObjectiveContext objectiveContext) {
        return switch (action) {
            case Action.FulfillNeeds a                   -> bonusFulfillNeeds(a, context, objectiveContext);
            case Action.UpgradeResident a                -> bonusUpgradeResident(a, context);
            case Action.SettleResident a                 -> bonusSettleResident(a, context);
            case Action.BuildFactory ignored             -> bonusBuildFactory(context);
            case Action.OverbuildDefaultFactory ignored  -> bonusBuildFactory(context);
            case Action.BuildShipyard ignored            -> bonusBuildShipyard(context);
            case Action.DiscoverNewWorldIsland ignored   -> bonusDiscoverNewWorld(context);
            case Action.DrawResidentCard a               -> bonusDrawResidentCard(a, context, objectiveContext);
            case Action.Expedition ignored               -> bonusExpedition(context);
            default                                      -> 0.0;
        };
    }

    /**
     * FulfillNeeds:
     * +VP-Wert der Karte (3/5/8 je nach Bevölkerungsstufe)
     * +15 wenn dies meine letzte Karte ist (triggert Endphase + 7 Feuerwerks-Bonuspunkte)
     * Berücksichtigt ResidentCardsPenalty bei der Endspielentscheidung
     * 
     * <p>Spielende-Mechanik:
     * - Wenn ein Spieler seine letzte Karte spielt (1 → 0 Karten), wird die Endphase ausgelöst
     * - Die aktuelle Runde wird zu Ende gespielt, dann eine letzte volle Runde
     * - Am Spielende: Feuerwerk gibt 7 VP für das Auslösen der Endphase
     * - Aber: Mit ResidentCardsPenalty verliert man -2 VP pro Karte auf der Hand
     */
    private double bonusFulfillNeeds(Action.FulfillNeeds action, GameContext context, 
                                     ObjectiveContext objectiveContext) {
        double bonus = 0.0;
        ResidentCard card = action.residentCard();
        if (card != null) {
            // Base VP value of the card
            double cardVP = switch (card.populationLevel()) {
                case 2, 3 -> 3.0;  // Bauer/Arbeiter: 3 VP
                case 4, 5 -> 8.0;  // Handwerker/Ingenieur/Investor: 8 VP
                case 7    -> 5.0;  // Neue-Welt-Karten: 5 VP
                default   -> 2.0;
            };
            
            // Scale VP value based on game phase
            // Early game: VP less important (invest in engine building)
            // Late game: VP critical (score aggressively)
            double vpScale = calculateVPScale(context, objectiveContext);
            bonus += cardVP * vpScale;
        }
        
        // Letzte Karte spielen → Endphase auslösen
        // myCardCount() ist die Anzahl VOR dem Spielen, also:
        // - myCardCount() == 1 bedeutet: Nach dieser Action habe ich 0 Karten → Endphase!
        if (context.myCardCount() <= 1) {
            // Großer Bonus: 7 VP Feuerwerk + strategischer Vorteil (Gegner haben weniger Zeit)
            bonus += 15.0;
        }
        
        // ResidentCardsPenalty: -2 VP pro Karte auf der Hand am Spielende
        // Je mehr Karten ich aktuell habe, desto wichtiger ist es, sie loszuwerden
        if (objectiveContext.hasResidentCardPenalty()) {
            int cardsAfterPlaying = context.myCardCount() - 1;
            if (cardsAfterPlaying > 0) {
                // Basis-Bonus: Spare 2 VP Malus + skaliert mit verbleibenden Karten
                double penaltyBonus = 2.0 + (cardsAfterPlaying * 0.5);
                
                // Kompetitiver Bonus: Gegner-Handkarten berücksichtigen
                double competitiveBonus = 0.0;
                
                // 1. Individueller Gegner-Bonus: Höchster Bonus wenn ein Gegner < 5 Karten hat
                // Betrachtet nur den Gegner mit den wenigsten Karten (nicht alle aufsummiert)
                double maxIndividualBonus = 0.0;
                for (int opponentCards : context.opponentCardCounts()) {
                    if (opponentCards < 5) {
                        // 4 Karten → +0.5, 3 → +1.0, 2 → +1.5, 1 → +2.0, 0 → +2.5
                        double individualBonus = (5 - opponentCards) * 0.5;
                        maxIndividualBonus = Math.max(maxIndividualBonus, individualBonus);
                    }
                }
                competitiveBonus += maxIndividualBonus;
                
                // 2. Durchschnitts-Bonus: 0 bei 9 Karten (neutral), linear skaliert
                // Bei < 9 → positiv (Gegner spielen viel → ich sollte auch spielen)
                // Bei > 9 → negativ (Gegner halten viele → vielleicht sollte ich auch halten)
                double avgBonus = (9.0 - context.avgOpponentCardCount()) * 0.3;
                competitiveBonus += avgBonus;
                
                // Individueller Bonus dominiert (Gewichtung 0.5 vs 0.3 pro Karte)
                bonus += penaltyBonus + competitiveBonus;
            }
        }
        
        return bonus;
    }

    /**
     * UpgradeResident:
     * +Zielstufe × 1.5 pro Bewohner.
     * Skaliert runter wenn Spiel bald endet (Investment lohnt sich nicht mehr).
     */
    private double bonusUpgradeResident(Action.UpgradeResident action, GameContext context) {
        if (action.residents() == null) return 0.0;
        double scale = context.isEndPhase() ? 0.3 : 1.0;
        double bonus = 0.0;
        for (Resident r : action.residents()) {
            bonus += (r.getPopulationLevel() + 1) * 1.5 * scale;
        }
        return bonus;
    }

    /**
     * SettleResident:
     * +Stufe × 1.5. Weniger wert wenn Spiel fast vorbei.
     */
    private double bonusSettleResident(Action.SettleResident action, GameContext context) {
        double scale = context.isEndPhase() ? 0.3 : 1.0;
        return action.level() * 1.5 * scale;
    }

    /**
     * BuildFactory:
     * -5 wenn keine freien Landplätze. Kein Bonus wenn Spiel fast vorbei.
     */
    private double bonusBuildFactory(GameContext context) {
        if (context.freeLandTiles() <= 0) return -5.0;
        return context.isEndPhase() ? 0.0 : 2.0;
    }

    /**
     * BuildShipyard:
     * -3 wenn keine freien Küstenplätze.
     */
    private double bonusBuildShipyard(GameContext context) {
        if (context.freeCoastTiles() <= 0) return -3.0;
        return context.isEndPhase() ? 0.0 : 1.0;
    }

    /**
     * DiscoverNewWorldIsland:
     * +3 wenn Entdecker-Schiffe vorhanden. -2 wenn Spiel fast vorbei.
     */
    private double bonusDiscoverNewWorld(GameContext context) {
        if (context.isEndPhase()) return -2.0;
        return context.canExplore() ? 3.0 : 0.0;
    }

    /**
     * DrawResidentCard:
     * +Stufe × 1.5. Kaum Wert wenn Spiel fast vorbei.
     * Stark reduziert wenn ResidentCardsPenalty aktiv ist (jede Karte = -2 VP am Ende).
     */
    private double bonusDrawResidentCard(Action.DrawResidentCard action, GameContext context,
                                         ObjectiveContext objectiveContext) {
        double scale = context.isEndPhase() ? 0.1 : 1.0;
        
        // Wenn ResidentCardsPenalty aktiv: Karten ziehen ist riskanter
        if (objectiveContext.hasResidentCardPenalty() && !context.isEndPhase()) {
            scale *= 0.6; // Reduziere Wert um 40%
        }
        
        return action.populationLevel() * 1.5 * scale;
    }

    /**
     * Expedition:
     * +2 wenn noch viele Runden verbleiben. -1 wenn Spiel fast vorbei.
     */
    private double bonusExpedition(GameContext context) {
        if (context.isEndPhase()) return -1.0;
        return context.estimatedRoundsLeft() >= 3 ? 2.0 : 0.0;
    }

    // =========================================================================
    // Game Phase Analysis
    // =========================================================================
    
    /**
     * Calculate VP scaling factor based on game phase.
     * 
     * <p>Game phases:
     * <ul>
     *   <li><b>Early game (> 60% remaining)</b>: Scale 0.5-0.6 → prioritize engine building</li>
     *   <li><b>Mid game (30-60% remaining)</b>: Scale 0.8-1.0 → balanced approach</li>
     *   <li><b>Late game (10-30% remaining)</b>: Scale 1.2-1.4 → VP important</li>
     *   <li><b>End game (< 10% remaining)</b>: Scale 1.5-1.8 → VP critical</li>
     * </ul>
     * 
     * @param context Current game context (rounds left)
     * @param objectiveContext Game setup context (expected game length)
     * @return Scaling factor for VP bonuses (0.5 to 1.8)
     */
    private double calculateVPScale(GameContext context, ObjectiveContext objectiveContext) {
        // If in end phase, use remaining turns for precise calculation
        if (context.isEndPhase()) {
            return switch (context.remainingTurns()) {
                case 1 -> 1.8;  // Last turn: VP rush!
                case 2 -> 1.5;  // Penultimate turn: VP very important
                default -> 1.4; // End phase active but more turns left
            };
        }
        
        // Calculate game progress percentage
        int expectedLength = objectiveContext.expectedGameLength();
        int roundsLeft = context.estimatedRoundsLeft();
        double progressPercent = 1.0 - ((double) roundsLeft / expectedLength);
        
        // Phase-based scaling
        if (progressPercent < 0.4) {
            // Early game: 0-40% progress → scale 0.5 to 0.7
            return 0.5 + (progressPercent * 0.5);  // 0.5 at 0%, 0.7 at 40%
        } else if (progressPercent < 0.7) {
            // Mid game: 40-70% progress → scale 0.7 to 1.1
            double midProgress = (progressPercent - 0.4) / 0.3;  // 0.0 to 1.0
            return 0.7 + (midProgress * 0.4);  // 0.7 at 40%, 1.1 at 70%
        } else if (progressPercent < 0.9) {
            // Late game: 70-90% progress → scale 1.1 to 1.4
            double lateProgress = (progressPercent - 0.7) / 0.2;  // 0.0 to 1.0
            return 1.1 + (lateProgress * 0.3);  // 1.1 at 70%, 1.4 at 90%
        } else {
            // Very late game: 90%+ progress → scale 1.4 to 1.5
            return 1.4 + ((progressPercent - 0.9) / 0.1 * 0.1);  // 1.4 at 90%, 1.5 at 100%
        }
    }

    // =========================================================================
    // Objective bonuses: strategy adjustments based on active ObjectiveCards
    // =========================================================================

    /**
     * Adds bonuses based on active ObjectiveCards.
     * This evaluates fixed parameters that were pre-computed at game start.
     * 
     * Examples:
     * - If MostInvestors is active, boost UpgradeResident(5) actions
     * - If NewWorldExplorer is active, boost DiscoverNewWorldIsland
     * - If specific factory objectives are active, boost BuildFactory for those types
     */
    private double getObjectiveBonus(Action action, ObjectiveContext objectiveContext) {
        double bonus = 0.0;
        
        // Example: Boost strategies aligned with active objectives
        switch (action) {
            case Action.UpgradeResident a -> {
                // If MostInvestors objective is active, prioritize upgrading to level 5
                if (objectiveContext.hasObjective(ObjectiveCard.MostInvestors.class)) {
                    for (var resident : a.residents()) {
                        if (resident.getPopulationLevel() == 4) { // Upgrading to Investor (5)
                            bonus += 3.0;
                        }
                    }
                }
                // Similar for MostEngineers
                if (objectiveContext.hasObjective(ObjectiveCard.MostEngineers.class)) {
                    for (var resident : a.residents()) {
                        if (resident.getPopulationLevel() == 3) { // Upgrading to Engineer (4)
                            bonus += 3.0;
                        }
                    }
                }
            }
            
            case Action.DiscoverNewWorldIsland ignored -> {
                // If NewWorldExplorer is active (6 VP per island), boost this action
                if (objectiveContext.hasObjective(ObjectiveCard.NewWorldExplorer.class)) {
                    bonus += 4.0;
                }
            }
            
            case Action.Expedition ignored -> {
                // If expedition-related objectives are active, boost expeditions
                if (objectiveContext.hasObjective(ObjectiveCard.MostExpeditionCards.class) ||
                    objectiveContext.hasObjective(ObjectiveCard.ArtifactBonus.class) ||
                    objectiveContext.hasObjective(ObjectiveCard.AnimalBonus.class)) {
                    bonus += 3.0;
                }
            }
            
            // Note: Factory-specific bonuses would require knowing the factory type
            // from the action, which may not be available in the Action object itself.
            // These could be added in more detailed subclasses or with action introspection.
            
            default -> {}
        }
        
        return bonus;
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public StrategyWeights getWeights() {
        return weights;
    }
}
