package com.anno1800.game.residents;

import static com.anno1800.game.residents.ResidentStatus.*;

public abstract class Resident {
    private final int populationLevel;
    private ResidentStatus status;

    protected Resident(int populationLevel) {
        this.populationLevel = populationLevel;
        this.status = ON_BOARD;
    }

    public int getPopulationLevel() {
        return populationLevel;
    }

    public ResidentStatus getStatus() {
        return status;
    }

    public void setStatus(ResidentStatus status) {
        this.status = status;
    }
    
    /**
     * Get the recover costs for this resident.
     * Recover costs are identical to settlement costs for the resident's population level.
     * 
     * @return The cost to recover this resident from exhausted state
     */
    public ResidentCosts.Cost getRecoverCost() {
        return ResidentCosts.getSettlementCost(populationLevel);
    }
    
    /**
     * Checks if this resident is exhausted.
     * 
     * @return true if the resident is in EXHAUSTED status
     */
    public boolean isExhausted() {
        return status == EXHAUSTED;
    }
    
    /**
     * Checks if this resident is fit (can be used for actions).
     * 
     * @return true if the resident is in FIT status
     */
    public boolean isFit() {
        return status == FIT;
    }
    
    /**
     * Exhausts this resident (sets status to EXHAUSTED).
     * Used when a resident performs work or is used for ObjectiveCard actions.
     */
    public void exhaust() {
        this.status = EXHAUSTED;
    }
    
    /**
     * Recovers this resident from exhausted state (sets status to FIT).
     */
    public void recover() {
        this.status = FIT;
    }
}