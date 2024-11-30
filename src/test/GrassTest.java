package test;

import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.objects.plants.Grass;
import simulator.objects.plants.Plant;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GrassTest {
    World w;

    @BeforeEach
    void setUp() {
        w = new World(5);
    }

    @Test
    void spreadChanceTest() {

        Grass grass = new Grass();
        this.w.step();

        int expectedGrass = grass.getSpreadChance();

        Location grassLocation = new Location(0,0);
        this.w.setTile(grassLocation, grass);
        this.w.setCurrentLocation(grassLocation);

        final int iterations = 100_000;
        int actualGrass = 0;
        for(int i = 0; i < iterations; i++) {
            grass.act(this.w);
            for(Location l : this.w.getSurroundingTiles(grassLocation)) {
                if(this.w.containsNonBlocking(l) && this.w.getNonBlocking(l) instanceof Grass g) {
                    actualGrass += 1;
                    this.w.delete(g);
                }
            }
        }


        double expectedChance = ((double)expectedGrass) / 100.0;
        double actualChance = ((double)actualGrass) / ((double)iterations);

        // allow 5% margin of error
        assertTrue( expectedChance - 0.05 <= actualChance && actualChance <= expectedChance + 0.05 );

        /* Spreading works a bit differently now that we have the Plant abstract class
        List<Integer> timeToSpread = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
           Grass g = new Grass();
           Location l = new Location(0, 0);
           w.setCurrentLocation(l);
           w.setTile(l, g);
           while (g.getCurrentStage() != Plant.Stage.RIPE) {
               g.act(w);
           }
           boolean whileLoop = true;
           int j = 0;
           while (whileLoop) {
               if (w.getSurroundingTiles().isEmpty()) {
                   g.act(w);
                   j++;
               } else {
                   timeToSpread.add(j);
                   Map<Object, Location> entities = w.getEntities();
                   entities.forEach((k, v) -> {
                       w.delete(k);
                   });
                   whileLoop = false;
               }
           }
        }
        long sum = 0;
        for (int n : timeToSpread) {
            sum += n;
        }
        double chance = sum / (double)timeToSpread.size();
        assertEquals(new Grass().getSpreadChance(), chance);
        */
    }

    @Test
    void getInformationTest() {
        Grass g = new Grass();
        Location l = new Location(0, 0);
        w.setCurrentLocation(l);
        w.setTile(l, g);
        for (int i = 0; i < 50; i++) {
            g.act(w);
            if (g.getCurrentStage() != Plant.Stage.RIPE) {
                assertEquals(Color.yellow, g.getInformation().getColor());
            } else {
                assertEquals(Color.green, g.getInformation().getColor());
            }
        }
    }
}
