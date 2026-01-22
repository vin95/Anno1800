package com.anno1800.game.cards;

import com.anno1800.data.gamedata.Producers;
import com.anno1800.game.player.Player;

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
        
        // Sort players by count (descending)
        List<Map.Entry<Player, Long>> sorted = residentCounts.entrySet().stream()
            .sorted(Map.Entry.<Player, Long>comparingByValue().reversed())
            .toList();
        
        Map<Player, Integer> points = new HashMap<>();
        
        if (sorted.isEmpty()) {
            return points;
        }
        
        // Find highest count
        long maxCount = sorted.get(0).getValue();
        if (maxCount == 0) {
            return points; // No one has any residents of this level
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
     * Create a shuffled deck of all objective cards.
     */
    static List<ObjectiveCard> createShuffledDeck() {
        List<ObjectiveCard> deck = new ArrayList<>(List.of(
            new MostInvestors(),
            new MostEngineers(),
            new NewWorldExplorer(),
            new BasicGoodsProducer(),
            new ExplorerTrader()
            // More cards can be added here in the future
        ));
        Collections.shuffle(deck);
        return deck;
    }
}
