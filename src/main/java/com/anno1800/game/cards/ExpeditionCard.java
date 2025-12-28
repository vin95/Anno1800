package com.anno1800.game.cards;

import com.anno1800.game.residents.Resident;

/**
 * Expedition card (also known as Relict card) with two slots for residents.
 * Grants points when residents with matching population level are placed.
 */
public class ExpeditionCard {
    private final int requiredPopulationLevel1;
    private final int requiredPopulationLevel2;
    private final int pointsForSlot1;
    private final int pointsForSlot2;
    private Resident slot1;  // nicht final - kann geändert werden
    private Resident slot2;  // nicht final - kann geändert werden

    public ExpeditionCard(int requiredPopulationLevel1, int requiredPopulationLevel2, 
                     int pointsForSlot1, int pointsForSlot2) {
        this.requiredPopulationLevel1 = requiredPopulationLevel1;
        this.requiredPopulationLevel2 = requiredPopulationLevel2;
        this.pointsForSlot1 = pointsForSlot1;
        this.pointsForSlot2 = pointsForSlot2;
        this.slot1 = null;
        this.slot2 = null;
    }

    /**
     * Places a resident in the specified slot
     */
    public void placeResident(Resident resident, int slotNumber) {
        if (slotNumber == 1 && slot1 == null) {
            slot1 = resident;
        } else if (slotNumber == 2 && slot2 == null) {
            slot2 = resident;
        }
    }

    /**
     * Calculates total points based on matching population levels
     */
    public int calculatePoints() {
        int points = 0;
        if (slot1 != null && slot1.getPopulationLevel() == requiredPopulationLevel1) {
            points += pointsForSlot1;
        }
        if (slot2 != null && slot2.getPopulationLevel() == requiredPopulationLevel2) {
            points += pointsForSlot2;
        }
        return points;
    }

    /**
     * Checks if both slots are filled
     */
    public boolean isFull() {
        return slot1 != null && slot2 != null;
    }

    // Getter
    public int requiredPopulationLevel1() { return requiredPopulationLevel1; }
    public int requiredPopulationLevel2() { return requiredPopulationLevel2; }
    public int pointsForSlot1() { return pointsForSlot1; }
    public int pointsForSlot2() { return pointsForSlot2; }
    public Resident slot1() { return slot1; }
    public Resident slot2() { return slot2; }
}
