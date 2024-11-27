package simulator.objects.holes;


import itumulator.world.Location;
import itumulator.world.World;
import org.jetbrains.annotations.NotNull;
import simulator.actors.Animal;

import java.util.Set;

public class RabbitHole extends Hole{
    private RabbitHoleNetwork network;

    public RabbitHole() {
        super();
        this.inhabitants = null;
        this.network = new RabbitHoleNetwork(this);
    }
    public RabbitHole(RabbitHoleNetwork network) {
        super();
        this.inhabitants = null;
        this.network = network;
    }

    public void enterRabbit(Animal rabbit, World world) {
        if (this.network == null) {return;}
        if (this.network.getInhabitants().contains(rabbit)) {return;}

        Location rabbitLocation = world.getLocation(rabbit);
        if (!rabbitLocation.equals(this.getLocation(world))) {return;}

        this.network.animalEnters(rabbit);
        world.remove(rabbit);
    }

    public void exitRabbit(Animal rabbit, World world) {
        Location holeLocation = this.getLocation(world);
        if (world.isTileEmpty(holeLocation)) {
            this.network.animalExits(rabbit);
            world.setTile(holeLocation, rabbit);
        } else {
            Set<Location> tiles = world.getEmptySurroundingTiles(holeLocation);
            if (tiles.isEmpty()) {
                return;
            }
            this.network.animalExits(rabbit);
            world.setTile(tiles.iterator().next(), rabbit);
        }
    }

    public RabbitHoleNetwork getNetwork() {return network;}

    public boolean predatorNearby(@NotNull World world) {
        return true; // TODO: REMOVE THIS WHEN PREDATOR IS ADDED AND UNCOMMENT BELLOW
        /*Set<Location> neighbors = world.getSurroundingTiles(this.getLocation(world), 3);
        Set<Predator> nearbyPredator = world.getAll(Predator.class, neighbors);
        return !nearbyPredator.isEmpty();*/
    }

    /**
     * Destroys the hole. Please use the Networks destroy hole Method
     * @param world reference to the world
     */
    @Override
    public void destroyHole(World world) {
        super.destroyHole(world);
        network = null;
    }

    @Override
    public Set<Animal> getInhabitants() {
        return network.getInhabitants();
    }

    @Override
    protected void animalRemove(Animal animal) {
        return;
    }

    @Override
    protected void animalAdd(Animal animal) {
        return;
    }
}
