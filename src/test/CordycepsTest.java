package test;

import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.actors.Bear;
import simulator.actors.Rabbit;
import simulator.actors.Wolf;
import simulator.actors.cordyceps.InfectedAnimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
class CordycepsTest {
    InfectedAnimal<Rabbit> infectedRabbit;
    InfectedAnimal<Wolf> infectedWolf;
    InfectedAnimal<Bear> infectedBear;

    @BeforeEach
    void setUp() {

    }

    @Test
    void spreadToRabbitTest() {
        Random rand = new Random();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Rabbit rabbit = new Rabbit();
            World world = new World(10);
            world.setTile(new Location(0,0), rabbit);
            int j = 0;
            while (world.contains(rabbit)) {
                infectedRabbit = new InfectedAnimal<>(Rabbit.class);
                Location l = new Location(rand.nextInt(1, 3), 0);
                world.setTile(l, infectedRabbit);
                world.setCurrentLocation(l);
                infectedRabbit.killAnimal(world);
                j++;
            }
            list.add(j);
        }
        int sum = 0;
        for (int i : list) {
            sum += i;
        }
        assertEquals(5, Math.round(sum/(double)list.size()));
    }

    @Test
    void spreadToWolfTest() {
        Random rand = new Random();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Wolf wolf = new Wolf();
            World world = new World(10);
            world.setTile(new Location(0,0), wolf);
            int j = 0;
            while (world.contains(wolf)) {
                infectedWolf = new InfectedAnimal<>(Wolf.class);
                Location l = new Location(rand.nextInt(1, 3), 0);
                world.setTile(l, infectedWolf);
                world.setCurrentLocation(l);
                infectedWolf.killAnimal(world);
                j++;
            }
            list.add(j);
        }
        int sum = 0;
        for (int i : list) {
            sum += i;
        }
        assertEquals(5, Math.round(sum/(double)list.size()));
    }

    @Test
    void spreadToBearTest() {
        Random rand = new Random();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Bear bear = new Bear();
            World world = new World(10);
            world.setTile(new Location(0, 0), bear);
            int j = 0;
            while (world.contains(bear)) {
                infectedBear = new InfectedAnimal<>(Bear.class);
                Location l = new Location(rand.nextInt(1, 3), 0);
                world.setTile(l, infectedBear);
                world.setCurrentLocation(l);
                infectedBear.killAnimal(world);
                j++;
            }
            list.add(j);
        }
        int sum = 0;
        for (int i : list) {
            sum += i;
        }
        assertEquals(5, Math.round(sum / (double) list.size()));
    }

    @Test
    void decompose() {
        World w = new World(5);
        infectedRabbit = new InfectedAnimal<>(Rabbit.class);
        infectedWolf = new InfectedAnimal<>(Wolf.class);
        infectedBear = new InfectedAnimal<>(Bear.class);
        for (int i = 0; i < 10; i++) {
            if (w.getCurrentTime() == World.getTotalDayDuration()/2) {
                assertEquals(infectedRabbit.maxHealth-1, infectedRabbit.getHealth());
                assertEquals(infectedWolf.maxHealth-1, infectedWolf.getHealth());
                assertEquals(infectedBear.maxHealth-1, infectedBear.getHealth());
                break;
            }
            w.step();
        }
    }
}