package com.anno1800.player;

import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.data.FactoryData;
import com.anno1800.residents.Resident;
import com.anno1800.Boardtiles.Producers;
import static com.anno1800.Boardtiles.Producers.*;
import static com.anno1800.residents.ResidentStatus.*;
import com.anno1800.board.Board;
import com.anno1800.cards.ResidentCard;
import java.util.ArrayList;

public class PlayerBoard {
    
    public enum ShipType {
        ExplorerShip,
        TradeShip
    }
    
    int landTiles = 10;
    int coastTiles = 5;
    int seaTiles = 5;
    int numShips = 0;
    int numFactories = 0;
    int numFactoriesOnLand = 0;
    int numFactoriesOnCoast = 0;
    int numShipyards = 0;
    int gold = 0;

    int availableTradeChips = 0;
    int availableExplorerChips = 0;
    
    Factory[] factories = new Factory[15]; // listet nur alle Factories eines Spielers auf. Nicht für Logik zu benutzen, da die SHipyards nicht berücksichtigt werden. Besser getFreeLandTiles etc. benutzen.
    
    ArrayList<Shipyard> shipyards = new ArrayList<>();

    ArrayList<TradeShip> tradeShips = new ArrayList<>();

    ArrayList<ExplorerShip> explorerShips = new ArrayList<>();
    
    ArrayList<Resident> residents = new ArrayList<>();

    ArrayList<ResidentCard> residentCards = new ArrayList<>();
    
    public PlayerBoard() {}

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    public ArrayList<ResidentCard> getResidentCards() {
        return residentCards;
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

    public Factory[] getFactories() {
        return factories;
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
    
    public int getGold() {
        return gold;
    }
    
    public int getAvailableTradeChips() {
        return availableTradeChips;
    }
    
    public int getAvailableExplorerChips() {
        return availableExplorerChips;
    }
    
    public void initializePlayerBoard(Player player, Board gameBoard) {
        PlayerBoard board = player.getPlayerBoard();
        
        board.adGold(player.getPosition(), gameBoard);

        // Take farmer from board and add to player board
        
        for (int i = 0; i < 4; i++) {
            adResident(gameBoard, board, 1);
        }
        
        for (int i = 0; i < 3; i++) {
            adResident(gameBoard, board, 2);
        }

        for (int i = 0; i < 2; i++) {
            adResident(gameBoard, board, 3);
        }

        board.adShip(1, ShipType.TradeShip, gameBoard);
        board.adShip(1, ShipType.TradeShip, gameBoard);
        board.adShip(1, ShipType.ExplorerShip, gameBoard);

        board.adFactory(SAWMILL_GREEN);
        board.adFactory(GRAIN_FARM_GREEN);
        board.adFactory(POTATO_FARM_GREEN);
        board.adFactory(PIG_FARM_GREEN);
        board.adFactory(SHEEP_FARM_GREEN);
        board.adFactory(COAL_MINE_RED);
        board.adFactory(BRICK_FACTORY_RED);
        board.adFactory(WAREHOUSE_RED);
        board.adFactory(STEEL_WORKS_RED);
        board.adFactory(SAILMAKERS_RED);

        board.adShipyard(1);
    }

    // Die nachfolgenden Methoden fügen dem PlayerBoard entsprechende Objekte hinzu und werden nur bei der Initialisierung verwendet.
    
    private void adGold(int gold, Board gameBoard) {
        this.gold = this.gold + gameBoard.takeGold(gold);
    }

    /**
     * Adds a ship to the player board if available on the game board.
     * Also adds the corresponding chips to the player's available chips.
     * Uses Rules.canBuildShip() for validation.
     * @throws IllegalStateException if ship cannot be built (rules violation)
     */
    private void adShip(int level, ShipType type, Board gameBoard) {
        
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

    private void adFactory(Producers type) {
        Factory newFactory = copyFactory(type);
        factories[numFactories] = newFactory;
        numFactoriesOnLand++;
        numFactories++;
    }

    private void adShipyard(int level) {
        Shipyard newShipyard = new Shipyard(level);
        shipyards.add(newShipyard);
        numShipyards++;
    }

    private void adResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        // Use the new unified takeResident method
        Resident resident = gameBoard.takeResident(populationLevel);
        resident.setStatus(FIT);
        this.residents.add(resident);
        ResidentCard residentCard = gameBoard.drawResidentCard(resident.getPopulationLevel());
        this.residentCards.add(residentCard);
    }
    
    /**
     * Creates a new Factory instance based on the template from FactoryData.
     * Each player gets their own Factory instances.
     */
    private static Factory copyFactory(Producers type) {
        Factory template = (Factory) FactoryData.getProducer(type);
        return new Factory(
            template.getType(),
            template.costs(),
            template.produces(),
            template.populationLevel()
        );
    }
}