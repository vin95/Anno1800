package com.anno1800.cards;

import com.anno1800.Rewards.Reward;
import com.anno1800.data.Goods;

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
