package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import simulator.objects.plants.Bush;
import simulator.objects.plants.Plant;

public class Bear extends Animal implements DynamicDisplayInformationProvider, Predator {

    static final DisplayInformation adultBear = new DisplayInformation(Color.black, "bear");
    static final DisplayInformation babyBear = new DisplayInformation(Color.black, "bear-small");

    private Set<Location> territory;

    public Bear() {
        super(50, 25, Bush.class, 100); // 50 max energy, 25 max age, eats bushes, 100 health
        this.territory = new HashSet<>();
    }

    private void establishTerritory(World world) {
        Location currentLocation = world.getLocation(this);
        if (territory.isEmpty()) {
            this.territory = world.getSurroundingTiles(currentLocation, 3);
        }
    }

    public boolean isInTerritory(Location location) {
        return territory.contains(location);
    }

    private boolean huntImmediate(World world, Set<Location> surroundingLocations) {
        if (this.hasEatenToday) return false; // Only hunt if the bear hasn’t eaten

        for (Location location : surroundingLocations) {
            Object obj = world.getTile(location);
            if (obj instanceof Rabbit || obj instanceof Wolf) {
                System.out.println("Bear attacked prey at " + location);
                this.attack((Animal) obj, world);
                return true; // Prey was attacked
            }
        }
        return false; // No prey found in adjacent tiles
    }

    private boolean eatCarcass(World world, Set<Location> surroundingLocations) {
        if (this.hasEatenToday) return false; // Only eat if the bear hasn’t eaten

        for (Location location : surroundingLocations) {
            if (world.containsNonBlocking(location) &&
                world.getNonBlocking(location) instanceof simulator.objects.Carcass carcass) {
                carcass.consume(world); // Consume the carcass
                System.out.println("Bear ate a carcass!");
                this.ate();
                this.increaseEnergy(30); // Bears get more energy from carcasses
                return true; // Exit as a carcass was eaten
            }
        }
        return false; // No carcass was found
    }

    private boolean eatBerries(World world, Set<Location> surroundingLocations) {
        if (this.hasEatenToday) return false; // Only eat if the bear hasn’t eaten

        for (Location location : surroundingLocations) {
            if (world.containsNonBlocking(location) &&
                world.getNonBlocking(location) instanceof Bush bush &&
                bush.getCurrentStage() == Plant.Stage.RIPE) {
                bush.consume(world);
                System.out.println("Bear ate berries!");
                this.ate();
                this.increaseEnergy(20);
                return true;
            }
        }
        return false; // No berries were eaten
    }

    private void daytimeBehavior(World world) {
        this.establishTerritory(world);

        if (this.hasEatenToday) {
            this.nighttimeBehavior(world);
            return;
        }

        Location currentLocation = world.getLocation(this);
        Set<Location> surroundingLocations = world.getSurroundingTiles(currentLocation);

        // Prioritize eating immediate berries
        if (this.eatBerries(world, surroundingLocations)) {
            return; // Exit if berries were eaten
        }

        // Eat surrounding carcass
        if (this.eatCarcass(world, surroundingLocations)) {
            return; // Exit if a carcass was eaten
        }

        if (this.huntImmediate(world, surroundingLocations)) {
            return; // Exit if prey was attacked
        }

        // If no food was consumed or hunted, pathfind to food
        if (!this.pathFinder.hasPath()) {
            this.pathFinder.setLocation(currentLocation);

            // Attempt to find food
            boolean pathFound = this.pathFinder.findPathToNearest(Bush.class, world);
            if (!pathFound) {
                pathFound = this.pathFinder.findPathToNearestBlocking(Rabbit.class, world);
                if (!pathFound) {
                    this.pathFinder.findPathToNearestBlocking(Wolf.class, world);
                }
            }
        }

        // Follow the path if one exists
        if (this.pathFinder.hasPath()) {
            Location nextStep = this.pathFinder.getPath().poll();
            if (world.isTileEmpty(nextStep)) {
                world.move(this, nextStep);
            } else {
                // Clear the path and wander if blocked
                this.pathFinder.clearPath();
                this.wander(world);
            }
        } else {
            // Wander if no path exists
            this.wander(world);
        }
    }

    private void nighttimeBehavior(World world) {
        Location currentLocation = world.getLocation(this);

        if (territory.isEmpty()) {
            // Reestablish territory if empty
            this.establishTerritory(world);
        }

        if (!isInTerritory(currentLocation)) {
            Location territoryCenter = this.territory.iterator().next();

            this.pathFinder.setLocation(currentLocation);
            if (!this.pathFinder.hasPath() || !this.pathFinder.getFinalLocationInPath().equals(territoryCenter)) {
                this.pathFinder.findPathToLocation(territoryCenter, world);
            }

            if (this.pathFinder.hasPath()) {
                Location nextStep = this.pathFinder.getPath().poll();
                if (world.isTileEmpty(nextStep)) {
                    world.move(this, nextStep);
                }
            }
        } else {
            this.pathFinder.clearPath();
        }
    }

    @Override
    public void act(World world) {
        if (world.isDay()) {
            this.daytimeBehavior(world);
        } else {
            this.nighttimeBehavior(world);
        }

        if (world.getCurrentTime() == 0) {
            this.decreaseEnergy(this.getAge() + (this.hasEatenToday ? 0 : 15), world);
            this.resetHunger();
        }
    }

    @Override
    public DisplayInformation getInformation() {
        if (this.age > 10) return Bear.adultBear;
        return Bear.babyBear;
    }

    @Override
    public void attack(Animal prey, World world) {
        prey.takeDamage(40, world); // Bear does big 40
    }
}
