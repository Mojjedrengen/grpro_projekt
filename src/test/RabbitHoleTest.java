package test;

import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.actors.Animal;
import simulator.actors.Rabbit;
import simulator.objects.RabbitHole;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RabbitHoleTest {
    RabbitHole hole;

    @BeforeEach
    void setUp() {
        hole = new RabbitHole();
    }

    @Test
    void connectHole() {
        RabbitHole connected = new RabbitHole(hole);
        hole.connectHole(connected);

        assertEquals(hole.getConnectedHoles(), connected.getConnectedHoles());
    }

    @Test
    void disconnectHole() {
        RabbitHole connected = new RabbitHole(hole);
        hole.connectHole(connected);
        hole.disconnectHole(connected);

        assertEquals(1, hole.getConnectedHoles().size());
    }

    @Test
    void animalEnters() {
        Animal rabbit = new Rabbit();
        hole.animalEnters(rabbit);

        assertEquals(1, hole.getInhabitants().size());
    }

    @Test
    void animalLeave() {
        Animal rabbit = new Rabbit();
        hole.animalEnters(rabbit);
        assertEquals(1, hole.getInhabitants().size());

        hole.animalLeave(rabbit);
        assertEquals(0, hole.getInhabitants().size());
    }

    @Test
    void getLocation() {
        World world = new World(5);
        Location l = new Location(0, 0);
        world.setCurrentLocation(l);
        world.setTile(l, hole);
        assertEquals(0, hole.getLocation(world).getX());
        assertEquals(0, hole.getLocation(world).getY());
    }

    @Test
    void destroyHole() {
        World world = new World(5);
        Location l = new Location(0, 0);
        world.setCurrentLocation(l);
        world.setTile(l, hole);
        for (int i = 0; i < 10; i++) {
            hole.connectHole(new RabbitHole(hole.getConnectedHoles()));
            assertEquals(i+2, hole.getConnectedHoles().size());
        }
        Set<RabbitHole> connectedHoles = hole.getConnectedHoles();
        hole.destroyHole(world);
        for (RabbitHole connected : connectedHoles) {
            assertFalse(connected.getConnectedHoles().contains(hole));
        }
        assertNull(hole.getConnectedHoles());
        assertFalse(world.contains(hole));
    }
}