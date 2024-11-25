package simulator.objects.holes;

import java.util.Set;
import java.util.HashSet;

import itumulator.world.Location;
import itumulator.world.World;

import simulator.objects.NonBlockable;
import simulator.actors.Animal;

public abstract class Hole extends NonBlockable {

    protected Set<Animal> inhabitants;

    public Hole() {
        this.inhabitants = new HashSet<>();
    }


    /**
     * Method to add an Animal to the inhabitants set.
     * SHOULD ONLY BE USED BY OTHER ANIMAL HOLES USE {@link #animalEnters(Animal animal)} INSTEAD.
     * @param animal animal to add
     */
    protected void animalAdd(Animal animal) {
        this.inhabitants.add(animal);
    }

    /**
     * Method to remove an Animal from the inhabitants set
     * SHOULD ONLY BE USED BY OTHER ANIMAL HOLES USE {@link #animalLeave(Animal)} INSTEAD.
     * @param animal animal to remove
     */
    protected void animalRemove(Animal animal) {
        this.inhabitants.remove(animal);
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
     * Deletes this hole from the world 
     *
     * @param world reference to the world
     */
    public void destroyHole(World world) {
        this.inhabitants = null;
        world.delete(this);
    }


}

