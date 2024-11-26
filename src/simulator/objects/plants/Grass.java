package simulator.objects.plants;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;

/**
 * Class to create grass. Grass can spread own its own and has different stages it goes through as it is growing.
 * Certain animals can eat grass to regain energy.
 * @author Moto
 */
public class Grass extends Plant implements DynamicDisplayInformationProvider {

    static final DisplayInformation yellowGrass = new DisplayInformation(Color.yellow);
    static final DisplayInformation greenGrass = new DisplayInformation(Color.green);

    public Grass() {
        // spread chance 10/100, growth chance 25/100, how to call the grass constructor
        super(10, 25, () -> {return new Grass();} );
    }

    /**
     * Grass' act method that is call evey step of world.
     * It creates a new Grass object in a sounding tile on a 1/5 chance if the grassStage equal to maxGrassStage
     * @param world providing details of the position on which the actor is currently located and much more.
     *
     */
    @Override
    public void act(World world) {
        Random rand = new Random();

        this.spread(world, rand);
        this.grow(rand);
    }

    /**
     * Animals can consume grass
     *
     * @param world - Reference to the world
     */
    @Override
    public boolean consume(World world) {
        world.delete(this);
        return true;
    }

    /**
     * This method changes the grass color depending on its grassStage
     * @return DisplayInformation(enum color) - How the grass looks like.
     */
    @Override
    public DisplayInformation getInformation() {
        if (this.getCurrentStage() != Plant.Stage.RIPE) return Grass.yellowGrass;
        return Grass.greenGrass;
    }

}
