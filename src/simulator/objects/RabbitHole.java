package simulator.objects;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Animal;

import java.util.HashSet;
import java.util.Set;

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

    @Override
    public void act(World world) {

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
        for (RabbitHole hole : this.connectedHoles) {
            if (hole.equals(this)) {continue;}
            hole.disconnectHole(this);
        }
        this.connectedHoles = null;
        this.inhabitants = null;
        world.delete(this);
    }
}
