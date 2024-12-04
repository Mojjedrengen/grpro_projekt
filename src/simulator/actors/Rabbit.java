package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java.awt.Color;

//import org.jetbrains.annotations.NotNull;
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
    private boolean hasAttemptedToReproduce;
    private RabbitHoleNetwork assignedNetwork; // The singleton of the network
    private boolean hasCreatedHole;
    private boolean hasAttemptedToCreateHole; //boolean to make sure the rabbit don't spend all night trying to make a hole


    public Rabbit() {
        super(20, 9, Grass.class, 25); // Call the Animal superclass constructor
        this.assignedNetwork = RabbitHoleNetwork.getInstance(); // Gets a variable that is a shortened version of RabbitHoleNetwork.getInstance(). To much to write each time
        // PathFinder expects starting location, setting to null for now
        this.hasAttemptedToReproduce = false;
        this.hasCreatedHole = false;
        this.hasAttemptedToCreateHole = false;

    }


    // Check if the rabbit has an assigned hole
    @Deprecated
    public boolean hasHole() {
        return assignedNetwork.getEntrances() != null && assignedNetwork.getEntrances().size() > 0;
    }

    /**
     * Determines if rabbit currently finds itself inside a rabbit hole.
     * This method will also return false if rabbit has no rabbit hole
     *
     * @return Whether if rabbit is currently in hole or not
     */
    public boolean isInHole() {
        return this.assignedNetwork.getInhabitants().contains(this);
    }

    public void assignNetwork(RabbitHoleNetwork network) {
        this.assignedNetwork = network;
    }


    @Override
    public void reproduce(World world) {
        this.assignedNetwork.reproduceInhabitant(world);
        hasAttemptedToReproduce = true;
        System.out.println(this + " reproduced");
    }

    // Move towards the assigned hole
    public void goHole(World world) {
        if (!hasHole() || isInHole()) return; //Returns if it meets any of the two conditions

        Location currentLocation = world.getLocation(this);
        this.pathFinder.setLocation(currentLocation);

        if(!this.pathFinder.hasPath()
        || Utilities.locationContainsNonBlockingType(world, this.pathFinder.getFinalLocationInPath(), RabbitHole.class)) {
            this.pathFinder.findPathToNearest(RabbitHole.class, world);
        }

        //Location rabbitHoleLocation = assignedHole.getLocation(world);
        // Avoid having to iterate through every single rabbit for every call to goHole()
        //Location rabbitHoleLocation = Utilities.getClosestLocationFromSet(this.assignedNetwork.getHoleLocation(world), world.getLocation(this));

        if ( Utilities.locationContainsNonBlockingType(world, currentLocation, RabbitHole.class)  ) { // Rabbit enters hole

            ((RabbitHole)world.getNonBlocking(currentLocation)).enterRabbit(this, world);

            // Weird bug causing test seeksRabbitHoleAtNightTest() to fail.
            // The hole.GetLocation() method cannot find the hole on the map despite being on the map.
            // Temporary fix is the fact that we already now current location is hole location
            //this.assignedNetwork.getHoleFromLocation(world, currentLocation).enterRabbit(this, world);
            return;
        }
        //if statement to only go towards hole if it's evening time...
        // Find path to the hole and move one step towards it

        if(this.pathFinder.hasPath()) {
            Location nextStep = this.pathFinder.getPath().poll();
            if(world.isTileEmpty(nextStep)) {
                world.move(this, nextStep); // Move rabbit to the next step
            }
        }
    }

    /**
     * Rabbit exists hole
     */
    private void exitHole(World world) {
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
        // for some reason exitHole is null therefor get the exit hole another way that use Utilities
        if (exitHole == null) {
            if (holes.isEmpty()) {
                exitHole = this.assignedNetwork.getEntrances().iterator().next();
            } else {
                exitHole = holes.iterator().next();
            }
        }
        exitHole.exitRabbit(this, world);
    }


    /**
     * Rabbit searches for nearby rabbit holes with a search radius of 3
     * If it finds one, it assigns itself to the hole and goes to it
     */
    private boolean noNearbyHoles(World world) {
        Set<Location> search = world.getSurroundingTiles(2);
        search.add(world.getCurrentLocation());
        for (Location location : search) {
            if (world.containsNonBlocking(location) && world.getNonBlocking(location) instanceof RabbitHole) {
                return false;
            }
        }
        return true;
    }

    private void nightTimeBehaviour(World world) {
        // Nighttime behavior
        if (this.hasCreatedHole || !this.noNearbyHoles(world)) {
            this.goHole(world); // Move towards the assigned hole
        } // Reproduce if in a hole and hasn't attempted yet
        else if (this.isInHole()) {
            if(!this.hasAttemptedToReproduce)
            this.reproduce(world);
        }else if(!this.hasAttemptedToCreateHole) {
            this.tryToMakeHole(world); // Try to make a new hole
            this.hasAttemptedToCreateHole = true;
            if(!this.hasCreatedHole) this.wander(world); // Wander if no hole found
            else this.goHole(world);
        }else{
            this.wander(world);
        } 
    }

    /**
     * Rabbit day behaviour.
     */
    private void dayTimeBehaviour(World world) {
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
    private void tryToMakeHole(World world) {

        Location currentLocation = world.getLocation(this);
        if(world.containsNonBlocking(currentLocation)) return;

        Random random = new Random();
        if(random.nextInt(101) <= 95) return;

        this.hasCreatedHole = true;

        // TODO find a way to add this to our WorldLoader list
        this.assignedNetwork.createHole(world, currentLocation);
    }

    @Override
    public void act(World world) {
        if (world.isNight()) {
            this.nightTimeBehaviour(world);
        } else {
            // Daytime behavior
            this.dayTimeBehaviour(world); // Simplified daytime logic
            if (!this.hasCreatedHole) this.hasAttemptedToCreateHole = false;
        }
        if( !this.isInHole() ) {
            this.eat(world); // Try to eat

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
            this.hasAttemptedToReproduce = false;
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
                if(this.hasEatenToday && this.getEnergy() == this.maxEnergy) return;
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

