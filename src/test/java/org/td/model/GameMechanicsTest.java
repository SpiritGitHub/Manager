package org.td.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.td.model.entities.City;
import org.td.model.entities.Residence;
import org.td.model.enums.ResidenceLevel;
import org.td.model.simulation.EconomyManager;
import org.td.model.simulation.EnergySimulator;
import org.td.model.simulation.PopulationManager;

import static org.junit.jupiter.api.Assertions.*;

class GameMechanicsTest {

    private City city;
    private EnergySimulator energySimulator;
    private EconomyManager economyManager;
    private PopulationManager populationManager;

    @BeforeEach
    void setUp() {
        city = new City("TestCity");
        // Clear default buildings to have a clean slate for tests
        city.getResidences().clear();
        city.getPowerPlants().clear();
        city.getInfrastructures().clear();

        // Re-initialize managers with the cleared city
        energySimulator = new EnergySimulator(city);
        economyManager = new EconomyManager(city);
        populationManager = new PopulationManager(city);

        // Advance time once to update internal stats (population -> 0, etc.)
        city.advanceTime();
    }

    @Test
    void testInitialState() {
        assertEquals(0, city.getPopulation());
        assertEquals(75000, city.getMoney(), "Initial money should be 75000");
        assertEquals(1, city.getLevel());
    }

    @Test
    void testResidenceEnergyDemand() {
        Residence residence = new Residence(ResidenceLevel.BASIC, 0, 0);
        city.addBuilding(residence);

        // Update simulation by advancing time
        city.advanceTime();

        assertTrue(city.getTotalEnergyDemand() > 0, "Residence should consume energy");
        assertTrue(residence.getEnergyDemand() > 0);
    }

    @Test
    void testEconomyRevenue() {
        // Add a residence to generate revenue
        Residence residence = new Residence(ResidenceLevel.BASIC, 0, 0);
        city.addBuilding(residence);

        // Add a power plant to produce energy to sell
        org.td.model.entities.CoalPlant plant = new org.td.model.entities.CoalPlant(1, 100, 100);
        city.addBuilding(plant);

        // Simulate one hour
        city.advanceTime();

        // Check if revenue was calculated (totalRevenue is updated in advanceTime)
        assertTrue(city.getTotalRevenue() > 0, "City should generate revenue from electricity sales");
    }

    @Test
    void testPopulationGrowth() {
        // Initial population is 0
        assertEquals(0, city.getPopulation());

        // Add a residence, which comes with population
        Residence residence = new Residence(ResidenceLevel.BASIC, 0, 0);
        city.addBuilding(residence);

        // Population is updated in advanceTime or when adding building?
        // City.addBuilding adds to list, but updatePopulation is called in advanceTime
        // AND initializeStartingCity.
        // Let's call advanceTime to be sure
        city.advanceTime();

        assertTrue(city.getPopulation() > 0, "Adding residence should increase population");
    }

    @Test
    void testEnergyCoverage() {
        Residence residence = new Residence(ResidenceLevel.BASIC, 0, 0);
        city.addBuilding(residence);

        // No power plants yet
        city.advanceTime();

        // Demand should be > 0, Production 0 -> Coverage 0
        assertEquals(0, energySimulator.getCoverageRate(), 0.1, "Coverage should be 0 without power plants");
        assertFalse(residence.hasElectricity());
    }
}
