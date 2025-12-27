package com.anno1800.residents;

public class Resident {
    private final int populationLevel;
    private ResidentStatus status;

    public Resident(int populationLevel, ResidentStatus status) {
        this.populationLevel = populationLevel;
        this.status = status;
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
}