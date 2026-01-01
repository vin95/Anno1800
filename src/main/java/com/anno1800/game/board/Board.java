package com.anno1800.game.board;

import static com.anno1800.data.gamedata.ResidentCardData.*;
import static com.anno1800.data.gamedata.ExpeditionCardData.*;
import static com.anno1800.game.factories.ShipyardFactory.*;
import static com.anno1800.game.factories.ShipFactory.*;
import static com.anno1800.game.factories.createResidents.*;
import static com.anno1800.data.gamedata.OldWorldIslandData.createOldWorldIslands;
import static com.anno1800.data.gamedata.NewWorldIslandsData.createNewWorldIslands;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.NewWorldIsland;
import com.anno1800.game.tiles.OldWorldIsland;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.TradeShip;
import com.anno1800.game.cards.ExpeditionCard;
import com.anno1800.data.gamedata.Producers;
import com.anno1800.data.gamedata.ShipType;
import com.anno1800.game.residents.Farmer;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.Worker;
import com.anno1800.game.residents.Artisan;
import com.anno1800.game.residents.Engineer;
import com.anno1800.game.residents.Investor;
import java.util.Deque;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.ArrayDeque;

/**
 * Game board containing all card stacks
 */
public class Board {
    // Factory stacks (35 stacks, excluding StartFactories)

    private final List<Deque<Factory>> factoryStacks;
    
    // 3 Resident card stacks
    private final Deque<ResidentCard> residentStack1;
    private final Deque<ResidentCard> residentStack2;
    private final Deque<ResidentCard> residentStack3;
    
    // Expedition card stack (also called Relict cards)
    private final Deque<ExpeditionCard> expeditionStack;
    
    // 3 Shipyard stacks (Level 1-3) - noch nicht implementiert
    private final Deque<Shipyard> shipyardLevel1;
    private final Deque<Shipyard> shipyardLevel2;
    private final Deque<Shipyard> shipyardLevel3;
    
    // 3 Trade ship stacks (Level 1-3) - noch nicht implementiert
    private final Deque<TradeShip> tradeShipLevel1;
    private final Deque<TradeShip> tradeShipLevel2;
    private final Deque<TradeShip> tradeShipLevel3;
    
    // 3 Explorer ship stacks (Level 1-3) - noch nicht implementiert
    private final Deque<ExplorerShip> explorerShipLevel1;
    private final Deque<ExplorerShip> explorerShipLevel2;
    private final Deque<ExplorerShip> explorerShipLevel3;

    // Old World Islands
    // Old World Islands
    private final Deque<OldWorldIsland> oldWorldIslands;

    // New World Islands
    private final Deque<NewWorldIsland> newWorldIslands;

    // Resident lists
    private final List<Farmer> residents_farmers;
    private final List<Worker> residents_workers;
    private final List<Artisan> residents_artisans;
    private final List<Engineer> residents_engineers;
    private final List<Investor> residents_investors;

    private int farmers = 25;
    private int workers = 40;
    private int artisans = 25;
    private int engineers = 20;
    private int investors = 15;

    private boolean endPhase = false;

    private int gold = 1000;
    private int tradeChips = 1000;
    private int explorerChips = 1000;

