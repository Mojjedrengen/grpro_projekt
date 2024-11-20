package simulator.objects;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Class to create grass
 * @author Moto
 */
public class Grass extends NonBlockable implements DynamicDisplayInformationProvider {
    private final int chanceToSpreed = 10; // this is the chance for the grass to spread. Current is 1/10
    private int grassStage = 0; // The stage of the grass
    private final int grassMaxStage = 3; // the max stage for the grass

    /**
     * Empty Constructor may be used later
     */
    public Grass() {
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
        if (grassStage < grassMaxStage && rand.nextInt(5) == 0) {
            grassStage++;
        }
        Set<Location> neighbours = world.getSurroundingTiles();
        neighbours.removeIf(world::containsNonBlocking);
        List<Location> list = new ArrayList<>(neighbours);
        if (list.isEmpty()) return;
        if (rand.nextInt(chanceToSpreed) == 0 && grassStage == grassMaxStage) {
            Location location = list.get(rand.nextInt(0, list.size()));
            Grass spreed = new Grass();
            world.setTile(location, spreed);
            //todo: Lav også noget her til at tilføge græset til listen.
        }
    }

    /**
     * Animals can consume grass
     *
     * @param world - Reference to the world
     */
    public void consume(World world) {
        // TODO
    }

    /**
     * This method changes the grass color depending on its grassStage
     * @return DisplayInformation(enum color) - How the grass looks like.
     */
    @Override
    public DisplayInformation getInformation() {
        if (grassStage < grassMaxStage) return new DisplayInformation(Color.yellow);
        return new DisplayInformation(Color.green);
    }
}
