package com.anno1800.game.player;

import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.TradeShip;
import com.anno1800.data.gamedata.FactoryData;
import com.anno1800.data.gamedata.Producers;
import com.anno1800.game.residents.Resident;

import static com.anno1800.data.gamedata.Producers.*;
import static com.anno1800.game.residents.ResidentStatus.*;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import java.util.ArrayList;
import com.anno1800.data.gamedata.ShipType;

public class PlayerBoard {

    int landTiles = 10;
    int coastTiles = 5;
    int seaTiles = 5;
    int numShips = 0;
    int numFactories = 0;
    int numFactoriesOnLand = 0;
    int numFactoriesOnCoast = 0;
    int numShipyards = 0;
    int numOldWorldIslands = 0;
    int numNewWorldIslands = 0;
    int gold = 0;

    int availableTradeChips = 0;
    int availableExplorerChips = 0;

    Plantation[] plantations = new Plantation[6];

    Factory[] factories = new Factory[15]; // listet nur alle Factories eines Spielers auf. Nicht für Logik zu benutzen,
                                           // da die Shipyards nicht berücksichtigt werden. Besser getFreeLandTiles etc.
                                           // benutzen.

    ArrayList<Shipyard> shipyards = new ArrayList<>();

    ArrayList<TradeShip> tradeShips = new ArrayList<>();

    ArrayList<ExplorerShip> explorerShips = new ArrayList<>();

    ArrayList<Resident> residents = new ArrayList<>();

    ArrayList<ResidentCard> residentCards = new ArrayList<>();

    public PlayerBoard() {
    }

    public int getLandTiles() {
        return landTiles;
    }

    public int getCoastTiles() {
        return coastTiles;
    }

    public int getSeaTiles() {
        return seaTiles;
    }

    public int getNumShips() {
        return numShips;
    }

    public int getNumFactories() {
        return numFactories;
    }

    public int getNumFactoriesOnLand() {
        return numFactoriesOnLand;
    }

    public int getNumFactoriesOnCoast() {
        return numFactoriesOnCoast;
    }

    public int getNumShipyards() {
        return numShipyards;
    }

    public int getNumOldWorldIslands() {
        return numOldWorldIslands;
    }

    public int getNumNewWorldIslands() {
        return numNewWorldIslands;
    }

    public int getGold() {
        return gold;
    }

    public int getAvailableTradeChips() {
        return availableTradeChips;
    }

    public int getAvailableExplorerChips() {
        return availableExplorerChips;
    }

    public Factory[] getFactories() {
        return factories;
    }

    public Plantation[] getPlantations() {
        return plantations;
    }

    public ArrayList<Shipyard> getShipyards() {
        return shipyards;
    }

    public ArrayList<TradeShip> getTradeShips() {
        return tradeShips;
    }

    public ArrayList<ExplorerShip> getExplorerShips() {
        return explorerShips;
    }

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    public ArrayList<ResidentCard> getResidentCards() {
        return residentCards;
    }

    /**
     * Entfernt eine ResidentCard vom Spieler und legt sie auf den passenden Stack des Boards zurück.
     */
    public void discardResidentCard(ResidentCard card, Board board) {
        residentCards.remove(card);
        int level = card.populationLevel();
        if (level <= 2) {
            board.getResidentStack1().push(card);
        } else if (level <= 5) {
            board.getResidentStack2().push(card);
        } else {
            board.getResidentStack3().push(card);
        }
    }   

    public int getFreeLandTiles() {
        return landTiles - numFactoriesOnLand;
    }

    public int getFreeCoastTiles() {
        return coastTiles - shipyards.size() - numFactoriesOnCoast;
    }

    public int getFreeSeaTiles() {
        return seaTiles - explorerShips.size() - tradeShips.size();
    }

