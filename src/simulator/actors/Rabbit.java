package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;
import simulator.objects.holes.RabbitHoleNetwork;
import simulator.objects.plants.Grass;
import simulator.objects.NonBlockable;
import simulator.objects.holes.RabbitHole;
import simulator.util.Utilities;

/**
 * This class represents a rabbit, here's what rabbits do from day to night:
 *  Rabbits will at the start off the day actively search for a patch of grass
 * - After eating one patch of grass, they'll randomly wander around
 * - Should rabbit randomly step on tile of grass, then they'll eat it
 * - Once night time arrives rabbits will do the following given it has no assigned rabbit hole:
 *      - Search for rabbit holes
 *      - Attempt to create rabbit hole with 5% success rate
 * - Given that it has a rabbit hole or just created one:
 *      - Enter rabbit hole
 *      - If inside rabbit hole with another rabbit, attempt to reproduce once with a 1/20 chance of success
 *
 * - Once a day ends, rabbits will age and lose energy.
 *   At the age of 3, their image will be swapped out with a large rabbit
 *   How much energy they lose depends on their age, the older, the faster they lose energy.
 *   If a rabbit hasn't eaten at all in a certain day, then they'll lose a large amount of energy.
 *   The oldest a rabbit can get is 9(they'll instantly die the day they hit 10)
 */
public class Rabbit extends Animal implements DynamicDisplayInformationProvider {

    // Images for rabbits. Rather than creating a new instance of DisplayInformation every
    // time getInformation() is called, the method simply returns one of these two.
    static DisplayInformation largeRabbit = new DisplayInformation(Color.red, "rabbit-large");
    static DisplayInformation smallRabbit = new DisplayInformation(Color.red, "rabbit-small");

    // Rabbit only attempts to reproduce once per night, this keeps track of whether it has or hasn't
    private boolean hasAttemptetToReproduce;
    private RabbitHoleNetwork assignedNetwork; // The singleton of the network
    private boolean hasCreatedHole;


    public Rabbit() {
        super(20, 9, Grass.class, 25); // Call the Animal superclass constructor
        this.assignedNetwork = RabbitHoleNetwork.getInstance(); // Gets a variable that is a shortened version of RabbitHoleNetwork.getInstance(). To much to write each time
        // PathFinder expects starting location, setting to null for now
        this.hasAttemptetToReproduce = false;
        this.hasCreatedHole = false;
    }


    // Check if the rabbit has an assigned hole
    @Deprecated
    public boolean hasHole() {
        return assignedNetwork.getEntrances() != null;
    }

    /**
     * Determines if rabbit currently finds itself inside a rabbit hole.
     * This method will also return false if rabbit has no rabbit hole
     *
     * @return Whether if rabbit is currently in hole or not
     */
    public boolean isInHole() {
        return assignedNetwork != null && assignedNetwork.getInhabitants().contains(this);
    }


    @Override
    public void reproduce(World world) {
        this.assignedNetwork.reproduceInhabitant(world);
        hasAttemptetToReproduce = true;
    }

    // Move towards the assigned hole
    public void goHole(World world) {
        //TODO: Change logic to make it so it can randomly crate a new hole if it hasn't done it in its lifetime.
        if (!hasHole() || isInHole()) return; //Returns if it meets any of the two conditions


        Location currentLocation = world.getLocation(this);
        //Location rabbitHoleLocation = assignedHole.getLocation(world);
        Location rabbitHoleLocation = Utilities.getClosestLocationFromSet(this.assignedNetwork.getHoleLocation(world), world.getLocation(this));

        if (currentLocation.equals(rabbitHoleLocation)) { // Rabbit enters hole
            this.assignedNetwork.getHoleFromLocation(world, rabbitHoleLocation).enterRabbit(this, world);
            return;
        }
        //if statement to only go towards hole if it's evening time...
        // Find path to the hole and move one step towards it

        this.pathFinder.setLocation(currentLocation);
        Location nextStep = null;
        if(pathFinder.hasPath() &&
                pathFinder.isFinalLocationInPath(rabbitHoleLocation) ) {

            nextStep = (this.pathFinder.getPath()).poll(); // Move closer to goal
        }else { // Path hasn't been found yet

            // findPathToLocation returns false if no route was found
            if(!this.pathFinder.findPathToLocation(rabbitHoleLocation, world)) {
                // No route to hole
                return;
            }
            nextStep = this.pathFinder.getPath().poll(); // Move closer to goal
        }

        if(world.isTileEmpty(nextStep)) {
            world.move(this, nextStep); // Move rabbit to the next step
        } else {
            //Path is blocked
            // pathFinder obstruction fixer method is underway!
            // pathFinder.fixObstructedPath();
        }
    }

    /**
     * Rabbit exists hole
     */
    private void exitHole(World world) {
        if (this.assignedNetwork != null) {
            Set<RabbitHole> holes = new HashSet<>();
            for (RabbitHole hole : this.assignedNetwork.getEntrances()) {
                if (!hole.predatorNearby(world)) holes.add(hole);
            }

            RabbitHole exitHole;
            if (holes.isEmpty()) {
                // No safe entrances, pick something and hope for the best
                exitHole = Utilities.getRandomFromSet(this.assignedNetwork.getEntrances());
            }else {
                exitHole = Utilities.getRandomFromSet(holes);
            }
            exitHole.exitRabbit(this, world);
        }
    }


