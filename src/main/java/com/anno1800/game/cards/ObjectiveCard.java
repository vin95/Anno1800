package com.anno1800.game.cards;

import com.anno1800.data.gamedata.Producers;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

import java.util.*;

/**
 * ObjectiveCards provide additional scoring opportunities and action modifications.
 * 5 cards are drawn at game start and remain active throughout the game.
 */
public sealed interface ObjectiveCard {
    
    /**
     * Get the title/name of this objective card.
     */
    String getTitle();
    
    /**
     * Get the description of this objective card.
     */
    String getDescription();
    
    /**
     * Calculate victory points for all players based on this objective.
     * This is called at the end of the game.
     * 
     * @param players All players in the game
     * @return Map of player to victory points awarded
     */
    default Map<Player, Integer> calculateEndGamePoints(Player[] players) {
        return Map.of();
    }
    
    /**
     * Check if this objective modifies action validation or execution.
     */
    default boolean modifiesActions() {
        return false;
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 1: Most Investors
    // ============================================================================================
    
    /**
     * Awards 10 points to player(s) with most Investors (popLv 5).
     * Awards 4 points to player(s) with second-most Investors.
     * Ties share the same ranking.
     */
    record MostInvestors() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Investment Tycoon";
        }
        
        @Override
        public String getDescription() {
            return "Most Investors (Level 5): 1st place = 10 VP, 2nd place = 4 VP";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            return calculateMostResidentsPoints(players, 5, 10, 4);
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 2: Most Engineers
    // ============================================================================================
    
    /**
     * Awards 10 points to player(s) with most Engineers (popLv 4).
     * Awards 4 points to player(s) with second-most Engineers.
     * Ties share the same ranking.
     */
    record MostEngineers() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Industrial Leader";
        }
        
        @Override
        public String getDescription() {
            return "Most Engineers (Level 4): 1st place = 10 VP, 2nd place = 4 VP";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            return calculateMostResidentsPoints(players, 4, 10, 4);
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 3: New World Islands
    // ============================================================================================
    
    /**
     * Awards 6 points per New World Island discovered.
     */
    record NewWorldExplorer() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "New World Explorer";
        }
        
