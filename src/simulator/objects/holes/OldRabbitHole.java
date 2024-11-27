package simulator.objects.holes;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import simulator.actors.Animal;
import simulator.actors.Rabbit;


/**
 * The RabbitHole class represents a rabbit hole that rabbits can enter at night.
 * This can also be used to represent a network of connected rabbit holes.
 * @author moto
 */
@Deprecated
public class OldRabbitHole extends Hole {
    private Set<OldRabbitHole> connectedHoles;

    /**
     * Constructor to create single hole
     */
    public OldRabbitHole() {
        super();
        this.connectedHoles = new HashSet<>();
        this.connectedHoles.add(this);
    }

    /**
     * Constructor to create a hole that is connected to a single other hole
     * @param hole the connected hole
     */
    public OldRabbitHole(OldRabbitHole hole) {
        super();
        this.connectedHoles = new HashSet<>();
        this.connectedHoles.add(this);
        this.connectedHoles.add(hole);
    }

    /**
     * Constructor to create a hole that is a part of a hole network
     * @param holes the hole network
     */
    public OldRabbitHole(Set<OldRabbitHole> holes) {
        super();
        this.connectedHoles = new HashSet<>(holes);
        this.connectedHoles.add(this);
    }

    /**
     * Method for the rabbit to reproduce inside the rabbit hole
     * @param world the current world
     */
    public void reproduceInhabitants(World world) {
        Random random = new Random();
        Set<Animal> offSpringSet = new HashSet<>();
        for (Animal animal : this.inhabitants) {
            // Ensure there are at least two animals to reproduce
            if (this.inhabitants.size() > 1 && random.nextInt(20) == 4 && offSpringSet.size() < this.inhabitants.size()/2) {
                Animal offspring = new Rabbit(this); // Creates a new Rabbit
                offSpringSet.add(offspring); // Adds the offspring to the rabbit hole
            }
        }
        if (!offSpringSet.isEmpty()) {
            for (Animal offSpring : offSpringSet) {
                this.rabbitEntersNetwork(offSpring);
                world.add(offSpring);
            }
        }
    }

    /**
     * Method for when a rabbits enters the hole network
     * @param rabbit the rabbit that entered teh network
     * @param world reference to the world
     */
    public void enterRabbit(Animal rabbit, World world) {
        if(this.inhabitants.contains(rabbit)) {
            // Rabbit is already inside
            return;
        }

        Location rabbitLocation = world.getLocation(rabbit);
        if(!rabbitLocation.equals(this.getLocation(world))) {
            // Rabbit cannot enter hole without being above it
            return;
        }

        this.rabbitEntersNetwork(rabbit);
        world.remove(rabbit);
    }

    /**
     * Method for when a rabbit leave the hole network
     * @param rabbit the rabbit that leaves the hole
     * @param world the current world
     */
    public void exitRabbit(Animal rabbit, World world) {
        Location holeLocation = this.getLocation(world);

        // If the rabbit hole tile is empty, place the rabbit there
        if (world.isTileEmpty(holeLocation)) {
            this.rabbitExitsNetwork(rabbit); // Remove the rabbit from the hole
            world.setTile(holeLocation, rabbit); // Place the rabbit on the tile
        } else {
            // If the rabbit hole is blocked, place the rabbit on a surrounding empty tile
            Set<Location> tiles = world.getEmptySurroundingTiles(holeLocation);
            if (tiles.isEmpty()) {
                // Rabbit can't exit, everything is blocked
                return;
            }
            this.rabbitExitsNetwork(rabbit); // Remove the rabbit from the hole
            world.setTile(tiles.iterator().next(), rabbit); // Place the rabbit on a nearby tile
        }
    }


    /**
     * Method to connect a hole to the hole network
     * @param hole hole to connect to network
     */
    public void connectHole(OldRabbitHole hole) {
        this.connectedHoles.add(hole);
    }

    /**
     * Method to disconnect a hole from the hole network
     * @param hole hole to disconnect from network
     */
    public void disconnectHole(OldRabbitHole hole) {
        this.connectedHoles.remove(hole);
    }

    /**
     * Method to add an Animal to the inhabitants of the hole network when it enters
     * @param animal the animal that enters the hole
     */
    public void rabbitEntersNetwork(Animal animal) {
        for (OldRabbitHole hole : this.connectedHoles) {
            hole.animalAdd(animal);
        }
    }

    /**
     * Method to remove an Animal from the inhabitants of the hole network when it leaves
     * @param animal animal that leaves
     */
    public void rabbitExitsNetwork(Animal animal) {
        for (OldRabbitHole hole : this.connectedHoles) {
            hole.animalRemove(animal);
        }
    }

    /**
     * Getter to get the hole network
     * @return the hole network
     */
    public Set<OldRabbitHole> getConnectedHoles() {
        return this.connectedHoles;
    }

    /**
     * Method to destroy the hole.
     * Removes the hole from the network then deletes it form the world
     * @param world the current world
     */
    @Override
    public void destroyHole(World world) {
        Iterator<OldRabbitHole> it = this.connectedHoles.iterator();

        while (it.hasNext()) {
            OldRabbitHole hole = it.next();
            if (hole.equals(this)) {continue;}
            it.remove();
        }
        this.connectedHoles = null;
        this.inhabitants = null;
        world.delete(this);
    }
}
