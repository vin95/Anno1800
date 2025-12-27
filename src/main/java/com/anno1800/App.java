package com.anno1800;

import com.anno1800.engine.Game;

public class App {
    public static void main(String[] args) {
        int numPlayers = 2;
        Game game = new Game(numPlayers);
        game.start();
    }
}
