package simulator.objects.plants;

import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.Location;

import simulator.util.Utilities;
import simulator.objects.NonBlockable;

public abstract class Plant extends NonBlockable implements Actor {

    public enum Stage {
        ERROR,
        NOT_GROWN,
        SPROUTING,
        GROWN,
        RIPE;

        public Stage getNext() {
            switch(this) {
                case NOT_GROWN:
                    return SPROUTING;
                case SPROUTING:
                    return GROWN;
                case GROWN:
                    return RIPE;
                case RIPE:
                    return RIPE;
                default: // Hopefully this doesn't happen
                    return ERROR;
            }
        }
    }

    // Growth state
    Stage currentStage;
    // Chances of plant spreading, between 0-100
    protected final int spreadChance;
    // Chances of plant growing, between 0-100
    protected final int growthChance;

    final Supplier<? extends Plant> dynamicPlantInstanceSuplier;

    public Plant(final int spreadChance, final int growthChance, final Supplier<? extends Plant> dynamicPlantInstanceSuplier) {
        this.currentStage = Stage.NOT_GROWN;
        this.spreadChance = spreadChance;
        this.growthChance = growthChance;
        this.dynamicPlantInstanceSuplier = dynamicPlantInstanceSuplier;
    }

    /**
     * Attempt to make the plant grow to its next stage.
     * Uses the growth chance of the plant to determine if it should grow.
     *
     * @param random instance of Random
     */
    protected void grow(Random random) {

        if(random.nextInt(1, 101) <= this.growthChance) {
            this.currentStage = this.currentStage.getNext();
        }
    }

    /**
     * Attempt to make plant to spread to surrounding tiles
     * Assumes that current location is set correctly.
     *
     * @param random Instace of Random
     * @param world reference to the world
     */
    protected void spread(World world, Random random) {
        // If not ripe, then don't spread
        // If spread rng fails, then also don't spread
        if(this.currentStage != Stage.RIPE || 
           random.nextInt(1, 101) > this.spreadChance) return;

        // Assuming current location is set correctly
        // This spread method is usually used from within the act method
        Set<Location> neighbours = world.getSurroundingTiles();
        neighbours.removeIf(world::containsNonBlocking);

        // Returns null if set was empty
        Location location = Utilities.getRandomFromSet(neighbours, random);
        if(location == null) return;

        world.setTile(location, this.dynamicPlantInstanceSuplier.get());
    }

    public Stage getCurrentStage() {
        return this.currentStage;
    }

    public void setToRipe () { //For testing
        this.currentStage = Stage.RIPE;
    }

    public int getSpreadChance() {
        return this.spreadChance;
    } 

    public boolean consume(World world) {
        if(this.currentStage != Stage.RIPE) return false;

        this.currentStage = Stage.SPROUTING;
        return true;
    }

}