    public Board(
        List<Deque<Factory>> factoryStacks,
        Deque<ResidentCard> residentStack1,
        Deque<ResidentCard> residentStack2,
        Deque<ResidentCard> residentStack3,
        Deque<ExpeditionCard> expeditionStack,
        Deque<Shipyard> shipyardLevel1,
        Deque<Shipyard> shipyardLevel2,
        Deque<Shipyard> shipyardLevel3,
        Deque<TradeShip> tradeShipLevel1,
        Deque<TradeShip> tradeShipLevel2,
        Deque<TradeShip> tradeShipLevel3,
        Deque<ExplorerShip> explorerShipLevel1,
        Deque<ExplorerShip> explorerShipLevel2,
        Deque<ExplorerShip> explorerShipLevel3,
        Deque<OldWorldIsland> oldWorldIslands, // Removed duplicate declaration
        Deque<NewWorldIsland> newWorldIslands, // Removed duplicate declaration
        List<Farmer> residents_farmers,
        List<Worker> residents_workers,
        List<Artisan> residents_artisans,
        List<Engineer> residents_engineers,
        List<Investor> residents_investors
    ) {
        // Board requires exactly 35 factory stacks (excluding StartFactories)
        if (factoryStacks.size() != 35) {
            throw new IllegalArgumentException("Board requires exactly 35 factory stacks, but got " + factoryStacks.size());
        }
        
        this.factoryStacks = factoryStacks;
        this.residentStack1 = residentStack1;
        this.residentStack2 = residentStack2;
        this.residentStack3 = residentStack3;
        this.expeditionStack = expeditionStack;
        this.shipyardLevel1 = shipyardLevel1;
        this.shipyardLevel2 = shipyardLevel2;
        this.shipyardLevel3 = shipyardLevel3;
        this.tradeShipLevel1 = tradeShipLevel1;
        this.tradeShipLevel2 = tradeShipLevel2;
        this.tradeShipLevel3 = tradeShipLevel3;
        this.explorerShipLevel1 = explorerShipLevel1;
        this.explorerShipLevel2 = explorerShipLevel2;
        this.explorerShipLevel3 = explorerShipLevel3;
        this.oldWorldIslands = oldWorldIslands;
        this.newWorldIslands = newWorldIslands;
        this.residents_farmers = residents_farmers;
        this.residents_workers = residents_workers;
        this.residents_artisans = residents_artisans;
        this.residents_engineers = residents_engineers;
        this.residents_investors = residents_investors;
    }

    // Getter methods
    public List<Deque<Factory>> getFactoryStacks() {
        return factoryStacks;
    }

    /**
     * Initializes the game board with all card stacks
     */
    @SuppressWarnings("unchecked")
    public static Board initializeBoard(int numPlayers) {
        // 35 Factory stacks (excluding StartFactories)
        List<Deque<Factory>> factoryStacks = createFactoryStacks(numPlayers);

        // Resident card stacks (levels 2, 5, 7) - shuffled for randomness
        Deque<ResidentCard> residentStack1 = shuffleStack(getCardsForLevel(2));
        Deque<ResidentCard> residentStack2 = shuffleStack(getCardsForLevel(5));
        Deque<ResidentCard> residentStack3 = shuffleStack(getCardsForLevel(7));

        // Expedition card stack - shuffled for randomness
        Deque<ExpeditionCard> expeditionStack = shuffleStack(createExpeditionCards());

        // Shipyard stacks
        Deque<Shipyard> shipyardLevel1 = createLevel1Shipyards(numPlayers);
        Deque<Shipyard> shipyardLevel2 = createLevel2Shipyards(numPlayers);
        Deque<Shipyard> shipyardLevel3 = createLevel3Shipyards(numPlayers);

        // Trade ship stacks
        Deque<TradeShip> tradeShipLevel1 = (Deque<TradeShip>) createShips(ShipType.TradeShip, 1, numPlayers);
        Deque<TradeShip> tradeShipLevel2 = (Deque<TradeShip>) createShips(ShipType.TradeShip, 2, numPlayers);
        Deque<TradeShip> tradeShipLevel3 = (Deque<TradeShip>) createShips(ShipType.TradeShip, 3, numPlayers);

        // Explorer ship stacks
        Deque<ExplorerShip> explorerShipLevel1 = (Deque<ExplorerShip>) createShips(ShipType.ExplorerShip, 1, numPlayers);
        Deque<ExplorerShip> explorerShipLevel2 = (Deque<ExplorerShip>) createShips(ShipType.ExplorerShip, 2, numPlayers);
        Deque<ExplorerShip> explorerShipLevel3 = (Deque<ExplorerShip>) createShips(ShipType.ExplorerShip, 3, numPlayers);

        // Old World Islands - shuffled for randomness
        Deque<OldWorldIsland> oldWorldIslands = shuffleStack(createOldWorldIslands());

        // New World Islands - shuffled for randomness
        Deque<NewWorldIsland> newWorldIslands = shuffleStack(createNewWorldIslands());

        // Resident lists
        List<Farmer> residents_farmers = createFarmers();
        List<Worker> residents_workers = createWorkers();
        List<Artisan> residents_artisans = createArtisans();
        List<Engineer> residents_engineers = createEngineers();
        List<Investor> residents_investors = createInvestors();

        return new Board(
            factoryStacks,
            residentStack1,
            residentStack2,
            residentStack3,
            expeditionStack,
            shipyardLevel1,
            shipyardLevel2,
            shipyardLevel3,
            tradeShipLevel1,
            tradeShipLevel2,
            tradeShipLevel3,
            explorerShipLevel1,
            explorerShipLevel2,
            explorerShipLevel3,
            oldWorldIslands,
            newWorldIslands,
            residents_farmers,
            residents_workers,
            residents_artisans,
            residents_engineers,
            residents_investors
        );
    }

