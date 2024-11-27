package test;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Animal;
import simulator.actors.Wolf;
import simulator.actors.WolfPack;
import simulator.actors.Rabbit;
import simulator.objects.holes.WolfHole;
import simulator.util.PathFinder;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Unit test for wolf
public class WolfTest {
    World w;

    @BeforeEach
    public void setUp() {
        this.w = new World(5);
    }

    /**
     * Test that the wolf moves during the day
     */
    @Test
    public void wanderTest() {
        this.w.setDay();

        Wolf wolf = new Wolf();

        Location startingLocation = new Location(0,0); 
        this.w.setTile(startingLocation, wolf);
        this.w.setCurrentLocation(startingLocation);

        wolf.act(this.w);
        Location afterLocation = this.w.getLocation(wolf);

        assertFalse(startingLocation.equals(afterLocation));
    }

    /**
     * Check wolf follows path in its pathfinding, if there is one
     */

    @Test
    public void followsPathFinderPathTest() {
        Wolf wolf = new Wolf();
        Location startingLocation = new Location(0,0);
        Location goalLocation = new Location(4,4);


        // At time = 0, animal registers start of new day and therefore loses energy if it hasn't eaten.
        // Must ensure time isn't 0 so wolf doesn't instantly starve
        this.w.step();
        // If rabbit thinks it hasn't eaten yet, it will overwrite path to nearest grass
        wolf.ate();

        this.w.setCurrentLocation(startingLocation);
        this.w.setTile(startingLocation, wolf);
        PathFinder pf = wolf.getPathFinder();
        pf.setLocation(startingLocation);
        pf.findPathToLocation(goalLocation, this.w);

        // Take 4 steps
        wolf.act(this.w);
        wolf.act(this.w);
        wolf.act(this.w);
        wolf.act(this.w);

        assertTrue( this.w.getLocation(wolf).equals(goalLocation) );
    }

    @Test
    public void seeksWolfPackHoleAtNightTest() {
        this.w.setNight();
        Location wolfLocation = new Location(0,0);
        Location holeLocation = new Location(4,4);

        Wolf wolf = new Wolf();
        this.w.setTile(wolfLocation, wolf);

        WolfHole wh = new WolfHole();
        WolfPack wp = new WolfPack(wh);
        this.w.setTile(holeLocation, wh);

        wolf.joinWolfPack(wp);
        wolf.act(this.w);

        PathFinder pf = wolf.getPathFinder();
        assertTrue( pf.getFinalLocationInPath().equals(holeLocation)  );

    }

    @Test
    public void wolfEatsRabbitTest() {
        this.w.step();
        Location wolfLocation = new Location(0,0);
        Location rabbitLocation = new Location(0,1);

        Wolf wolf = new Wolf();
        Rabbit rabbit = new Rabbit();

        this.w.setTile(wolfLocation, wolf);
        this.w.setTile(rabbitLocation, rabbit);

        assertFalse(this.w.isTileEmpty(rabbitLocation));
        assertTrue(this.w.getTile(rabbitLocation) instanceof Rabbit);

        this.w.setCurrentLocation(wolfLocation);
        wolf.act(this.w);
        // TODO update to check for Carcass once that is added
        assertTrue(this.w.isTileEmpty(rabbitLocation));
    }

    @Test
    public void wolfJoinsWolfPackTest() {

        Wolf w1 = new Wolf();
        Location w1Location = new Location(0,0);

        Wolf w2 = new Wolf();
        Location w2Location = new Location(0,1);

        Location holeLocation = new Location(4,4);
        WolfHole wh = new WolfHole();
        WolfPack wp = new WolfPack(wh);
        this.w.setTile(holeLocation, wh);

        this.w.setTile(w1Location, w1);
        this.w.setTile(w2Location, w2);

        w2.joinWolfPack(wp);

        assertFalse(w1.getWolfPack() == w2.getWolfPack());

        // Wolf 1 should join Wolf 2's wolfpack since it's near
        this.w.setCurrentLocation(w1Location);
        w1.act(this.w);

        assertTrue(w1.getWolfPack() == w2.getWolfPack());

    }

    @Test
    public void wolfReproductionTest() {
        this.w.setNight();

        Wolf w1 = new Wolf();
        Wolf w2 = new Wolf();

        Location holeLocation = new Location(4,4);
        WolfHole wh = new WolfHole();
        WolfPack wp = new WolfPack(wh);
        this.w.setTile(holeLocation, wh);

        w1.joinWolfPack(wp);
        w2.joinWolfPack(wp);

        this.w.setTile(holeLocation, w1);
        wh.wolfEnter(w1, this.w);
        this.w.setTile(holeLocation, w2);
        wh.wolfEnter(w2, this.w);

        assertTrue(wh.getInhabitants().size() == 2);

        for(int i = 0; i < 100; i++) {
            wh.reproduce(this.w);
            wh.resetReproductionAttempt();
        }

        assertTrue(wh.getInhabitants().size() > 2);

    }

}