    public void initializePlayerBoard(Player player, Board gameBoard) {
        PlayerBoard board = player.getPlayerBoard();

        board.addGold(player.getPosition(), gameBoard);

        // Take farmer from board and add to player board
        for (int i = 0; i < 4; i++) {
            addResident(gameBoard, board, 1);
        }
        for (int i = 0; i < 3; i++) {
            addResident(gameBoard, board, 2);
        }
        for (int i = 0; i < 2; i++) {
            addResident(gameBoard, board, 3);
        }

        board.addShip(1, ShipType.TradeShip, gameBoard);
        board.addShip(1, ShipType.TradeShip, gameBoard);
        board.addShip(1, ShipType.ExplorerShip, gameBoard);

        // Factories initialisieren
        board.addFactory(FactoryData.getFactory(SAWMILL_GREEN));
        board.addFactory(FactoryData.getFactory(GRAIN_FARM_GREEN));
        board.addFactory(FactoryData.getFactory(POTATO_FARM_GREEN));
        board.addFactory(FactoryData.getFactory(PIG_FARM_GREEN));
        board.addFactory(FactoryData.getFactory(SHEEP_FARM_GREEN));
        board.addFactory(FactoryData.getFactory(COAL_MINE_RED));
        board.addFactory(FactoryData.getFactory(BRICK_FACTORY_RED));
        board.addFactory(FactoryData.getFactory(WAREHOUSE_RED));
        board.addFactory(FactoryData.getFactory(STEEL_WORKS_RED));
        board.addFactory(FactoryData.getFactory(SAILMAKERS_RED));

        // Plantations initialisieren (Beispiel, ggf. anpassen)
        board.addPlantation(FactoryData.getPlantation(CACAO_PLANTATION));
        board.addPlantation(FactoryData.getPlantation(SUGAR_PLANTATION));
        board.addPlantation(FactoryData.getPlantation(TOBACCO_PLANTATION));
        board.addPlantation(FactoryData.getPlantation(COFFEE_PLANTATION));
        board.addPlantation(FactoryData.getPlantation(COTTON_PLANTATION));
        board.addPlantation(FactoryData.getPlantation(RUBBER_PLANTATION));

        board.addShipyard(1);
    }

    // ========== Initialization Methods (used only during setup) ==========

    private void addGold(int gold, Board gameBoard) {
        this.gold = this.gold + gameBoard.takeGold(gold);
    }


    private void addFactory(Factory factory) {
        factories[numFactories] = factory;
        numFactoriesOnLand++;
        numFactories++;
    }

    private int numPlantations = 0;
    private void addPlantation(Plantation plantation) {
        plantations[numPlantations] = plantation;
        numPlantations++;
    }

    private void addShipyard(int level) {
        Shipyard newShipyard = new Shipyard(level);
        shipyards.add(newShipyard);
        numShipyards++;
    }

    /**
     * Adds a ship to the player board during initialization.
     * Also adds the corresponding chips to the player's available chips.
     * 
     * @param level The ship level (1-3)
     * @param type The type of ship (ExplorerShip or TradeShip)
     * @param gameBoard The game board to take chips from
     */
    private void addShip(int level, ShipType type, Board gameBoard) {
        switch (type) {
            case ExplorerShip -> {
                ExplorerShip ship = new ExplorerShip(level);
                explorerShips.add(ship);
                availableExplorerChips += gameBoard.takeExplorerChip(level);
            }
            case TradeShip -> {
                TradeShip ship = new TradeShip(level);
                tradeShips.add(ship);
                availableTradeChips += gameBoard.takeTradeChip(level);
            }
        }
    }

