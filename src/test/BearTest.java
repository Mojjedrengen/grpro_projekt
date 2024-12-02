package test;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Bear;
import simulator.actors.Rabbit;
import simulator.actors.Wolf;
import simulator.objects.Carcass;
import simulator.objects.plants.Bush;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the Bear class
public class BearTest {

    World w;

    @BeforeEach
    public void setUp() {
        this.w = new World(5); // Small world for testing
    }

    /**
     * Test that the bear moves during the day
     */
    @Test
    public void wanderTest() {
        w.setDay();

        Bear bear = new Bear();

        Location startingLocation = new Location(0, 0);
        w.setTile(startingLocation, bear);

        bear.act(w);
        Location afterLocation = w.getLocation(bear);

        assertNotEquals(startingLocation, afterLocation, "Bear should have moved to a new location");
    }

    /**
     * Test that the bear eats berries when adjacent to a bush with ripe berries
     */
    @Test
    public void eatBerriesTest() {
        w.setDay();

        Bear bear = new Bear();
        Bush bush = new Bush();

        Location bearLocation = new Location(0, 0);
        Location bushLocation = new Location(0, 1);

        // Set up the bear and a ripe bush
        w.setTile(bearLocation, bear);
        w.setTile(bushLocation, bush);
        bush.setToRipe();

        assertTrue(w.containsNonBlocking(bushLocation), "Bush should be present at the location");

        // Bear should eat the berries
        bear.decreaseEnergy(10, w);
        int currentEnergy = bear.getEnergy();
        bear.act(w);

        assertTrue(bear.getEnergy() > currentEnergy, "Bear's energy should increase after eating berries");
    }

    /**
     * Test that the bear eats a carcass if it is nearby
     */
    @Test
    public void eatCarcassTest() {
        w.setDay();

        Bear bear = new Bear();
        Carcass carcass = new Carcass(Carcass.smallCarcass, false);

        Location bearLocation = new Location(0, 0);
        Location carcassLocation = new Location(0, 1);

        w.setTile(bearLocation, bear);
        w.setTile(carcassLocation, carcass);

        assertTrue(w.containsNonBlocking(carcassLocation), "Carcass should be present at the location");

        // Bear should eat the carcass
        bear.act(w);

        assertTrue(bear.getEnergy() == 50, "Bear's energy should increase after eating a carcass");
    }

    /**
     * Test that the bear attacks a rabbit if it is adjacent
     */
    @Test
    public void attackRabbitTest() {
        w.setDay();

        Bear bear = new Bear();
        Rabbit rabbit = new Rabbit();

        Location bearLocation = new Location(0, 0);
        Location rabbitLocation = new Location(0, 1);

        w.setTile(bearLocation, bear);
        w.setTile(rabbitLocation, rabbit);

        assertFalse(w.containsNonBlocking(rabbitLocation), "No carcass should be present initially");

        // Bear should attack the rabbit
        bear.act(w);

        assertTrue(w.containsNonBlocking(rabbitLocation), "Rabbit should have been replaced with a carcass");
        assertTrue(w.getNonBlocking(rabbitLocation) instanceof Carcass, "A carcass should replace the rabbit");
    }

    /**
     * Test that the bear establishes a territory when it acts
     */
    @Test
    public void establishTerritoryTest() {
        w.setDay();

        Bear bear = new Bear();
        Location bearLocation = new Location(2, 2);

        w.setTile(bearLocation, bear);

        // Bear should establish a territory
        bear.act(w);

        assertNotNull(bear.getPathFinder(), "Bear should establish a path finder");
    }

    /**
     * Test that the bear returns to its territory at night
     */
    @Test
    public void returnToTerritoryAtNightTest() {
        w.setNight();

        Bear bear = new Bear();
        Location startingLocation = new Location(0, 0);
        Location territoryLocation = new Location(2, 2);

        w.setTile(startingLocation, bear);

        // Establish territory
        bear.act(w);

        // Move the bear away from the territory
        w.move(bear, territoryLocation);
        // Act to move back to the territory
        bear.act(w);

        assertTrue(bear.isInTerritory(territoryLocation), "Bear should return to its territory at night");
    }

    /**
     * Test that the bear prefers berries over hunting
     */
    @Test
    public void preferBerriesOverHuntingTest() {
        w.setDay();

        Bear bear = new Bear();
        Rabbit rabbit = new Rabbit();
        Bush bush = new Bush();

        Location bearLocation = new Location(0, 0);
        Location rabbitLocation = new Location(0, 1);
        Location bushLocation = new Location(1, 0);

        w.setTile(bearLocation, bear);
        w.setTile(rabbitLocation, rabbit);
        w.setTile(bushLocation, bush);
        bush.setToRipe();

        // Bear should eat berries and not attack the rabbit
        bear.decreaseEnergy(10, w);
        int currentEnergy = bear.getEnergy();
        bear.act(w);
        assertTrue(bear.getEnergy() > currentEnergy, "Bear's energy should increase after eating a Berry");
        assertTrue(w.getTile(rabbitLocation) instanceof Rabbit, "Rabbit should not have been attacked");
    }

    /**
     * Test that the bear attacks only when hungry
     */
    @Test
    public void attackOnlyWhenHungryTest() {
        w.setDay();

        Bear bear = new Bear();
        Rabbit rabbit = new Rabbit();

        Location bearLocation = new Location(0, 0);
        Location rabbitLocation = new Location(0, 1);

        w.setTile(bearLocation, bear);
        w.setTile(rabbitLocation, rabbit);

        // Bear eats first
        bear.ate();

        // Bear acts but shouldn't attack as it is not hungry
        bear.act(w);

        assertTrue(w.getTile(rabbitLocation) instanceof Rabbit, "Rabbit should not have been attacked");
    }
}
