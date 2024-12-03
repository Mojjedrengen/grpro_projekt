package test;

import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import simulator.actors.Predator;
import simulator.actors.Rabbit;
import simulator.actors.Wolf;
import simulator.objects.holes.RabbitHole;
import simulator.objects.holes.RabbitHoleNetwork;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RabbitHoleAndNetworkTest {
    RabbitHole hole;
    RabbitHoleNetwork network;
    World world;

    @BeforeEach
    void setUp() {
        this.world = new World(5);
        this.network = RabbitHoleNetwork.getInstance();
        this.hole = new RabbitHole();
    }

    @Test
    void partOfNetworkTest(){
        assertTrue(this.network.getEntrances().contains(hole));
    }

    @Test
    void rabbitEntersTest() {
        Rabbit rabbit = new Rabbit();
        Location l = new Location(0, 0);
        this.world.setTile(l, this.hole);
        this.world.setTile(l, rabbit);
        this.hole.enterRabbit(rabbit, world);
        assertTrue(this.network.getInhabitants().contains(rabbit));
    }

    @Test
    void rabbitExitsTest() {
        Rabbit rabbit = new Rabbit();
        Location l = new Location(0, 0);
        this.world.setTile(l, this.hole);
        this.world.setTile(l, rabbit);
        this.hole.enterRabbit(rabbit, world);
        assertTrue(this.network.getInhabitants().contains(rabbit));
        this.hole.exitRabbit(rabbit, world);
        assertFalse(this.network.getInhabitants().contains(rabbit));
    }

    @Test
    void predatorNearTest() {
        this.world.setTile(new Location(0, 1), this.hole);
        assertFalse(this.hole.predatorNearby(this.world));
        Predator predator = new Wolf();
        this.world.setTile(new Location(0, 0), predator);
        assertTrue(this.hole.predatorNearby(this.world));

        this.world.delete(predator);
    }

    @Test
    void exitThroughDifferentHoleTest() {
        Location l = new Location(0, 0);
        Location nl = new Location(0, 1);
        Rabbit rabbit = new Rabbit();
        this.world.setTile(l, this.hole);
        this.world.setTile(l, rabbit);
        this.hole.enterRabbit(rabbit, world);
        assertTrue(this.network.getInhabitants().contains(rabbit));
        RabbitHole newHole = new RabbitHole();
        this.world.setTile(nl, newHole);
        newHole.exitRabbit(rabbit, world);
        Map<Object, Location> entities = this.world.getEntities();
        assertEquals(nl, entities.get(rabbit));
    }

    @AfterEach
    void tearDown() {
        if (this.world.contains(hole)) {
            this.world.delete(this.hole);
        }
    }
}
