package simulator.objects;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import simulator.actors.Animal;
import simulator.actors.Rabbit;


/**
 * A hole that rabbits can go into during the night
 * @author moto
 */
public class RabbitHole extends NonBlockable {
    private Set<RabbitHole> connectedHoles;
    private Set<Animal> inhabitants;

    /**
     * Constructor to create single hole
     */
    public RabbitHole() {
        this.connectedHoles = new HashSet<>();
        this.inhabitants = new HashSet<>();
        this.connectedHoles.add(this);
    }

    public void reproduceInhabitants(World world) {
    Random random = new Random();
    for (Animal animal : this.inhabitants) {
        // Ensure there are at least two animals to reproduce
        if (this.inhabitants.size() > 1 && random.nextInt(20) == 4) {
            Animal offspring = new Rabbit(); // Creates a new Rabbit
            this.animalAdd(offspring); // Adds the offspring to the rabbit hole
            Location holeLocation = this.getLocation(world);
            if (world.isTileEmpty(holeLocation)) {
                world.setTile(holeLocation, offspring); // Places the offspring in the world
            } else {
                // Place offspring in one of the surrounding tiles
                Set<Location> surroundingTiles = world.getEmptySurroundingTiles(holeLocation);
                if (!surroundingTiles.isEmpty()) {
                    world.setTile(surroundingTiles.iterator().next(), offspring);
                }
            }
        }
    }
}

    /**
     * Constructor to create a hole that is connected to a single other hole
     * @param hole the connected hole
     */
    public RabbitHole(RabbitHole hole) {
        this.connectedHoles = new HashSet<>();
        this.inhabitants = new HashSet<>();
        this.connectedHoles.add(this);
        this.connectedHoles.add(hole);
    }

    public void exitRabbit(Animal rabbit, World world) {
        Location holeLocation = this.getLocation(world);
    
        // If the rabbit hole tile is empty, place the rabbit there
        if (world.isTileEmpty(holeLocation)) {
            this.animalLeave(rabbit); // Remove the rabbit from the hole
            world.setTile(holeLocation, rabbit); // Place the rabbit on the tile
        } else {
            // If the rabbit hole is blocked, place the rabbit on a surrounding empty tile
            Set<Location> tiles = world.getEmptySurroundingTiles(holeLocation);
            if (tiles.isEmpty()) {
                // Rabbit can't exit, everything is blocked
                return;
            }
            this.animalLeave(rabbit); // Remove the rabbit from the hole
            world.setTile(tiles.iterator().next(), rabbit); // Place the rabbit on a nearby tile
        }
    }
    

    /**
     * Constructor to create a hole that is a part of a hole network
     * @param holes the hole network
     */
    public RabbitHole(Set<RabbitHole> holes) {
        this.connectedHoles = new HashSet<>(holes);
        this.inhabitants = new HashSet<>();
        this.connectedHoles.add(this);
    }

    /**
     * Method to connect a hole to the hole network
     * @param hole hole to connect to network
     */
    public void connectHole(RabbitHole hole) {
        this.connectedHoles.add(hole);
    }

    /**
     * Method to disconnect a hole from the hole network
     * @param hole hole to disconnect from network
     */
    public void disconnectHole(RabbitHole hole) {
        this.connectedHoles.remove(hole);
    }

    /**
     * Method to add an Animal to the inhabitants of the hole network when it enters
     * @param animal the animal that enters the hole
     */
    public void animalEnters(Animal animal) {
        for (RabbitHole hole : this.connectedHoles) {
            hole.animalAdd(animal);
        }
    }
    /**
     * Method to add an Animal to the inhabitants set.
     * SHOULD ONLY BE USED BY OTHER ANIMAL HOLES USE {@link #animalEnters(Animal animal)} INSTEAD.
     * @param animal animal to add
     */
    public void animalAdd(Animal animal) {
        this.inhabitants.add(animal);
    }

    /**
     * Method to remove an Animal from the inhabitants of the hole network when it leaves
     * @param animal animal that leaves
     */
    public void animalLeave(Animal animal) {
        for (RabbitHole hole : this.connectedHoles) {
            hole.animalRemove(animal);
        }
    }

    /**
     * Method to remove an Animal from the inhabitants set
     * SHOULD ONLY BE USED BY OTHER ANIMAL HOLES USE {@link #animalLeave(Animal)} INSTEAD.
     * @param animal animal to remove
     */
    public void animalRemove(Animal animal) {
        this.inhabitants.remove(animal);
    }

    /**
     * Getter to get the hole network
     * @return the hole network
     */
    public Set<RabbitHole> getConnectedHoles() {
        return this.connectedHoles;
    }

    /**
     * Getter to get the inhabitants of the hole network
     * @return inhabitants of the hole network
     */
    public Set<Animal> getInhabitants() {
        return this.inhabitants;
    }

    /**
     * Getter of the location of this hole
     * @param world the current world
     * @return the location of the hole
     */
    public Location getLocation(World world) {
        return world.getLocation(this);
    }

    /**
     * Method to destroy the hole.
     * Removes the hole from the network then deletes it form the world
     * @param world the current world
     */
    public void destroyHole(World world) {
        Iterator<RabbitHole> it = this.connectedHoles.iterator();

        while (it.hasNext()) {
            RabbitHole hole = it.next();
            if (hole.equals(this)) {continue;}
            it.remove();
        }
        this.connectedHoles = null;
        this.inhabitants = null;
        world.delete(this);
    }
}
