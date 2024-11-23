package simulator.actors;

import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.Location;
import java.util.Set;
import java.util.ArrayList;
import java.util.Random;

import simulator.objects.NonBlockable;
import simulator.objects.Grass;
import simulator.util.PathFinder;


public abstract class Animal implements Actor {

    private int energy;
    final int maxEnergy;

    protected int age;
    final int maxAge;

    protected boolean hasEatenToday;

    private Class<? extends NonBlockable> foodType;
    protected PathFinder pathFinder;

    /**
     * Animal constructor
     *
     * @param startEnergy - how much energy an animal has to start off with. This will also be the max energy for that animal
     * @param maxAge - the oldest an animal can get before dying
     * @param foodType - what type of food does the animal eat
     */
    public Animal(int startEnergy, int maxAge, Class<? extends NonBlockable> foodType) {
        this.energy = startEnergy;
        this.maxEnergy = startEnergy;
        this.age = 0;
        this.hasEatenToday = false;
        this.maxAge = maxAge;
        this.foodType = foodType;

        this.pathFinder = new PathFinder(null);
    }

    /**
     * Animal constructor defaults to 100 energy, max age of 10 and grass as its food type
     */
    public Animal() {
        this(100, 10, Grass.class);
    }

    /**
     * Returns the animal's current energy level
     *
     * @return energy level of animal
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Increase animals energy.
     * If final amount of energy is higher than the animals max energy, then the animal is simply left with its max energy
     *
     * @param amount - the amount of energy the animal gains
     */
    public void increaseEnergy(int amount) {
        this.energy = Math.min(this.energy + amount, this.maxEnergy);
    }

    /**
     * Decrese energy and age animal.
     * If energy drops to 0 or if the animal's age exceeds its max age, then it dies
     *
     * @param amount - the amount of energy the animal loses
     * @param world - reference to the world
     */
    public void decreaseEnergy(int amount, World world) {
        this.energy -= amount;
        age++;
        if (this.energy <= 0 || age > this.maxAge) {
            killAnimal(world);
        }
    }

    /*
     * Kills the animal and deletes it from the world
     *
     * @param world - reference to the world
     */
    public void killAnimal(World world) {
        if(world != null) {
            //world.remove(this);
            world.delete(this);
            System.out.println("Animal died");
        }
    }

    /**
     * Random wander behaviour.
     * Animal picks an empty sorrounding tile and goes there.
     *
     * @param world - reference to the world
     */
    protected void wander(World world) {
        Location currentLocation = world.getLocation(this);
        Set<Location> emptyTiles = world.getEmptySurroundingTiles(currentLocation);
        if (!emptyTiles.isEmpty()) {
            Location randomLocation = new ArrayList<>(emptyTiles).get(new Random().nextInt(emptyTiles.size()));
            world.move(this, randomLocation);
        }
    }

    /**
     * Animal eating logic. To be overwritten
     *
     * @param world - reference to the world
     */
    public void eat(World world) {

    }

    /**
     * Animal reproduction logic. To be overwritten
     *
     * @param world - reference to the world
     */
    public void reproduce(World world) {

    }

    /**
     * Animal acting logic. To be overwritten
     *
     * @param world - reference to the world
     */
    @Override
    public void act(World world) {
        eat(world);      // Eat to regain energy
        wander(world);   // Move to a random nearby tile
        reproduce(world); // Reproduce if conditions are met
        decreaseEnergy(1, world); // Lose energy per "step"
    }

    /**
     * Resets the animal's has eaten today boolean to false
     */
    protected void resetHunger() {
        this.hasEatenToday = false;
    }

    /**
     * Sets the animal's has eaten today boolean to true
     */
    public void ate() {
        this.hasEatenToday = true;
    }

    /**
     * Returns the animals current age 
     *
     * @return current age of animal
     */
    protected int getAge() {
        return this.age;
    }

    // Currently unsused
    // Actively hunt for food, tries to find nearest food
    protected void activeHunt(World world) {
        Location currentLocation = world.getLocation(this);
        this.pathFinder.setLocation(currentLocation);
        this.pathFinder.findPathToNearest(this.foodType, world);
    }

    /**
     * Gets a reference to the PathFinder that the animal is using
     *
     * @return reference to animal's PathFinder
     */
    public final PathFinder getPathFinder() {
        return this.pathFinder;
    }

}