        @Override
        public String getDescription() {
            return "6 VP per New World Island discovered";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                int islandCount = player.getPlayerBoard().getNumNewWorldIslands();
                int pts = islandCount * 6;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 4: Basic Goods Factories
    // ============================================================================================
    
    /**
     * Awards 3 points per basic goods factory:
     * BREWERY, BAKERY, DISTILLERY, SLAUGHTERHOUSE, SOAP_FACTORY, CANNERY, FRAMEWORK_KNITTERS
     */
    record BasicGoodsProducer() implements ObjectiveCard {
        
        private static final Set<Producers> COUNTED_FACTORIES = Set.of(
            Producers.BREWERY_BLUE,
            Producers.BAKERY_BLUE,
            Producers.DISTILLERY_BLUE,
            Producers.SLAUGHTERHOUSE_BLUE,
            Producers.SOAP_FACTORY_BLUE,
            Producers.CANNERY_BLUE,
            Producers.FRAMEWORK_KNITTERS_BLUE
        );
        
        @Override
        public String getTitle() {
            return "Basic Goods Producer";
        }
        
        @Override
        public String getDescription() {
            return "3 VP per Factory: Brewery, Bakery, Distillery, Slaughterhouse, Soap Factory, Cannery, Framework Knitters";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                long factoryCount = Arrays.stream(player.getPlayerBoard().getFactories())
                    .filter(f -> COUNTED_FACTORIES.contains(f.getType()))
                    .count();
                int pts = (int) factoryCount * 3;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 5: Trade with Explorer Chips
    // ============================================================================================
    
    /**
     * Allows using 2 Explorer Chips instead of 1 Trade Chip for:
     * - TradeGoods
     * - FulfillNeeds
     * - ImportGood
     */
    record ExplorerTrader() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Explorer Trader";
        }
        
        @Override
        public String getDescription() {
            return "Use 2 Explorer Chips instead of 1 Trade Chip for trading/fulfilling needs/importing goods";
        }
        
        @Override
        public boolean modifiesActions() {
            return true;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 6: Luxury Factories (Gramophones, Highbikes, Cars)
    // ============================================================================================
    
    /**
     * Awards 6 points per luxury factory: Gramophones, Highbikes, Cars.
     */
    record LuxuryFactories() implements ObjectiveCard {
        
        private static final Set<Producers> COUNTED_FACTORIES = Set.of(
            Producers.GRAMOPHONE_FACTORY_PURPLE,
            Producers.BICYCLE_FACTORY_PURPLE,
            Producers.CAR_FACTORY_PURPLE
        );
        
        @Override
        public String getTitle() {
            return "Luxury Manufacturer";
        }
        
        @Override
        public String getDescription() {
            return "6 VP per Factory: Gramophones, Highbikes, Cars";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                long factoryCount = Arrays.stream(player.getPlayerBoard().getFactories())
                    .filter(f -> COUNTED_FACTORIES.contains(f.getType()))
                    .count();
                int pts = (int) factoryCount * 6;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 7: Single Island Bonus
    // ============================================================================================
    
    /**
     * Awards 18 points if player has at most one OldWorldIsland at end of game.
     */
    record SingleIslandBonus() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Single Island Master";
        }
        
        @Override
        public String getDescription() {
            return "18 VP if you have at most 1 Old World Island";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                int islandCount = player.getPlayerBoard().getNumOldWorldIslands();
                if (islandCount <= 1) {
                    points.put(player, 18);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 8: Most Expedition Cards
    // ============================================================================================
    
    /**
     * Awards 10 points to player(s) with most Expedition cards.
     * Awards 4 points to player(s) with second-most Expedition cards.
     */
    record MostExpeditionCards() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Expedition Master";
        }
        
        @Override
        public String getDescription() {
            return "Most Expedition Cards: 1st place = 10 VP, 2nd place = 4 VP";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            return calculateMostOfTypePoints(players, 
                p -> p.getPlayerBoard().getExpeditionCards().size(), 
                10, 4);
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 9: Extra Action (Free Action)
    // ============================================================================================
    
    /**
     * Free Action: Pay 3 Gold and 3 Explorer Chips to take an additional action this turn.
     * Can only be used once per turn.
     */
    record ExtraAction() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Extra Action";
        }
        
        @Override
        public String getDescription() {
            return "FREE ACTION: Pay 3 Gold + 3 Explorer Chips for an additional action (1x per turn)";
        }
        
        @Override
        public boolean modifiesActions() {
            return true;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 10: Museum Bonus (Artifact Bonus)
    // ============================================================================================
    
    /**
     * Museum: Adds +1 VP to each artifact slot that has a visitor.
     * Rule: "Jede dieser Karten erhöht die Einfluss-Punkte, die ihr für 
     *        besuchte Artefakte bekommt, um 1."
     * 
     * This means: A visited artifact slot gives its base VP + 1 bonus from this card.
     * Example: Ingenieur (2 VP slot) + Museum card = 3 VP total for that slot.
     */
    record ArtifactBonus() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Museum";
        }
        
        @Override
        public String getDescription() {
            return "+1 VP per visited Artifact (adds to expedition scoring)";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                int artifactCount = 0;
                for (ExpeditionCard card : player.getPlayerBoard().getExpeditionCards()) {
                    if (card.hasArtifact()) {
                        artifactCount++;
                    }
                }
                if (artifactCount > 0) {
                    points.put(player, artifactCount);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 11: New World Product Factories
    // ============================================================================================
    
    /**
     * Awards 6 points per New World product factory: Chocolate, Coffee, Cigars, Rum.
     */
    record NewWorldProductFactories() implements ObjectiveCard {
        
        private static final Set<Producers> COUNTED_FACTORIES = Set.of(
            Producers.CHOCOLATE_FACTORY_RED,
            Producers.COFFEE_ROASTERS_RED,
            Producers.CIGAR_FACTORY_RED,
            Producers.RUM_DISTILLERY_RED
        );
        
        @Override
        public String getTitle() {
            return "New World Importer";
        }
        
        @Override
        public String getDescription() {
            return "6 VP per Factory: Chocolate, Coffee, Cigars, Rum";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                long factoryCount = Arrays.stream(player.getPlayerBoard().getFactories())
                    .filter(f -> COUNTED_FACTORIES.contains(f.getType()))
                    .count();
                int pts = (int) factoryCount * 6;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 12: Zoo Bonus (Animal Bonus)
    // ============================================================================================
    
    /**
     * Zoo: Adds +1 VP to each animal slot that has a visitor.
     * Rule: "Jede dieser Karten erhöht die Einfluss-Punkte, die ihr für 
     *        besuchte Tiere bekommt, um 1."
     * 
     * This means: A visited animal slot gives its base VP + 1 bonus from this card.
     * Example: Ingenieur (2 VP slot) + Zoo card = 3 VP total for that slot.
     */
    record AnimalBonus() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Zoo";
        }
        
        @Override
        public String getDescription() {
            return "+1 VP per visited Animal (adds to expedition scoring)";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                int animalCount = 0;
                for (ExpeditionCard card : player.getPlayerBoard().getExpeditionCards()) {
                    if (card.hasAnimal()) {
                        animalCount++;
                    }
                }
                if (animalCount > 0) {
                    points.put(player, animalCount);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 13: Artisan Goods Factories (Pocketwatches, Spectacles, Coats)
    // ============================================================================================
    
    /**
     * Awards 6 points per artisan factory: Pocketwatches, Spectacles, Coats.
     */
    record ArtisanGoodsFactories() implements ObjectiveCard {
        
        private static final Set<Producers> COUNTED_FACTORIES = Set.of(
            Producers.CLOCKMAKERS_RED,
            Producers.SPECTACLE_FACTORY_RED,
            Producers.FUR_DEALER_RED
        );
        
        @Override
        public String getTitle() {
            return "Artisan Master";
        }
        
        @Override
        public String getDescription() {
            return "6 VP per Factory: Pocketwatches, Spectacles, Coats";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                long factoryCount = Arrays.stream(player.getPlayerBoard().getFactories())
                    .filter(f -> COUNTED_FACTORIES.contains(f.getType()))
                    .count();
                int pts = (int) factoryCount * 6;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 14: Most Trade Chips (Madame Kahina)
    // ============================================================================================
    
    /**
     * Madame Kahina: Awards 10 points to player(s) with most Trade Chips.
     * Awards 4 points to player(s) with second-most Trade Chips.
     * Trade Chips are counted by summing all Trade Ship levels (each level = 1 chip capacity).
     */
    record MostTradeChips() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Madame Kahina";
        }
        
        @Override
        public String getDescription() {
            return "Most Trade Chips: 1st place = 10 VP, 2nd place = 4 VP";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            return calculateMostOfTypePoints(players, 
                p -> p.getPlayerBoard().getTradeShips().stream()
                    .mapToInt(ship -> ship.getLevel())
                    .sum(), 
                10, 4);
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 15: Prestige Factories (Champagne, Big Berta)
    // ============================================================================================
    
    /**
     * Awards 6 points per prestige factory: Champagne, Big Berta.
     */
    record PrestigeFactories() implements ObjectiveCard {
        
        private static final Set<Producers> COUNTED_FACTORIES = Set.of(
            Producers.CHAMPAGNE_CELLAR_RED,
            Producers.HEAVY_WEAPONS_FACTORY_PURPLE
        );
        
        @Override
        public String getTitle() {
            return "Prestige Producer";
        }
        
        @Override
        public String getDescription() {
            return "6 VP per Factory: Champagne, Big Berta";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                long factoryCount = Arrays.stream(player.getPlayerBoard().getFactories())
                    .filter(f -> COUNTED_FACTORIES.contains(f.getType()))
                    .count();
                int pts = (int) factoryCount * 6;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 16: Resident Cards Penalty
    // ============================================================================================
    
    /**
     * Awards -2 points per Resident Card still in hand at end of game.
     */
    record ResidentCardsPenalty() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Resident Cards Penalty";
        }
        
        @Override
        public String getDescription() {
            return "-2 VP per Resident Card still in hand";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                int cardCount = player.getPlayerBoard().getResidentCards().size();
                if (cardCount > 0) {
                    points.put(player, -2 * cardCount);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 17: Engineer Goods Factories (Light Bulbs, Sewing Machines)
    // ============================================================================================
    
    /**
     * Awards 6 points per engineer factory: Light Bulbs, Sewing Machines.
     */
    record EngineerGoodsFactories() implements ObjectiveCard {
        
        private static final Set<Producers> COUNTED_FACTORIES = Set.of(
            Producers.LIGHT_BULB_FACTORY_PURPLE,
            Producers.SEWING_MACHINE_FACTORY_RED
        );
        
        @Override
        public String getTitle() {
            return "Engineer Master";
        }
        
        @Override
        public String getDescription() {
            return "6 VP per Factory: Light Bulbs, Sewing Machines";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            Map<Player, Integer> points = new HashMap<>();
            for (Player player : players) {
                long factoryCount = Arrays.stream(player.getPlayerBoard().getFactories())
                    .filter(f -> COUNTED_FACTORIES.contains(f.getType()))
                    .count();
                int pts = (int) factoryCount * 6;
                if (pts > 0) {
                    points.put(player, pts);
                }
            }
            return points;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 18: Discard Resident Card (Free Action)
    // ============================================================================================
    
    /**
     * Free Action: Pay 2 Explorer Chips to discard 1 Resident Card (place under deck).
     * Can only be used once per turn.
     */
    record DiscardResidentCard() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Discard Resident Card";
        }
        
        @Override
        public String getDescription() {
            return "FREE ACTION: Pay 2 Explorer Chips to return 1 Resident Card under deck (1x per turn)";
        }
        
        @Override
        public boolean modifiesActions() {
            return true;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 19: Investor Exhaust for Gold (Free Action)
    // ============================================================================================
    
    /**
     * Free Action: Exhaust 1 Investor (PopLv 5) to gain 5 Gold.
     * Can only be used once per turn.
     */
    record InvestorExhaustForGold() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Investor Exhaust for Gold";
        }
        
        @Override
        public String getDescription() {
            return "FREE ACTION: Exhaust 1 Investor (Fit) to gain 5 Gold (1x per turn)";
        }
        
        @Override
        public boolean modifiesActions() {
            return true;
        }
    }
    
    // ============================================================================================
    // OBJECTIVE CARD 20: Most Residents
    // ============================================================================================
    
    /**
     * Awards 10 points to player(s) with most Residents (total).
     * Awards 4 points to player(s) with second-most Residents.
     */
    record MostResidentsTotal() implements ObjectiveCard {
        @Override
        public String getTitle() {
            return "Population Leader";
        }
        
        @Override
        public String getDescription() {
            return "Most Residents (total): 1st place = 10 VP, 2nd place = 4 VP";
        }
        
        @Override
        public Map<Player, Integer> calculateEndGamePoints(Player[] players) {
            return calculateMostOfTypePoints(players, 
                p -> p.getPlayerBoard().getResidents().size(), 
                10, 4);
        }
    }
    
    // ============================================================================================
    // HELPER METHODS
    // ============================================================================================
    
    /**
     * Helper method to calculate points for "most residents of a specific level" objectives.
     * Awards firstPlacePoints to players with most residents, secondPlacePoints to second-most.
     * Handles ties appropriately.
     */
    private static Map<Player, Integer> calculateMostResidentsPoints(
            Player[] players, 
            int populationLevel, 
            int firstPlacePoints, 
            int secondPlacePoints) {
        
        // Count residents of specified level for each player
        Map<Player, Long> residentCounts = new HashMap<>();
        for (Player player : players) {
            long count = player.getPlayerBoard().getResidents().stream()
                .filter(r -> r.getPopulationLevel() == populationLevel)
                .count();
            residentCounts.put(player, count);
        }
        
        return calculatePointsFromCounts(residentCounts, firstPlacePoints, secondPlacePoints);
    }
    
    /**
     * Generic helper method to calculate "most of type" points.
     * Uses a function to extract the count from each player.
     */
    private static Map<Player, Integer> calculateMostOfTypePoints(
            Player[] players,
            java.util.function.Function<Player, Integer> countExtractor,
            int firstPlacePoints,
            int secondPlacePoints) {
        
        Map<Player, Long> counts = new HashMap<>();
        for (Player player : players) {
            counts.put(player, (long) countExtractor.apply(player));
        }
        
        return calculatePointsFromCounts(counts, firstPlacePoints, secondPlacePoints);
    }
    
    /**
     * Helper method to calculate points from count maps.
     * Awards firstPlacePoints to players with highest count, secondPlacePoints to second-highest.
     * Handles ties appropriately.
     */
    private static Map<Player, Integer> calculatePointsFromCounts(
            Map<Player, Long> counts,
            int firstPlacePoints,
            int secondPlacePoints) {
        
        // Sort players by count (descending)
        List<Map.Entry<Player, Long>> sorted = counts.entrySet().stream()
            .sorted(Map.Entry.<Player, Long>comparingByValue().reversed())
            .toList();
        
        Map<Player, Integer> points = new HashMap<>();
        
        if (sorted.isEmpty()) {
            return points;
        }
        
        // Find highest count
        long maxCount = sorted.get(0).getValue();
        if (maxCount == 0) {
            return points; // No one has any
        }
        
        // Award first place points to all players with max count
        List<Player> firstPlace = sorted.stream()
            .filter(e -> e.getValue() == maxCount)
            .map(Map.Entry::getKey)
            .toList();
        
        for (Player player : firstPlace) {
            points.put(player, firstPlacePoints);
        }
        
        // Find second place count (must be less than max)
        long secondCount = sorted.stream()
            .map(Map.Entry::getValue)
            .filter(c -> c < maxCount && c > 0)
            .findFirst()
            .orElse(0L);
        
        if (secondCount > 0) {
            // Award second place points to all players with second count
            List<Player> secondPlace = sorted.stream()
                .filter(e -> e.getValue() == secondCount)
                .map(Map.Entry::getKey)
                .toList();
            
            for (Player player : secondPlace) {
                points.put(player, secondPlacePoints);
            }
        }
        
        return points;
    }
    
    /**
     * Create an unshuffled deck of all objective cards (for testing).
     */
    static List<ObjectiveCard> createDeck() {
        return new ArrayList<>(List.of(
            // Original cards
            new MostInvestors(),
            new MostEngineers(),
            new NewWorldExplorer(),
            new BasicGoodsProducer(),
            new ExplorerTrader(),
            // New cards
            new LuxuryFactories(),
            new SingleIslandBonus(),
            new MostExpeditionCards(),
            new ExtraAction(),
            new ArtifactBonus(),
            new NewWorldProductFactories(),
            new AnimalBonus(),
            new ArtisanGoodsFactories(),
            new MostTradeChips(),
            new PrestigeFactories(),
            new ResidentCardsPenalty(),
            new EngineerGoodsFactories(),
            new DiscardResidentCard(),
            new InvestorExhaustForGold(),
            new MostResidentsTotal()
        ));
    }
    
    /**
     * Create a shuffled deck of all objective cards.
     */
    static List<ObjectiveCard> createShuffledDeck() {
        List<ObjectiveCard> deck = createDeck();
        Collections.shuffle(deck);
        return deck;
    }

    /**
     * Create a shuffled deck of all objective cards using a seeded Random.
     */
    static List<ObjectiveCard> createShuffledDeck(java.util.Random rng) {
        List<ObjectiveCard> deck = createDeck();
        Collections.shuffle(deck, rng);
        return deck;
    }
}
