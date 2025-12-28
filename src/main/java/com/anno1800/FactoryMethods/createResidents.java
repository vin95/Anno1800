package com.anno1800.FactoryMethods;

import java.util.ArrayList;
import java.util.List;

import com.anno1800.residents.*;

public class createResidents {
    static int farmers = 25;
    static int workers = 40;
    static int artisans = 25;
    static int engineers = 20;
    static int investors = 15;

    public static List<Farmer> createFarmers() {
        ArrayList<Farmer> residents = new ArrayList<>();
        for (int i = 0; i < farmers; i++) {
            residents.add(new Farmer());
        } 
        return residents;
    }

    public static List<Worker> createWorkers() {
        ArrayList<Worker> residents = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            residents.add(new Worker());
        } 
        return residents;
    }

    public static List<Artisan> createArtisans() {
        ArrayList<Artisan> residents = new ArrayList<>();
        for (int i = 0; i < artisans; i++) {
            residents.add(new Artisan());
        } 
        return residents;
    }

    public static List<Engineer> createEngineers() {
        ArrayList<Engineer> residents = new ArrayList<>();
        for (int i = 0; i < engineers; i++) {
            residents.add(new Engineer());
        } 
        return residents;
    }

    public static List<Investor> createInvestors() {
        ArrayList<Investor> residents = new ArrayList<>();
        for (int i = 0; i < investors; i++) {
            residents.add(new Investor());
        } 
        return residents;
    }
}