    private void addResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        // Use the new unified takeResident method
        Resident resident = gameBoard.takeResident(populationLevel);
        resident.setStatus(FIT);
        this.residents.add(resident);
        ResidentCard residentCard = gameBoard.drawResidentCard(resident.getPopulationLevel());
        this.residentCards.add(residentCard);
    }

    // ========== Build Methods (used by ActionHandler during gameplay) ==========

    /**
     * Builds a factory on the player board.
     * Used during gameplay when a player builds a factory.
     * Prefers land tiles over coast tiles.
     * 
     * @param factory The factory instance to build
     * @throws IllegalStateException if no free land or coast tiles available
     */
    public void buildFactory(Factory factory) {
        if (getFreeLandTiles() <= 0 && getFreeCoastTiles() <= 0) {
            throw new IllegalStateException("No free tiles available to place factory");
        }

        factories[numFactories] = factory;

        // Prefer land tiles over coast tiles
        if (getFreeLandTiles() > 0) {
            numFactoriesOnLand++;
        } else {
            numFactoriesOnCoast++;
        }

        numFactories++;
    }

    /**
     * Builds a shipyard on the player board.
     * Used during gameplay when a player builds a shipyard.
     * Shipyards can only be built on coast tiles.
     * 
     * @param shipyard The shipyard instance to build
     * @throws IllegalStateException if no free coast tiles available
     */
    public void buildShipyard(Shipyard shipyard) {
        if (getFreeCoastTiles() <= 0) {
            throw new IllegalStateException("No free coast tiles available to place shipyard");
        }

        shipyards.add(shipyard);
        numShipyards++;
    }

    /**
     * Builds a ship on the player board.
     * Used during gameplay when a player builds a ship.
     * Also adds the corresponding chips to the player's available chips.
     * 
     * @param ship     The ship instance to build (ExplorerShip or TradeShip)
     * @param shipType The type of ship
     * @param level    The level of the ship (determines chip amount)
     * @throws IllegalStateException if no free sea tiles available
     */
    public void buildShip(Object ship, ShipType shipType, int level) {
        if (getFreeSeaTiles() <= 0) {
            throw new IllegalStateException("No free sea tiles available to place ship");
        }

        switch (shipType) {
            case ExplorerShip -> {
                if (ship instanceof com.anno1800.game.tiles.ExplorerShip explorerShip) {
                    explorerShips.add(explorerShip);
                    availableExplorerChips += level;
                } else {
                    throw new IllegalArgumentException(
                            "Expected ExplorerShip but got " + ship.getClass().getSimpleName());
                }
            }
            case TradeShip -> {
                if (ship instanceof com.anno1800.game.tiles.TradeShip tradeShip) {
                    tradeShips.add(tradeShip);
                    availableTradeChips += level;
                } else {
                    throw new IllegalArgumentException("Expected TradeShip but got " + ship.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Creates a new Factory instance based on the template from FactoryData.
     * Each player gets their own Factory instances.
     * @param type The Producers enum value for the factory
     * @return a new Factory instance
     */
    private static Factory copyFactory(Producers type) {
        Factory template = FactoryData.getFactory(type);
        return new Factory(
                template.getType(),
                template.costs(),
                template.produces(),
                template.populationLevel(),
                template.getTradeCosts() // Always read from FactoryData
        );
    }

    public void earnGold(int amount) {
        this.gold += amount;
    }

    public void spendGold(int amount) {
        if (amount > gold) {
            throw new IllegalArgumentException("Not enough gold to spend");
        }
        this.gold -= amount;
    }

    public void reduceAvailableTradeChips(int amount) {
        if (amount > availableTradeChips) {
            throw new IllegalArgumentException("Cannot reduce trade chips below zero");
        }
        availableTradeChips -= amount;
    }

    public void reduceAvailableExplorerChips(int amount) {
        if (amount > availableExplorerChips) {
            throw new IllegalArgumentException("Cannot reduce explorer chips below zero");
        }
        availableExplorerChips -= amount;
    }

    public void increaseAvailableTradeChips(int amount) {
        if( amount < 0) {
            throw new IllegalArgumentException("Cannot increase trade chips by negative amount");
        }
        availableTradeChips += amount;
    }

    public void increaseAvailableExplorerChips(int amount) {
        if( amount < 0) {
            throw new IllegalArgumentException("Cannot increase explorer chips by negative amount");
        }
        availableExplorerChips += amount;
    }

    public void resetAvailableTradeChips() {
        this.availableTradeChips = 0;
    }

    public void resetAvailableExplorerChips() {
        this.availableExplorerChips = 0;
    }

    public int getPlayersTradeChips() {
        int ownedTradeChips = 0;
        for (TradeShip ship : this.tradeShips) {
            ownedTradeChips += ship.getLevel();
        }
        return ownedTradeChips;
    }

    public int getPlayersExplorerChips() {
        int ownedExplorerChips = 0;
        for (ExplorerShip ship : this.explorerShips) {
            ownedExplorerChips += ship.getLevel();
        }
        return ownedExplorerChips;
    }

    private void buildResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        addResident(gameBoard, playerBoard, populationLevel);
    }

    public void buildFactoryAsReward(Factory factory) {
        addFactory(factory);
    }
}