    /**
     * Creates all factory stacks from FactoryData using Producers enum.
     * Each factory type gets a stack with numPlayers copies.
     * @param numPlayers The number of players in the game
     * @return List of factory stacks, one for each factory type
     */
    private static List<Deque<Factory>> createFactoryStacks(int numPlayers) {
        List<Deque<Factory>> factoryStacks = new java.util.ArrayList<>();
        int numFactories = (numPlayers <= 2) ? 1 : 2;
        for (com.anno1800.data.gamedata.Producers producer : com.anno1800.data.gamedata.Producers.values()) {
            // Skip StartFactories - they are default factories on PlayerBoard
            if (isStartFactory(producer)) {
                continue;
            }
            // Only add if this producer is a Factory (not Plantation)
            try {
                com.anno1800.game.tiles.Factory template = com.anno1800.data.gamedata.FactoryData.getFactory(producer);
                java.util.Deque<com.anno1800.game.tiles.Factory> stack = new java.util.ArrayDeque<>();
                for (int i = 0; i < numFactories; i++) {
                    com.anno1800.game.tiles.Factory factoryCopy = new com.anno1800.game.tiles.Factory(
                        template.getType(),
                        template.costs(),
                        template.produces(),
                        template.populationLevel(),
                        template.getTradeCosts()
                    );
                    stack.addLast(factoryCopy);
                }
                factoryStacks.add(stack);
            } catch (IllegalArgumentException e) {
                // Not a Factory, skip
            }
        }
        return factoryStacks;
    }

    public Deque<Factory> getFactoryStacks(int index) {
        if (index < 0 || index >= 35) {
            throw new IllegalArgumentException("Factory stack index must be between 0 and 34");
        }
        return factoryStacks.get(index);
    }

    public Deque<ResidentCard> getResidentStack1() {
        return residentStack1;
    }

    public Deque<ResidentCard> getResidentStack2() {
        return residentStack2;
    }

    public Deque<ResidentCard> getResidentStack3() {
        return residentStack3;
    }

    public Deque<ExpeditionCard> getExpeditionStack() {
        return expeditionStack;
    }

    public Deque<Shipyard> getShipyardLevel1() {
        return shipyardLevel1;
    }

    public Deque<Shipyard> getShipyardLevel2() {
        return shipyardLevel2;
    }

    public Deque<Shipyard> getShipyardLevel3() {
        return shipyardLevel3;
    }

    public Deque<TradeShip> getTradeShipLevel1() {
        return tradeShipLevel1;
    }

    public Deque<TradeShip> getTradeShipLevel2() {
        return tradeShipLevel2;
    }

    public Deque<TradeShip> getTradeShipLevel3() {
        return tradeShipLevel3;
    }

    public Deque<ExplorerShip> getExplorerShipLevel1() {
        return explorerShipLevel1;
    }

    public Deque<ExplorerShip> getExplorerShipLevel2() {
        return explorerShipLevel2;
    }

    public Deque<ExplorerShip> getExplorerShipLevel3() {
        return explorerShipLevel3;
    }

