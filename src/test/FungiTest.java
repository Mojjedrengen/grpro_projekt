package test;

import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.objects.Carcass;
import simulator.objects.plants.Fungi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

public class FungiTest {

    private World world;

    @BeforeEach
    public void setUp() {
        world = new World(5); // Create a small 5x5 test world
    }

    /**
     * Test that fungi is initialized correctly with its size and age
     */
    @Test
    public void testInitialization() {
        Fungi smallFungi = new Fungi(false);
        Fungi largeFungi = new Fungi(true);

        assertNotNull(smallFungi.getInformation(), "Small fungi should have display information");
        assertNotNull(largeFungi.getInformation(), "Large fungi should have display information");
        assertEquals(0, smallFungi.getCurrentAge(), "Small fungi should start with age 0");
        assertEquals(0, largeFungi.getCurrentAge(), "Large fungi should start with age 0");
    }

    /**
     * Test fungi spreading to a nearby carcass
     */
    @Test
    public void testSpreadToCarcass() {
        Fungi fungi = new Fungi(false);
        Location fungiLocation = new Location(2, 2);
        Location carcassLocation = new Location(2, 3);

        Carcass carcass = new Carcass(Carcass.smallCarcass, false);
        world.setTile(fungiLocation, fungi);
        world.setTile(carcassLocation, carcass);

        fungi.act(world);

        assertTrue(carcass.hasFungi(), "Carcass should be infected with fungi after spreading");
    }

    /**
     * Test fungi aging when no carcasses are nearby
     */
    @Test
    public void testFungiAgingWithoutCarcasses() {
        Fungi fungi = new Fungi(false);
        Location fungiLocation = new Location(2, 2);
        world.setTile(fungiLocation, fungi);

        for (int i = 0; i < fungi.getMaxAge(); i++) {
            fungi.act(world);
        }

        assertFalse(world.contains(fungi), "Fungi should be removed after reaching max age");
    }

    /**
     * Test fungi does not age when carcasses are nearby
     */
    @Test
    public void testFungiDoesNotAgeWithCarcassesNearby() {
        Fungi fungi = new Fungi(false);
        Location fungiLocation = new Location(2, 2);
        Location carcassLocation = new Location(2, 3);

        Carcass carcass = new Carcass(Carcass.smallCarcass, false);
        world.setTile(fungiLocation, fungi);
        world.setTile(carcassLocation, carcass);

        for (int i = 0; i < fungi.getMaxAge(); i++) {
            fungi.act(world);
        }

        assertTrue(world.contains(fungi), "Fungi should not age and disappear when carcasses are nearby");
    }

    /**
     * Test that fungi does not spread to already infected carcasses
     */
    @Test
    public void testFungiDoesNotSpreadToInfectedCarcass() {
        Fungi fungi = new Fungi(false);
        Location fungiLocation = new Location(2, 2);
        Location carcassLocation = new Location(2, 3);

        Carcass carcass = new Carcass(Carcass.smallCarcass, true); // Already infected
        world.setTile(fungiLocation, fungi);
        world.setTile(carcassLocation, carcass);

        fungi.act(world);

        assertTrue(carcass.hasFungi(), "Infected carcass should remain infected and not double-infect");
    }

    /**
     * Test that large fungi lasts longer than small fungi
     */
    @Test
    public void testLargeFungiLastsLonger() {
        Fungi smallFungi = new Fungi(false);
        Fungi largeFungi = new Fungi(true);

        Location smallFungiLocation = new Location(2, 2);
        Location largeFungiLocation = new Location(3, 3);

        world.setTile(smallFungiLocation, smallFungi);
        world.setTile(largeFungiLocation, largeFungi);

        for (int i = 0; i < smallFungi.getMaxAge(); i++) {
            smallFungi.act(world);
        }

        for (int i = 0; i < largeFungi.getMaxAge() - 1; i++) {
            largeFungi.act(world);
        }

        assertFalse(world.contains(smallFungi), "Small fungi should be removed after max age");
        assertTrue(world.contains(largeFungi), "Large fungi should still exist before its max age");
    }

    /**
     * Test fungi spreading to multiple carcasses
     */
    @Test
    public void testFungiSpreadsToMultipleCarcasses() {
        Fungi fungi = new Fungi(false);
        Location fungiLocation = new Location(2, 2);
        Location carcassLocation1 = new Location(2, 3);
        Location carcassLocation2 = new Location(3, 2);

        Carcass carcass1 = new Carcass(Carcass.smallCarcass, false);
        Carcass carcass2 = new Carcass(Carcass.smallCarcass, false);

        world.setTile(fungiLocation, fungi);
        world.setTile(carcassLocation1, carcass1);
        world.setTile(carcassLocation2, carcass2);

        fungi.act(world);

        assertTrue(carcass1.hasFungi(), "Carcass 1 should be infected with fungi");
        assertTrue(carcass2.hasFungi(), "Carcass 2 should be infected with fungi");
    }
}
