package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import simulator.objects.plants.Bush;

public class Bear extends Animal implements DynamicDisplayInformationProvider, Predator {

    static final DisplayInformation adultBear = new DisplayInformation(Color.black, "bear");
    static final DisplayInformation babyBear = new DisplayInformation(Color.black, "bear-small");

    private Set<Location> territory;

    public Bear() {
        super(50, 25, Bush.class, 100); // 50 max energy, 25 max age, eats bushes, 100 health
        this.territory = new HashSet<>();
    }

    private void establishTerritory(World world) { //Establishes a territory if no territory exists
        Location currentLocation = world.getLocation(this);
        if (territory.isEmpty()) {
            this.territory = world.getSurroundingTiles(currentLocation, 3);
        }
    }

    private boolean isInTerritory(Location location) { //Returns if location is in territory
        return territory.contains(location);
    }

    private void attackIfInRange(World world, Set<Location> surroundingLocations, Function<Object, Boolean> attackCondition) { //Attacks animals in range
        for (Location location : surroundingLocations) {
            Object obj = world.getTile(location);
            if (attackCondition.apply(obj) && obj instanceof Animal animal) {
                this.attack(animal, world);
                break;
            }
        }
    }

    private void hunt(World world, Set<Location> surroundingLocations) { //Calls attackIfInRange
        this.attackIfInRange(world, surroundingLocations, obj -> obj instanceof Rabbit || obj instanceof Wolf);
    }

    private void eatBerries(World world, Set<Location> surroundingLocations) { //Eats berries if theyre in surroundinglocation set aka territory
        for (Location location : surroundingLocations) {
            if (world.containsNonBlocking(location) &&
                world.getNonBlocking(location) instanceof Bush bush &&
                bush.getCurrentStage() == Bush.Stage.RIPE && this.getEnergy() != this.maxEnergy) {
                bush.consume(world);
                this.ate();
                this.increaseEnergy(20);
                return;
            }
        }
    }

    private void daytimeBehavior(World world) { //Daytime behavior
        this.establishTerritory(world);

        Location currentLocation = world.getLocation(this);
        Set<Location> surroundingLocations = world.getSurroundingTiles(currentLocation);

        this.eatBerries(world, surroundingLocations);

        if (this.getEnergy() < this.maxEnergy) {
            this.hunt(world, surroundingLocations);
        }

        if (!this.pathFinder.hasPath() || !this.isInTerritory(this.pathFinder.getFinalLocationInPath())) {
            this.pathFinder.setLocation(currentLocation); //TODO Idk if this is correct or not, i just looked at the methods for pathfinder and hoped for the best, might need a redo

            boolean pathFound = this.pathFinder.findPathToNearest(Bush.class, world);
            if (!pathFound) {
                pathFound = this.pathFinder.findPathToNearestBlocking(Rabbit.class, world);
                if (!pathFound) {
                    this.pathFinder.findPathToNearestBlocking(Wolf.class, world);
                }
            }

            if (!pathFound) {
                this.wander(world);
            }
        }

        if (this.pathFinder.hasPath()) {
            Location nextStep = this.pathFinder.getPath().poll();
            if (world.isTileEmpty(nextStep)) {
                world.move(this, nextStep);
            }
        } else {
            this.wander(world);
        }
    }

    private void nighttimeBehavior(World world) {
        Location currentLocation = world.getLocation(this);

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
        prey.takeDamage(40, world); //Bear does big 40
    }
}
