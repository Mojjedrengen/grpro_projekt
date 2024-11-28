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
        // 50 max energy, lives for 25 years, gets 100 health
        // Bears eat mostly berries from bushes, but can also hunt so we need to take that into accout
        super(50, 25, Bush.class, 100);
        this.territory = new HashSet<>();
    }
    
    /** Establish a territory in a 3 tile radius around current location
     * @param world reference to the world
     */
    private void establishTerritory(World world) {
        Location currentLocation = world.getLocation(this);
        if (territory.isEmpty()) { //Make check to see if other territory is there already
            this.territory = world.getSurroundingTiles(currentLocation, 3);
        }
    }
    //Check if given location is within bears territory
    private boolean isInTerritory(Location location) {
        return territory.contains(location);
    }

    private void attackIfInRange(World world, Set<Location> surroundingLocations, Function<Object, Boolean> attackCondition) { //Wolf and bear share this, move to predator perhaps
    for (Location location : surroundingLocations) {
        Object obj = world.getTile(location);
        if (attackCondition.apply(obj) && obj instanceof Animal animal) {
            this.attack(animal, world);
            break; 
        }
    }
}

    private void hunt(World world, Set<Location> surroundingLocations) {
        this.attackIfInRange(world, surroundingLocations, obj -> 
        obj instanceof Rabbit || obj instanceof Wolf
    );
    }

    private void eatBerries(World world, Set<Location> surroundingLocations) {
        surroundingLocations.forEach(location -> {
            // Check for a bush with berries
            if (world.containsNonBlocking(location) &&
                world.getNonBlocking(location) instanceof Bush bush &&
                bush.getCurrentStage() == Bush.Stage.RIPE) {
                // Consume berries
                bush.consume(world);
                this.ate();
                this.increaseEnergy(20);
            }
        });
    }

    private void daytimeBehavior(World world) {
        // Establish territory if not already done
        this.establishTerritory(world);

        Location currentLocation = world.getLocation(this);
        Set<Location> surroundingLocations = world.getSurroundingTiles(currentLocation);

        // Attempt to eat berries first
        this.eatBerries(world, surroundingLocations);

        // If no berries or not at max energy, hunt for food
        if (this.getEnergy() < this.maxEnergy) {
            this.hunt(world, surroundingLocations);
        }

        // If no path exists or the path destination is outside the territory, generate a new path
        if (!this.pathFinder.hasPath() || !this.isInTerritory(this.pathFinder.getFinalLocationInPath())) {
            this.pathFinder.setLocation(currentLocation);
        
            // Find a path to berries, rabbits, or wolves
            boolean pathFound = this.pathFinder.findPathToNearest(Bush.class, world);
            if (!pathFound) {
                pathFound = this.pathFinder.findPathToNearestBlocking(Rabbit.class, world);
                if (!pathFound) {
                    pathFound = this.pathFinder.findPathToNearestBlocking(Wolf.class, world);
                }
            }
        
            // If no path was found, wander
            if (!pathFound) {
                this.wander(world);
            }
        }
        
    }

    /**
     * Bear's nighttime behavior: returns to territory and rests.
     * @param world Reference to the world
     */
    private void nighttimeBehavior(World world) {
        if (!isInTerritory(world.getLocation(this))) {
            this.pathFinder.setLocation(world.getLocation(this));
            Location territoryCenter = this.territory.iterator().next(); // Arbitrary point in the territory
            this.pathFinder.findPathToLocation(territoryCenter, world);
        } else {
            // Clear path at night to reset behavior for the next day
            this.pathFinder.clearPath();
        }
    }
    

    /**
     * Bear's `act` method to define its behavior per simulation step.
     * @param world Reference to the world
     */
    @Override
    public void act(World world) {
        if (world.isDay()) {
            this.daytimeBehavior(world);
        } else {
            this.nighttimeBehavior(world);
        }

        // End-of-day logic
        if (world.getCurrentTime() == 0) {
            this.decreaseEnergy(this.getAge() + (this.hasEatenToday ? 0 : 15), world);
            this.resetHunger();
        }
    }

    

    @Override
    public DisplayInformation getInformation() {
        if(this.age > 10) return Bear.adultBear;
        return Bear.babyBear;
    }

    @Override
    public void attack(Animal prey, World world) {
        prey.takeDamage(40, world);
    }

}