    /**
     * Rabbit searches for nearby rabbit holes with a search radius of 3
     * If it finds one, it assigns itself to the hole and goes to it
     */
    private boolean noNearbyHoles(World world) {
        Set<Location> search = world.getSurroundingTiles(2);
        for (Location location : search) {
            if (world.containsNonBlocking(location) && world.getNonBlocking(location) instanceof RabbitHole) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rabbit day behaviour.
     */
    private void actDuringDay(World world) {
        // Exit hole if in one(since it's day time)
        if (isInHole()) {
            exitHole(world);
        } else if (this.pathFinder.hasPath()) {
            // If it has a path in the path finder, e.g.
            // path to nearest patch of grass, then follow that.
            Location nextStep = this.pathFinder.getPath().poll();
            if (world.isTileEmpty(nextStep)) world.move(this, nextStep);
        } else {
            // Otherwise, wander randomly
            this.wander(world);
        }
    }


    /**
     * 5% chance for rabbit to create a hole on the tile it's currently standing on.
     * If current tile already contains nonblocking, then this method simply returns
     */
    private void tryToMakeHole(@NotNull World world) {

        Location currentLocation = world.getLocation(this);
        if(world.containsNonBlocking(currentLocation)) return;

        Random random = new Random();
        if(random.nextInt(101) <= 95) return;

        // TODO find a way to add this to our WorldLoader list
        this.assignedNetwork.createHole(world, world.getLocation(this));
    }

    @Override
    public void act(World world) {
        if (world.isNight()) {
            // Nighttime behavior
            if (this.hasCreatedHole) {
                this.goHole(world); // Move towards the assigned hole
            } else {
                if (this.noNearbyHoles(world) && !this.hasCreatedHole) {
                    this.tryToMakeHole(world); // Try to make a new hole
                    if (this.noNearbyHoles(world) && !this.hasCreatedHole) this.wander(world); // Wander if no hole found
                }
            }

            // Reproduce if in a hole and hasn't attempted yet
            if (this.isInHole() && !this.hasAttemptetToReproduce) {
                this.reproduce(world);
            }
        } else {
            // Daytime behavior
            this.actDuringDay(world); // Simplified daytime logic
        }
        if( !this.isInHole() ) {
            this.eat(world); // Try to eat

            // Bad practice by using instanceof, we have disappointed Claus, but this will have to do for now
            // Potential fix would be keeping a separate list containing all rabbit holes in the world
//            Location currentLocation = world.getLocation(this);
//            if(world.containsNonBlocking(currentLocation)) {
//                NonBlockable nonBlock = (NonBlockable)world.getNonBlocking(world.getLocation(this));
//                if(nonBlock instanceof RabbitHole rabbitHole) {
//                    this.setAssignedNetwork(rabbitHole.getNetwork());
//                }
//            }
            //TODO: I see no point in above code. Please review and remove
        }

        // End of day logic:aging, energy loss, and reset
        // Lose amount of energy corresponding to the rabbits age
        // As the rabbit ages, it loses energy faster
        // Lose 10 extra if the rabbit hasn't eaten at all today
        if(world.getCurrentTime() == 0) {
            // Decrease energy also causes aging
            this.decreaseEnergy(this.getAge() + (this.hasEatenToday ? 0 : 10), world);
            this.resetHunger();
            //System.out.println("Energy levels end of day: " + this.getEnergy());
            hasAttemptetToReproduce = false;
        }
    }



    // TODO
    // Move to animal abstract class since every animal has the small behaviour in this regard.
    // The only different thing is the type of food, which can be specificed with Class<T> type.
    /**
     * If animal hasn't eaten at all today, then it finds the path to the nearest food source.
     * If it's standing on top of food, then it'll eat it.
     *
     * @param world - reference to the world
     */
    @Override
    public void eat(World world) {

        Location currentLocation = world.getLocation(this);

        // Has not eaten today, actively search for food
        if(!this.hasEatenToday) {
            // Check if rabbit already has path to food or/and if final destination in path still has food.
            if(!this.pathFinder.hasPath() ||
                    !Utilities.locationContainsNonBlockingType(world, this.pathFinder.getFinalLocationInPath(), this.foodType)) {
                // Go to nearest food. Inherited from Animal
                this.findPathToNearestFood(world);
            }
        }

        // Always eat food if standing on top of food.
        if(world.containsNonBlocking(currentLocation)) {
            NonBlockable nonBlockable = (NonBlockable)world.getNonBlocking(currentLocation);
            if(nonBlockable instanceof Grass grass) {
                if(this.hasEatenToday && this.getEnergy() < this.maxEnergy) return;
                grass.consume(world);
                this.hasEatenToday = true;
                this.increaseEnergy(1);

                // There's a chance that while an animal is walking towards grass, the grass will grow towards the rabbit,
                // causing it to eat twice and throwing the pathFinding off balance. We'll simply clear the pathFinder in this
                // case to prevent any issues.
                this.pathFinder.clearPath();
            }
        }
    }

    @Override
    public DisplayInformation getInformation() {
        if (age > 2) return Rabbit.largeRabbit;
        return Rabbit.smallRabbit;
    }

}