    public Deque<OldWorldIsland> getOldWorldIslands() {
        return oldWorldIslands;
    }

    public Deque<NewWorldIsland> getNewWorldIslands() {
        return newWorldIslands;
    }

    public int getFarmers() {
        return farmers;
    }

    public int getWorkers() {
        return workers;
    }

    public int getArtisans() {
        return artisans;
    }

    public int getEngineers() {
        return engineers;
    }

    public int getInvestors() {
        return investors;
    }

    public int getGold() {
        return gold;
    }

    public int getTradeChips() {
        return tradeChips;
    }

    public int getExplorerChips() {
        return explorerChips;
    }

    public boolean isEndPhase() {
        return endPhase;
    }

    /**
     * Takes a resident of the specified population level from the board.
     * 
     * @param populationLevel The population level (1=Farmer, 2=Worker, 3=Artisan, 4=Engineer, 5=Investor)
     * @return The resident of the requested type
     * @throws IllegalArgumentException if populationLevel is invalid
     * @throws IllegalStateException if no residents of that type are available
     */
    public Resident takeResident(int populationLevel) {
        return switch (populationLevel) {
            case 1 -> {
                if (farmers > 0) {
                    farmers--;
                    yield residents_farmers.remove(0);
                }
                throw new IllegalStateException("No farmers left on the board");
            }
            case 2 -> {
                if (workers > 0) {
                    workers--;
                    yield residents_workers.remove(0);
                }
                throw new IllegalStateException("No workers left on the board");
            }
            case 3 -> {
                if (artisans > 0) {
                    artisans--;
                    yield residents_artisans.remove(0);
                }
                throw new IllegalStateException("No artisans left on the board");
            }
            case 4 -> {
                if (engineers > 0) {
                    engineers--;
                    yield residents_engineers.remove(0);
                }
                throw new IllegalStateException("No engineers left on the board");
            }
            case 5 -> {
                if (investors > 0) {
                    investors--;
                    yield residents_investors.remove(0);
                }
                throw new IllegalStateException("No investors left on the board");
            }
            default -> throw new IllegalArgumentException("Invalid population level: " + populationLevel + ". Must be 1-5.");
        };
    }

    /**
     * Returns a resident of the specified population level to the board.
     * 
     * @param populationLevel The population level (1=Farmer, 2=Worker, 3=Artisan, 4=Engineer, 5=Investor)
     * @param resident The resident to return
     * @throws IllegalArgumentException if populationLevel is invalid or doesn't match resident's level
     */
    public void returnResident(int populationLevel, Resident resident) {
        // Validate that the resident's level matches the parameter
        if (resident.getPopulationLevel() != populationLevel) {
            throw new IllegalArgumentException(
                "Resident population level mismatch: expected " + populationLevel + 
                ", but resident has level " + resident.getPopulationLevel()
            );
        }
        
        switch (populationLevel) {
            case 1 -> {
                farmers++;
                residents_farmers.add((Farmer) resident);
            }
            case 2 -> {
                workers++;
                residents_workers.add((Worker) resident);
            }
            case 3 -> {
                artisans++;
                residents_artisans.add((Artisan) resident);
            }
            case 4 -> {
                engineers++;
                residents_engineers.add((Engineer) resident);
            }
            case 5 -> {
                investors++;
                residents_investors.add((Investor) resident);
            }
            default -> throw new IllegalArgumentException("Invalid population level: " + populationLevel + ". Must be 1-5.");
        }
    }

    public ResidentCard drawResidentCard(int populationLevel) {
        Deque<ResidentCard> stack;
        if (populationLevel <= 2) {
            stack = residentStack1;
        } else if (populationLevel <= 5) {
            stack = residentStack2;
        } else {
            stack = residentStack3;
        }

        if (stack.isEmpty()) {
            throw new IllegalStateException("No resident cards left in the stack for population level " + populationLevel);
        }
        return stack.pop();
    }

