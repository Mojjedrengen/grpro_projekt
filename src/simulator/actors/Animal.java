package simulator.actors;

import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import simulator.objects.Carcass;
import simulator.objects.plants.Grass;
import simulator.util.PathFinder;

/**
 * Animal is an abstract class that all animals inherit from.
 * It contains some of the default animal behaviour and every method that
 * every animal has in common.
 */
public abstract class Animal implements Actor {

    private int energy;
    final int maxEnergy;
    protected int health;

    protected int age;
    final int maxAge;

    protected boolean hasEatenToday;

    protected Class<?> foodType;
    protected PathFinder pathFinder;

    /**
     * Animal constructor
     *
     * @param startEnergy - how much energy an animal has to start off with. This will also be the max energy for that animal
     * @param maxAge - the oldest an animal can get before dying
     * @param foodType - what type of food does the animal eat
     */
    public Animal(int startEnergy, int maxAge, Class<?> foodType, int initialHealth) {
        this.energy = startEnergy;
        this.health = initialHealth;
        this.maxEnergy = startEnergy;
        this.age = 0;
        this.hasEatenToday = false;
        this.maxAge = maxAge;
        this.foodType = foodType;

        this.pathFinder = new PathFinder(null);
    }

    /**
     * Animal constructor defaults to 100 energy, max age of 10, default health of 50 and grass as its food type
     */
    public Animal() {
        this(100, 10, Grass.class, 50);
    }

    /**
     * Returns the animal's current energy level
     *
     * @return energy level of animal
     */
    public int getEnergy() {
        return energy;
    }

    public int getHealth() {
        return health;
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
            
            Carcass carcass;
            if(this instanceof Rabbit) { //Changes carcass display img depending on rabbit or other
                carcass = new Carcass("carcass-small");
            } else {
                carcass = new Carcass("carcass");
            }
            replaceWithCarcass(carcass, world);
        }
    }

    public void takeDamage(int damage, World world) {
        this.health = health - damage;
        if (health <= 0) {
            this.killAnimal(world);
        }
    }

    public void setHealth(int health, World world) {
        this.health = health;
        if (health <= 0) {
            this.killAnimal(world);
        }
    }

    private void replaceWithCarcass(Carcass carcass, World world) {
        if (world.isOnTile(this)) { //if it isn't in a hole
        Location currentLocation = world.getLocation(this);
        if(world != null) { //If world isnt null
            world.delete(this); //kill the mf
            if(!world.containsNonBlocking(currentLocation) && world.isTileEmpty(currentLocation)) //If there isnt already a nonblocking object and the tile is empty
            {
                world.setTile(currentLocation, carcass); //add a carcass to location
            }
            }
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
    protected void findPathToNearestFood(World world) {
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
