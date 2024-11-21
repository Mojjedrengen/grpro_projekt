package test;

import itumulator.executable.DisplayInformation;
import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.objects.Grass;

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
        List<Integer> timeToSpread = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
           Grass g = new Grass();
           Location l = new Location(0, 0);
           w.setCurrentLocation(l);
           w.setTile(l, g);
           while (g.getGrassStage() != g.getGrassMaxStage()) {
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
        assertEquals(new Grass().getGrassStage(), chance);
    }

    @Test
    void getInformationTest() {
        Grass g = new Grass();
        Location l = new Location(0, 0);
        w.setCurrentLocation(l);
        w.setTile(l, g);
        for (int i = 0; i < 50; i++) {
            g.act(w);
            if (g.getGrassStage() != g.getGrassMaxStage()) {
                assertEquals(Color.yellow, g.getInformation().getColor());
            } else {
                assertEquals(Color.green, g.getInformation().getColor());
            }
        }
    }
}