    public ExpeditionCard drawExpeditionCard() {

        if (expeditionStack.isEmpty()) {
            throw new IllegalStateException("No expedition cards left in the stack");
        }
        return expeditionStack.pop();
    }

    public int takeGold(int amount) {
        if (amount > gold) {
            System.out.println("Not enough gold available");
            gold = 0;
            return amount - gold;
        }
        gold -= amount;
        return amount;
    }

    public void returnGold(int amount) {
        gold += amount;
    }

    /**
     * Checks if a ship of the given type and level can be taken from the board.
     * Validates both ship availability and required chips.
     * @return true if the ship can be taken, false otherwise
     */
    public boolean canTakeShip(ShipType type, int level) {
        // Check if enough chips are available
        boolean hasChips = switch (type) {
            case ExplorerShip -> explorerChips >= level;
            case TradeShip -> tradeChips >= level;
        };
        
        if (!hasChips) {
            return false;
        }
        
        // Check if ship is available in the deque
        Deque<?> shipDeque = getShipDeque(type, level);
        return shipDeque != null && !shipDeque.isEmpty();
    }

    /**
     * Takes a ship from the board and the corresponding chips.
     * @return the ship object (ExplorerShip or TradeShip)
     * @throws IllegalStateException if ship or chips are not available
     */
    public Object takeShip(ShipType type, int level) {
        if (!canTakeShip(type, level)) {
            throw new IllegalStateException("Cannot take ship of type " + type + " level " + level + ": not enough ships or chips available");
        }
        
        // Take the chips
        switch (type) {
            case ExplorerShip -> takeExplorerChip(level);
            case TradeShip -> takeTradeChip(level);
        }
        
        // Take and return the ship
        return switch (type) {
            case ExplorerShip -> switch (level) {
                case 1 -> explorerShipLevel1.poll();
                case 2 -> explorerShipLevel2.poll();
                case 3 -> explorerShipLevel3.poll();
                default -> throw new IllegalArgumentException("Invalid explorer ship level: " + level);
            };
            case TradeShip -> switch (level) {
                case 1 -> tradeShipLevel1.poll();
                case 2 -> tradeShipLevel2.poll();
                case 3 -> tradeShipLevel3.poll();
                default -> throw new IllegalArgumentException("Invalid trade ship level: " + level);
            };
        };
    }

    /**
     * Helper method to get the appropriate ship deque based on type and level.
     */
    private Deque<?> getShipDeque(ShipType type, int level) {
        return switch (type) {
            case ExplorerShip -> switch (level) {
                case 1 -> explorerShipLevel1;
                case 2 -> explorerShipLevel2;
                case 3 -> explorerShipLevel3;
                default -> null;
            };
            case TradeShip -> switch (level) {
                case 1 -> tradeShipLevel1;
                case 2 -> tradeShipLevel2;
                case 3 -> tradeShipLevel3;
                default -> null;
            };
        };
    }

    public int takeTradeChip(int amount) {
        if (amount > tradeChips) {
            System.out.println("Not enough trade ships available");
            return amount - tradeChips;
        }
        tradeChips -= amount;
        return amount;
    }

    public void returnTradeChip(int amount) {
        tradeChips += amount;
    }

    public int takeExplorerChip(int amount) {
        if (amount > explorerChips) {
            System.out.println("Not enough explorer ships available");
            return amount - explorerChips;
        }
        explorerChips -= amount;
        return amount;
    }

    public void returnExplorerChip(int amount) {
        explorerChips += amount;
    }

    /**
     * Checks if a factory of the given type is available on the board.
     * @param type The factory type (Producers enum)
     * @return true if at least one factory of this type is available
     */
    public boolean hasFactory(Producers type) {
        int stackIndex = type.ordinal();
        if (stackIndex < 0 || stackIndex >= factoryStacks.size()) {
            return false;
        }
        Deque<Factory> stack = factoryStacks.get(stackIndex);
        return stack != null && !stack.isEmpty();
    }

