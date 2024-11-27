package test;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Rabbit;
import simulator.objects.holes.OldRabbitHole;
import simulator.objects.plants.Grass;
import simulator.util.PathFinder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Unit test for the Rabbit class
public class RabbitTest {

    World w;

    @BeforeEach
    public void setUp() {
        this.w = new World(5);
    }

    /**
     * Test that the rabbit actually moves around
     */
    @Test
    public void wanderTest() {
        this.w.setDay();
        
        Rabbit r = new Rabbit();

        Location startingLocation = new Location(0,0);
        this.w.setTile(startingLocation, r);

        r.act(this.w);
        Location locationAfterAct = this.w.getLocation(r);
        assertFalse(startingLocation.equals(locationAfterAct));
    }

    /**
     * Test that rabbit follows the path in its pathfinding, if there is one
     */
    @Test
    public void followsPathFinderPathTest() {
        Rabbit r = new Rabbit();
        Location startingLocation = new Location(0,0);
        Location goalLocation = new Location(4,4);


        // At time = 0, animal registers start of new day and therefore loses energy if it hasn't eaten.
        // We must perform a time step so that the rabbit doesn't instantly starve after two steps
        this.w.step();
        // If rabbit thinks it hasn't eaten yet, it will overwrite path to nearest grass
        r.ate();

        this.w.setCurrentLocation(startingLocation);
        this.w.setTile(startingLocation, r);
        PathFinder pf = r.getPathFinder();
        pf.setLocation(startingLocation);
        pf.findPathToLocation(goalLocation, this.w);

        // Take 4 steps
        r.act(this.w);
        r.act(this.w);
        r.act(this.w);
        r.act(this.w);

        assertTrue( this.w.getLocation(r).equals(goalLocation) );
    }

    /**
     * If rabbit hasn't eaten yet, it should actively search for food
     */
    @Test
    public void seeksFoodTest() {
        this.w.step();

        Rabbit r = new Rabbit();
        Location startingLocation = new Location(0,0);
        this.w.setTile(startingLocation, r);

        Grass grass = new Grass();
        Location grassLocation = new Location(4,4);
        this.w.setTile(grassLocation, grass);

        r.act(w);

        assertTrue( r.getPathFinder().getFinalLocationInPath().equals(grassLocation) );

    }

    /**
     * Once it's night time, rabbit should return to its assigned hole if it has one
     */
    @Test
    public void seeksRabbitHoleAtNightTest() {
        Rabbit r = new Rabbit();
        OldRabbitHole rh = new OldRabbitHole();

        Location startingLocation = new Location(0,0);
        Location rabbitHoleLocation = new Location(1,2);

        r.setAssignedNetwork(rh);
        this.w.setNight();

        this.w.setTile(startingLocation, r);
        this.w.setTile(rabbitHoleLocation, rh);
        this.w.setCurrentLocation(startingLocation);

        // First rabbit walks to 1,1, then 1,2 and then enters the hole
        r.act(this.w);
        r.act(this.w);
        r.act(this.w);

        assertTrue(rh.getInhabitants().contains(r));

    }

    /**
     * Rabbit should eat grass and the grass should disappear afterwards
     */
    @Test
    public void rabbitEatsTest() {
        this.w.step();
        Rabbit r = new Rabbit();
        Grass grass = new Grass();
        Location location = new Location(0,0);

        this.w.setTile(location, grass);
        this.w.setTile(location, r);
        assertTrue(this.w.containsNonBlocking(location));

        // Rabbit will wander before eating the grass it's on, so it should eat grass within 2 steps
        r.act(this.w);
        r.act(this.w);

        assertFalse(this.w.containsNonBlocking(location));
    }

    /**
     * Given enough chances, a rabbit should reprodouce if it finds itself inside a rabbit hole with another rabbit
     */
    @Test
    public void rabbitReproductionTest() {
        this.w.setNight();

        Rabbit r1 = new Rabbit();
        Rabbit r2 = new Rabbit();
        OldRabbitHole rh = new OldRabbitHole();

        Location location = new Location(0,0);

        r1.setAssignedNetwork(rh);
        r2.setAssignedNetwork(rh);

        this.w.setTile(location, rh);

        this.w.add(r1);
        this.w.add(r2);

        rh.rabbitEntersNetwork(r1);
        rh.rabbitEntersNetwork(r2);

        for(int i = 0; i < 100; i++) {
            r1.reproduce(w);
            r2.reproduce(w);
        }

        assertTrue( rh.getInhabitants().size() > 2 );

    }

}
