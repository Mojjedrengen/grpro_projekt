package test;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Animal;
import simulator.actors.Wolf;
import simulator.actors.WolfPack;
import simulator.actors.Rabbit;
import simulator.objects.holes.WolfHole;
import simulator.objects.Carcass;
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
    public void wolfKillsRabbitTest() {
        this.w.step();
        Location wolfLocation = new Location(0,0);
        Location rabbitLocation = new Location(0,1);

        Wolf wolf = new Wolf();
        Rabbit rabbit = new Rabbit();

        this.w.setTile(wolfLocation, wolf);
        this.w.setTile(rabbitLocation, rabbit);

        assertFalse(this.w.isTileEmpty(rabbitLocation));
        assertTrue(!this.w.containsNonBlocking(rabbitLocation));
        assertTrue(this.w.getTile(rabbitLocation) instanceof Rabbit);

        this.w.setCurrentLocation(wolfLocation);
        wolf.act(this.w);
        assertTrue(this.w.containsNonBlocking(rabbitLocation));
        assertTrue(this.w.getNonBlocking(rabbitLocation) instanceof Carcass);
        assertFalse( this.w.getTile(rabbitLocation) instanceof Rabbit );
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

    @Test
    public void wolfEatsCarcassTest() {
        this.w.setDay();
        this.w.step();

        Wolf w1 = new Wolf();
        Location wLocation = new Location(0,0);

        Carcass c = new Carcass(Carcass.bigCarcass, false);
        Location cLocation = new Location(0,0);

        this.w.setTile(wLocation, w1);
        this.w.setTile(cLocation, c);

        final int currentMeat = c.getMeatLeft();
        this.w.setCurrentLocation(wLocation);
        w1.act(this.w);

        assertTrue(currentMeat != c.getMeatLeft());
    }

    /**
     * If wolf has eaten and it is day, then it will try to follow member
     */
    @Test
    public void wolfFollowsWolfPackMemberTest() {
        this.w.setDay();
        this.w.step();

        Wolf w1 = new Wolf();
        Wolf w2 = new Wolf();
        Location w1LocationStart = new Location(0,0);
        Location w2LocationStart = new Location(4,4);

        Location holeLocation = new Location(4,4);
        WolfHole wh = new WolfHole();
        WolfPack wp = new WolfPack(wh);
        this.w.setTile(holeLocation, wh);

        this.w.setTile(w2LocationStart, w2);
        this.w.setTile(w1LocationStart, w1);
        w1.joinWolfPack(wp);
        w2.joinWolfPack(wp);

        // Ensure that wolves aren't hungry
        w1.ate();
        w2.ate();

        PathFinder w1pf = w1.getPathFinder();
        assertTrue(!w1pf.hasPath());

        this.w.setCurrentLocation(w1LocationStart);
        w1.act(this.w);

        boolean result = false;
        Location destinationPathFinder = w1pf.getFinalLocationInPath();

        for(Location location : this.w.getSurroundingTiles(w2LocationStart)) {
            if(destinationPathFinder.equals(location)) result = true;
        }

        assertTrue(result);

    }

    @Test
    public void wolfAttacksNonWolfPackMembersTest() {
        this.w.setDay();
        this.w.step();

        Wolf w1 = new Wolf();
        Wolf w2 = new Wolf();
        Location w1LocationStart = new Location(0,0);
        Location w2LocationStart = new Location(0,1);

        this.w.setTile(w1LocationStart, w1);
        this.w.setTile(w2LocationStart, w2);

        WolfPack wp1 = new WolfPack(this.w);
        WolfPack wp2 = new WolfPack(this.w);

        w1.joinWolfPack(wp1);
        w2.joinWolfPack(wp2);

        assertTrue( w2.getHealth() == w2.maxHealth );

        this.w.setCurrentLocation(w1LocationStart);
        // If wolf 1 is hungry, then it will priotize trying to find 
        // rabbit or carcass to eat
        w1.ate();
        w1.act(this.w);

        assertFalse( w2.getHealth() == w2.maxHealth );

    }

}