    /**
     * Checks if a factory of the given type can be taken from the board.
     * This is the same as hasFactory() for now, but can be extended with additional checks.
     * @param type The factory type (Producers enum)
     * @return true if the factory can be taken
     */
    public boolean canTakeFactory(Producers type) {
        return hasFactory(type);
    }

    /**
     * Takes a factory of the specified type from the board.
     * Removes it from the appropriate stack.
     * @param type The factory type (Producers enum)
     * @return The factory instance
     * @throws IllegalStateException if no factory of this type is available
     */
    public Factory takeFactory(Producers type) {
        if (!hasFactory(type)) {
            throw new IllegalStateException("No factory of type " + type + " available on the board");
        }
        
        int stackIndex = type.ordinal();
        Deque<Factory> stack = factoryStacks.get(stackIndex);
        Factory factory = stack.poll();
        
        if (factory == null) {
            throw new IllegalStateException("Factory stack for " + type + " is unexpectedly empty");
        }
        
        return factory;
    }

    /**
     * Checks if a shipyard of the given level is available on the board.
     * @param level The shipyard level (1-3)
     * @return true if at least one shipyard of this level is available
     */
    public boolean hasShipyard(int level) {
        Deque<Shipyard> stack = getShipyardStack(level);
        return stack != null && !stack.isEmpty();
    }

    /**
     * Checks if a shipyard of the given level can be taken from the board.
     * This is the same as hasShipyard() for now, but can be extended with additional checks.
     * @param level The shipyard level (1-3)
     * @return true if the shipyard can be taken
     */
    public boolean canTakeShipyard(int level) {
        return hasShipyard(level);
    }

    /**
     * Takes a shipyard of the specified level from the board.
     * Removes it from the appropriate stack.
     * @param level The shipyard level (1-3)
     * @return The shipyard instance
     * @throws IllegalStateException if no shipyard of this level is available
     * @throws IllegalArgumentException if level is not 1-3
     */
    public Shipyard takeShipyard(int level) {
        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("Shipyard level must be 1-3, got: " + level);
        }
        
        if (!hasShipyard(level)) {
            throw new IllegalStateException("No shipyard of level " + level + " available on the board");
        }
        
        Deque<Shipyard> stack = getShipyardStack(level);
        Shipyard shipyard = stack.poll();
        
        if (shipyard == null) {
            throw new IllegalStateException("Shipyard stack for level " + level + " is unexpectedly empty");
        }
        
        return shipyard;
    }

    /**
     * Helper method to get the shipyard stack for a given level.
     * @param level The shipyard level (1-3)
     * @return The corresponding shipyard deque
     */
    private Deque<Shipyard> getShipyardStack(int level) {
        return switch (level) {
            case 1 -> shipyardLevel1;
            case 2 -> shipyardLevel2;
            case 3 -> shipyardLevel3;
            default -> null;
        };
    }

    /**
     * Helper method to shuffle a Deque for randomness.
     * Converts the deque to a list, shuffles it, and returns a new ArrayDeque.
     * 
     * @param <T> The type of elements in the deque
     * @param deque The deque to shuffle
     * @return A new shuffled deque with the same elements
     */
    private static <T> Deque<T> shuffleStack(Deque<T> deque) {
        List<T> list = new ArrayList<>(deque);
        Collections.shuffle(list);
        return new ArrayDeque<>(list);
    }

    /**
     * Helper method to determine if a producer is a StartFactory.
     * StartFactories are the GREEN and RED factories that come as default on PlayerBoard.
     * 
     * @param producer The producer to check
     * @return true if it's a StartFactory
     */
    private static boolean isStartFactory(com.anno1800.data.gamedata.Producers producer) {
        return switch (producer) {
            // GREEN StartFactories
            case SAWMILL_GREEN, GRAIN_FARM_GREEN, POTATO_FARM_GREEN, 
                 PIG_FARM_GREEN, SHEEP_FARM_GREEN,
            // RED StartFactories
                 COAL_MINE_RED, BRICK_FACTORY_RED, WAREHOUSE_RED, 
                 STEEL_WORKS_RED, SAILMAKERS_RED -> true;
            default -> false;
        };
    }

}