package com.anno1800.player;

public class Player {
    private final String name;
    private final PlayerBoard playerBoard;

    public Player(String name, PlayerBoard playerBoard) {
        this.name = name;
        this.playerBoard = playerBoard;
    }

    public String getName() {
        return name;
    }

    public PlayerBoard getPlayerBoard() {
        return playerBoard;
    }

    public static Player[] initializePlayers(int numPlayers) {
        if (numPlayers <= 0) {
            throw new IllegalArgumentException("Number of players must be positive");
        }
        if (numPlayers > 4) {
            throw new IllegalArgumentException("Maximum number of players is 4");
        }
        Player[] players = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            players[i] = new Player("Player " + (i + 1), new PlayerBoard());
        }
        return players;
    }
}