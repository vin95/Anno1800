package com.anno1800.game.cards;

import com.anno1800.game.residents.Resident;

/**
 * Expedition card (also known as Relict card) with two slots for residents.
 * 
 * Rule: "Expeditions-Karten zeigen jeweils links ein Tier für den Zoo und 
 *        rechts ein Artefakt für das Museum."
 * 
 * - Slot 1 (left): Animal for the Zoo (Tier für den Zoo)
 * - Slot 2 (right): Artifact for the Museum (Artefakt für das Museum)
 * 
 * Each slot requires a specific population level (Handwerker=3, Ingenieur=4, Investor=5)
 * and grants points when a matching resident is placed.
 * 
 * Rule: "Jeder Bevölkerungs-Stein kann nur für je ein Feld genutzt werden."
 * Each resident can only be used for one slot across all expedition cards.
 */
public class ExpeditionCard {
    // Zoo slot (left side - Animal/Tier)
    private final int requiredPopulationLevelForAnimal;
    private final int pointsForAnimal;
    private Resident animalSlot;  // Tier für den Zoo
    
    // Museum slot (right side - Artifact/Artefakt)  
    private final int requiredPopulationLevelForArtifact;
    private final int pointsForArtifact;
    private Resident artifactSlot;  // Artefakt für das Museum

    /**
     * Creates an expedition card with two slots.
     * 
     * @param requiredPopulationLevelForAnimal Population level required for Zoo/Animal slot (left)
     * @param requiredPopulationLevelForArtifact Population level required for Museum/Artifact slot (right)
     * @param pointsForAnimal Victory points for filling the Zoo/Animal slot
     * @param pointsForArtifact Victory points for filling the Museum/Artifact slot
     */
    public ExpeditionCard(int requiredPopulationLevelForAnimal, int requiredPopulationLevelForArtifact, 
                     int pointsForAnimal, int pointsForArtifact) {
        this.requiredPopulationLevelForAnimal = requiredPopulationLevelForAnimal;
        this.requiredPopulationLevelForArtifact = requiredPopulationLevelForArtifact;
        this.pointsForAnimal = pointsForAnimal;
        this.pointsForArtifact = pointsForArtifact;
        this.animalSlot = null;
        this.artifactSlot = null;
    }

    /**
     * Places a resident in the specified slot.
     * 
     * @param resident The resident to place
     * @param slotNumber 1 for Zoo/Animal slot, 2 for Museum/Artifact slot
     */
    public void placeResident(Resident resident, int slotNumber) {
        if (slotNumber == 1 && animalSlot == null) {
            animalSlot = resident;
        } else if (slotNumber == 2 && artifactSlot == null) {
            artifactSlot = resident;
        }
    }

    /**
     * Places a resident as a visitor for the Zoo (Animal slot).
     * 
     * @param resident The resident visiting the Zoo
     * @return true if placement was successful
     */
    public boolean placeAnimalVisitor(Resident resident) {
        if (animalSlot == null && resident.getPopulationLevel() == requiredPopulationLevelForAnimal) {
            animalSlot = resident;
            return true;
        }
        return false;
    }

    /**
     * Places a resident as a visitor for the Museum (Artifact slot).
     * 
     * @param resident The resident visiting the Museum
     * @return true if placement was successful
     */
    public boolean placeArtifactVisitor(Resident resident) {
        if (artifactSlot == null && resident.getPopulationLevel() == requiredPopulationLevelForArtifact) {
            artifactSlot = resident;
            return true;
        }
        return false;
    }

    /**
     * Calculates total points based on matching population levels.
     * Points are awarded for each slot that has a resident with the required level.
     */
    public int calculatePoints() {
        int points = 0;
        if (animalSlot != null && animalSlot.getPopulationLevel() == requiredPopulationLevelForAnimal) {
            points += pointsForAnimal;
        }
        if (artifactSlot != null && artifactSlot.getPopulationLevel() == requiredPopulationLevelForArtifact) {
            points += pointsForArtifact;
        }
        return points;
    }

    /**
     * Calculates points for Zoo animals only.
     * Useful when Zoo objective card is active (+1 bonus per animal).
     */
    public int calculateZooPoints() {
        if (animalSlot != null && animalSlot.getPopulationLevel() == requiredPopulationLevelForAnimal) {
            return pointsForAnimal;
        }
        return 0;
    }

    /**
     * Calculates points for Museum artifacts only.
     * Useful when Museum objective card is active (+1 bonus per artifact).
     */
    public int calculateMuseumPoints() {
        if (artifactSlot != null && artifactSlot.getPopulationLevel() == requiredPopulationLevelForArtifact) {
            return pointsForArtifact;
        }
        return 0;
    }

    /**
     * Checks if both slots are filled.
     */
    public boolean isFull() {
        return animalSlot != null && artifactSlot != null;
    }

    /**
     * Checks if the Zoo/Animal slot is filled.
     */
    public boolean hasAnimal() {
        return animalSlot != null;
    }

    /**
     * Checks if the Museum/Artifact slot is filled.
     */
    public boolean hasArtifact() {
        return artifactSlot != null;
    }

    // ========== Getters with semantic names ==========
    
    /** Required population level for Zoo/Animal slot (left) */
    public int getRequiredLevelForAnimal() { return requiredPopulationLevelForAnimal; }
    
    /** Required population level for Museum/Artifact slot (right) */
    public int getRequiredLevelForArtifact() { return requiredPopulationLevelForArtifact; }
    
    /** Victory points for filling Zoo/Animal slot */
    public int getPointsForAnimal() { return pointsForAnimal; }
    
    /** Victory points for filling Museum/Artifact slot */
    public int getPointsForArtifact() { return pointsForArtifact; }
    
    /** Resident placed in Zoo/Animal slot */
    public Resident getAnimalVisitor() { return animalSlot; }
    
    /** Resident placed in Museum/Artifact slot */
    public Resident getArtifactVisitor() { return artifactSlot; }

    // ========== Legacy getters for backwards compatibility ==========
    
    /** @deprecated Use getRequiredLevelForAnimal() instead */
    @Deprecated
    public int requiredPopulationLevel1() { return requiredPopulationLevelForAnimal; }
    
    /** @deprecated Use getRequiredLevelForArtifact() instead */
    @Deprecated
    public int requiredPopulationLevel2() { return requiredPopulationLevelForArtifact; }
    
    /** @deprecated Use getPointsForAnimal() instead */
    @Deprecated
    public int pointsForSlot1() { return pointsForAnimal; }
    
    /** @deprecated Use getPointsForArtifact() instead */
    @Deprecated
    public int pointsForSlot2() { return pointsForArtifact; }
    
    /** @deprecated Use getAnimalVisitor() instead */
    @Deprecated
    public Resident slot1() { return animalSlot; }
    
    /** @deprecated Use getArtifactVisitor() instead */
    @Deprecated
    public Resident slot2() { return artifactSlot; }
}
