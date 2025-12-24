package com.anno1800.residents;

public class Resident {
    private final int populationLevel;
    private Enum<ResidentStatus> status;

    public Resident(int populationLevel, Enum<ResidentStatus> status) {
        this.populationLevel = populationLevel;
        this.status = status;
    }

    public int getPopulationLevel() {
        return populationLevel;
    }

    public Enum<ResidentStatus> getStatus() {
        return status;
    }

    public void setStatus(Enum<ResidentStatus> status) {
        this.status = status;
    }
}