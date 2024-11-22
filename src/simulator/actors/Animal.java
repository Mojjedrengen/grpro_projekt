package simulator.actors;

import itumulator.simulator.Actor;
import itumulator.world.World;
import itumulator.world.Location;
import java.util.Set;
import java.util.ArrayList;
import java.util.Random;

import simulator.objects.NonBlockable;
import simulator.util.PathFinder;

public abstract class Animal implements Actor {

    private int energy;
    final int maxEnergy;

    final int maxAge;
    private int age;

    protected boolean hasEatenToday;

    private Class<? extends NonBlockable> foodType;
    protected PathFinder pathFinder;

    public Animal(int startEnergy, int maxAge, Class<? extends NonBlockable> foodType) {
        this.energy = startEnergy;
        this.maxEnergy = startEnergy;
        this.age = 0;
        this.hasEatenToday = false;
        this.maxAge = maxAge;
        this.foodType = foodType;

        this.pathFinder = new PathFinder(null);
    }

    // NOTE: Current implementation of WorldLoader cannot handle constructor arguments.
    // Will be fixed soon. For the meanwhile, give default energy levels to all animals
    // and have a constructor with no arguments
    public Animal() {
        this.energy = 100;
        this.age = 0;
        this.maxEnergy = this.energy;
        this.maxAge = 10;
    }

    // Getter
    public int getEnergy() {
        return energy;
    }

    // Increase energy
    public void increaseEnergy(int amount) {
        this.energy = Math.min(this.energy + amount, this.maxEnergy);
    }

    // Decreases energy
    public void decreaseEnergy(int amount, World world) {
        this.energy -= amount;
        age++;
        if (this.energy <= 0) {
            killAnimal(world);
        }
    }

    public void killAnimal(World world) {
        if(world != null) {
            //world.remove(this);
            world.delete(this);
            System.out.println("Animal died");
        }
    }

    // Wander behaviour
    protected void wander(World world) {
        Location currentLocation = world.getLocation(this);
        Set<Location> emptyTiles = world.getEmptySurroundingTiles(currentLocation);
        if (!emptyTiles.isEmpty()) {
            Location randomLocation = new ArrayList<>(emptyTiles).get(new Random().nextInt(emptyTiles.size()));
            world.move(this, randomLocation);
        }
    }

    // Eating behavior, to be overwritten
    public void eat(World world) {

    }

    // Reproduction behavior, to be overwritten
    public void reproduce(World world) {

    }

    //to be overwritten
    public void act(World world) {
        eat(world);      // Eat to regain energy
        wander(world);   // Move to a random nearby tile
        reproduce(world); // Reproduce if conditions are met
        decreaseEnergy(1, world); // Lose energy per "step"
    }

    protected void resetHunger() {
        this.hasEatenToday = false;
    }

    public void ate() {
        this.hasEatenToday = true;
    }

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

    public final PathFinder getPathFinder() {
        return this.pathFinder;
    }

}
