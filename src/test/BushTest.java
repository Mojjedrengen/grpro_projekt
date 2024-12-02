
package test;

import itumulator.world.Location;
import itumulator.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.objects.plants.Bush;
import simulator.objects.plants.Plant;

import static org.junit.jupiter.api.Assertions.*;

class BushTest {
    World w;
    int worldSize;

    @BeforeEach
    void setUp() {
        this.worldSize = 5;
        this.w = new World(this.worldSize);
    }

    @Test
    void bushSpreadChanceTest() {
        Bush bush = new Bush();
        this.w.step();

        int expectedBushes = bush.getSpreadChance();

        Location bushLocation = new Location(0,0);
        this.w.setTile(bushLocation, bush);
        this.w.setCurrentLocation(bushLocation);

        final int iterations = 1000;
        int actualBushes = 0;
        for(int i = 0; i < iterations; i++) {
            bush.act(this.w);
            for(Location l : this.w.getSurroundingTiles(bushLocation)) {
                if(this.w.containsNonBlocking(l) && this.w.getNonBlocking(l) instanceof Bush b) {
                    actualBushes += 1;
                    this.w.delete(b);
                }
            }
        }


        double expectedChance = ((double)expectedBushes) / 100.0;
        double actualChance = ((double)actualBushes) / ((double)iterations);

        // allow 2% margin of error
        assertTrue( expectedChance - 0.02 <= actualChance && actualChance <= expectedChance + 0.02  );

    }

    @Test
    void bushGetInformationTest() {
        Bush bush = new Bush();
        this.w.step();
        this.w.setTile(new Location(0,0), bush);
        assertTrue(bush.getInformation() == Bush.regularBush);

        while(bush.getCurrentStage() != Plant.Stage.RIPE)
            bush.act(this.w);

        assertTrue(bush.getInformation() == Bush.ripeBush);

    }

    @Test
    void bushDiesTest() {
        Bush bush = new Bush();
        Location bushLocation = new Location(0,0);
        this.w.setTile(bushLocation, bush);
        this.w.setCurrentLocation(bushLocation);

        assertTrue(this.w.containsNonBlocking(bushLocation));

        // World time is = 0.
        // Bush ages whenever time is 0
        // This therefore simulates 6 entire days
        for(int i = 0; i <= Bush.bushMaxAge; i++) {
            bush.act(this.w);
        }

        assertFalse(this.w.containsNonBlocking(bushLocation));

    }

}
