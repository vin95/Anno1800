package com.anno1800.player;

import com.anno1800.board.Board;

public class Player {
    private final String name;
    private final PlayerBoard playerBoard;
    private final int id;
    private int position;

    public Player(String name, PlayerBoard playerBoard, int id) {
        this.name = name;
        this.playerBoard = playerBoard;
        this.id = id;
        this.position = 0;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public PlayerBoard getPlayerBoard() {
        return playerBoard;
    }

    public static Player[] initializePlayers(int numPlayers, Board board) {
        if (numPlayers <= 0) {
            throw new IllegalArgumentException("Number of players must be positive");
        }
        if (numPlayers > 4) {
            throw new IllegalArgumentException("Maximum number of players is 4");
        }
        Player[] players = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            int id = i;
            players[i] = new Player("Player " + (i + 1), new PlayerBoard(), id);
        }
        return players;
    }
}