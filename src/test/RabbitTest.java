package test;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Animal;
import simulator.actors.Rabbit;
import simulator.objects.RabbitHole;
import simulator.objects.Grass;
import simulator.util.PathFinder;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RabbitTest {

    World w;

    @BeforeEach
    public void setUp() {
        this.w = new World(5);
    }

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

    @Test
    public void followsPathFinderPathTest() {
        Rabbit r = new Rabbit();
        Location startingLocation = new Location(0,0);
        Location goalLocation = new Location(4,4);

        PathFinder pf = r.getPathFinder();

        pf.setLocation(startingLocation);
        pf.findPathToLocation(goalLocation, this.w);

        // Path finder gives shortest route which in this case would be going
        // diagonally across the world
        assertTrue(pf.getPath().size() == 4);

        // At time = 0, animal registers start of new day and therefore loses energy if it hasn't eaten.
        // We must perform a time step so that the rabbit doesn't instantly starve after two steps
        this.w.step();
        // If rabbit thinks it hasn't eaten yet, it will overwrite path to nearest grass
        r.ate();

        this.w.setCurrentLocation(startingLocation);
        this.w.setTile(startingLocation, r);
        // Take 4 steps
        r.act(this.w);
        r.act(this.w);
        r.act(this.w);
        r.act(this.w);

        assertTrue( this.w.getLocation(r).equals(goalLocation) );
    }

    // If rabbit hasn't eaten yet, it will actively hunt for food
    @Test
    public void seeksFoodTest() {
        this.w.step();

        Rabbit r = new Rabbit();
        Location startingLocation = new Location(0,0);
        this.w.setTile(startingLocation, r);

        Grass grass = new Grass();
        Location grassLocation = new Location(4,4);
        this.w.setTile(grassLocation, grass);

        r.act(this.w);

        PathFinder pf = r.getPathFinder();

        assertTrue( pf.getFinalLocationInPath().equals(grassLocation) );

    }

    @Test
    public void seeksRabbitHoleAtNightTest() {
        Rabbit r = new Rabbit();
        RabbitHole rh = new RabbitHole();

        Location startingLocation = new Location(0,0);
        Location rabbitHoleLocation = new Location(1,2);

        r.assignHole(rh);
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

    @Test
    public void rabbitReproductionTest() {
        this.w.setNight();

        Rabbit r1 = new Rabbit();
        Rabbit r2 = new Rabbit();
        RabbitHole rh = new RabbitHole();

        Location location = new Location(0,0);

        r1.assignHole(rh);
        r2.assignHole(rh);

        this.w.setTile(location, rh);

        this.w.add(r1);
        this.w.add(r2);

        rh.animalEnters(r1);
        rh.animalEnters(r2);

        for(int i = 0; i < 100; i++) {
            r1.reproduce(w);
            r2.reproduce(w);
        }

        assertTrue( rh.getInhabitants().size() > 2 );

    }

}
