package com.anno1800.game.cards;

import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.Goods;

public record ResidentCard(
    int populationLevel,
    Goods[] needs,
    Reward reward,
    boolean isPlayed
) {
    // Überladener Konstruktor - isPlayed wird auf false gesetzt
    public ResidentCard(int populationLevel, Goods[] needs, Reward reward) {
        this(populationLevel, needs, reward, false);
    }
}
