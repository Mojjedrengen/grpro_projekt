package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Queue;
import java.util.ArrayList;

import itumulator.world.World;
import itumulator.world.Location;

import simulator.util.PathFinder;
import simulator.actors.Rabbit;
import simulator.objects.Grass;

public class PathFinderTest {

    PathFinder pf;
    World world;

    @BeforeEach
    public void setUp() {
        this.pf = new PathFinder(null);
    }

    @Test
    public void validPathTest() {
        this.world = new World(5);

        world.setTile(new Location(1,0), new Rabbit());

        world.setTile(new Location(1,1), new Rabbit());
        world.setTile(new Location(1,2), new Rabbit());

        world.setTile(new Location(3,4), new Rabbit());
        world.setTile(new Location(3,3), new Rabbit());
        world.setTile(new Location(3,2), new Rabbit());
        world.setTile(new Location(3,1), new Rabbit());

        Location movingRabbitStart = new Location(4,4);
        world.setTile(movingRabbitStart, new Rabbit());

        this.pf.setLocation(movingRabbitStart);
        assertTrue(this.pf.findPathToLocation(new Location(0,0), this.world));

        List<Location> optimalRoute = new ArrayList<Location>() {{
            add(new Location(4,3));
            add(new Location(4,2));
            add(new Location(4,1));
            add(new Location(3,0));
            add(new Location(2,1));
            add(new Location(2,2));
            add(new Location(1,3));
            add(new Location(0,2));
            add(new Location(0,1));
            add(new Location(0,0));
        }};

        int index = 0;
        Queue<Location> foundPath = this.pf.getPath();
        while(!foundPath.isEmpty()) {
            Location l = foundPath.poll();
            assertTrue(l.equals(optimalRoute.get(index)));
            index++;
        }
    }

    @Test
    public void trappedTest() {
        this.world = new World(3);
        world.setTile(new Location(1,0), new Rabbit());
        world.setTile(new Location(1,1), new Rabbit());
        world.setTile(new Location(0,1), new Rabbit());

        this.pf.setLocation(new Location(0,0));
        assertFalse(this.pf.findPathToLocation( new Location(2,2), this.world ));

    }

    @Test
    public void findNearestGrassTest() {
        this.world = new World(5);

        world.setTile(new Location(1,0), new Grass());
        world.setTile(new Location(1,1), new Grass());
        world.setTile(new Location(1,2), new Grass());
        world.setTile(new Location(1,3), new Grass());

        world.setTile(new Location(2,1), new Grass());
        world.setTile(new Location(2,2), new Grass());

        world.setTile(new Location(4,1), new Grass());

        this.pf.setLocation(new Location(4,4));
        this.pf.findPathToNearest(Grass.class, this.world);
        Location closestGrassToLocation4x4y = new Location(2,2);
        assertTrue( this.pf.isFinalLocationInPath(closestGrassToLocation4x4y));

    }


}


