package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;
import java.awt.Color;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import simulator.objects.Carcass;
import simulator.objects.holes.WolfHole;
import simulator.util.Utilities;

/** The Wolf class represents a wolf. They have 50 max energy, live until 12,
 * eat carcasses, hunt rabbits and have 50 health points
 *
 * During the day wolves will look for Carcass to eat, if none is found then it
 * will hunt down a rabbit and eat its carcass.
 *
 * Once a wolf has eaten, it will look for a member of its wolfpack and follow
 * them until night. Should a wolf from another wolf pack approach the wolf,
 * then the wolf will attack.
 *
 * Once night falls, the wolf will return to the wolf hole that is owned by the
 * wolf pack.
 *
 * If a wolf does not have a wolfpack and can't find one, then after 2 days it
 * will attempt to create one with a 1/10 chance of success.
 */
public class Wolf extends Animal implements DynamicDisplayInformationProvider,
Predator {

    // Image for adult wolf
    static final DisplayInformation adultWolf = new DisplayInformation(Color.black, "wolf");
    // Image for baby wolf
    static final DisplayInformation babyWolf = new DisplayInformation(Color.black, "wolf-small");

    // The wolfpack the wolf is currently in
    WolfPack wolfPack;
    // Variable that tracks the amount of time a wolf has spent without being apart of a wolf pack
    private int daysWithoutWolfPack;

    /** Wolf constructor. Creates wolf with 50 energy, 12 max age, hunts rabbits and 50 health points.
     */
    public Wolf() {
        // 50 max energy, lives until age of 12, eats rabbits, 50 health
        super(50, 12, Rabbit.class, 50);
        wolfPack = null;
        this.daysWithoutWolfPack = 0;
    }

    /**
     * If wolf isn't in a wolfpack, then join the specified wolf pack
     *
     * @param wolfPack the wolf pack to join
     */
    public void joinWolfPack(WolfPack wolfPack) {
        // Already in wolfpack
        if(this.wolfPack != null) return;

        this.wolfPack = wolfPack;
        wolfPack.addMember(this);
    }

    protected boolean isEnemyWolf(Object obj) { //To simplify other code
        return obj instanceof Wolf wolf && wolf.wolfPack != this.wolfPack;
    }

    protected boolean isFriendlyWolf(Object obj) { //To simplify other code
        return obj instanceof Wolf wolf && wolf.wolfPack == this.wolfPack;
    }

    /**
     * Get the wolfpack of this wolf
     *
     * @return reference to wolfpack
     */
    public WolfPack getWolfPack() {
        return this.wolfPack;
    }

    /**
     * Searches for wolfpack within a radius of three.
     * If a wolf with a wolfpack is found witihn this radius, then this wolf joins that wolfpack
     *
     * If wolf hasn't found wolfpack after 2 days, then there's a 1/10 chance it will create one now
     *
     * @param world reference to the world
     */
    protected void searchForWolfPack(World world) {
        Set<Location> locations = world.getSurroundingTiles(3);
        final Location currentLocation = world.getLocation(this);
        locations.remove(currentLocation);

        final Set<Wolf> nearbyWolves = world.getAll(Wolf.class, locations);
        for(final Wolf nearbyWolf : nearbyWolves) {
            if(nearbyWolf.hasWolfPack()) {
                this.joinWolfPack(nearbyWolf.wolfPack);
                return;
            }
        }

        // If wolf hasn't found WolfPack after 2 days, then try to make one with 1/10 success rate
        if(this.wolfPack == null && this.daysWithoutWolfPack > 2 && (new Random()).nextInt(10) == 0) {
            if(world.containsNonBlocking(currentLocation)) return;

            WolfHole wolfHole = new WolfHole();
            world.setTile(currentLocation, wolfHole);
            this.wolfPack = new WolfPack(wolfHole);
            this.wolfPack.addMember(this);
        }
    }

    /**
     * Checks if wolf has wolfpack
     *
     * @return whether if wolf currently is a member of a wolfpack
     */
    protected boolean hasWolfPack() {
        return this.wolfPack != null;
    }

    /**
     * Makes wolf go to its wolfpack hole if it is a member of a wolfpack
     */
    protected void goToHole(World world) {
        if(!this.hasWolfPack() || this.isInHole()) return;

        {
            final Location holeLocation = this.wolfPack.getHole().getLocation(world);
            final Location wolfLocation = world.getLocation(this);
            if(holeLocation.equals(wolfLocation)) {
                this.wolfPack.getHole().wolfEnter(this, world);
                return;
            }

            if(!this.pathFinder.hasPath() || !(this.pathFinder.getFinalLocationInPath().equals(holeLocation))) {
                this.pathFinder.setLocation(wolfLocation);
                this.pathFinder.findPathToLocation(holeLocation, world);
            }
        }

        if(this.pathFinder.hasPath()) {
            final Location nextStep = this.pathFinder.getPath().poll();
            if(world.isTileEmpty(nextStep)) world.move(this, nextStep);
        }

    }

    /**
     * Method used to attack an animal if it is within range(on a surrounding tile).
     *
     * @param world reference to the world
     * @param surroundingLocations set to the surrouding tiles which are the effective range of the wolf
     * @param attackCondition lambda function that takes an object and returns a boolean determining if the wolf should attack that animal.
     */
    protected void attackIfInRange(World world, Set<Location> surroundingLocations, Function<Object, Boolean> attackCondition ) {
        surroundingLocations.forEach( l -> {
            final Object obj = world.getTile(l);
            if(attackCondition.apply(obj)) {
                // lambda function tells us this is valid Animal to typecast
                this.attack((Animal)obj, world);
                // Return to avoid attacking multiple animals in one tick
                return;
            }
        });
    }

    /**
     * Method used to make wolf eat a carcass than is on a surrounding tile
     *
     * @param world reference to the world
     * @param surroundingLocations set to the surrounding tiles
     */
    protected void eatCarcassIfInRange(World world, Set<Location> surroundingLocations) {
        surroundingLocations.forEach( l -> {
            if(world.containsNonBlocking(l) && world.getNonBlocking(l) instanceof Carcass carcass 
            // This logic introduces a bug which prevents the wolf from eating on its very first day
            // it starts off with max energy, but since hasEatenToday starts as false, it will starve itself.
            // On a side note: Remember to run unit tests after introducing logic changes! wolfEatsCarcassTest() fails after adding this
            /* BUG: && this.getEnergy() != this.maxEnergy */) {
                carcass.consume(world);
                this.ate();
                this.increaseEnergy(30);
                // Only eat once per time step 
                return;
            }
        });

    }

    /**
     * Method to make wolf perform its day time behaviour
     *
     * @param world reference to the world
     */
    protected void dayTimeBehaviour(World world) {
        if(this.isInHole()) {
            this.wolfPack.getHole().wolfExit(this, world);
        }else if(!this.hasWolfPack()) { // Can't have hole and not have wolf pack at same time
            this.searchForWolfPack(world);
        }

        final Location currentLocation = world.getLocation(this);
        this.pathFinder.setLocation(currentLocation);

        // Try to generate path to Carcass, if none was found then try the same for Rabbit instead
        Runnable generatePathToFood = () -> {
            if(!this.pathFinder.findPathToNearest(Carcass.class, world))
            this.pathFinder.findPathToNearestBlocking(this.foodType, world);
        };

        // If hasn't eaten today and current path doesn't lead to rabbit,
        // then generate a path to nearest rabbit
        if(!this.hasEatenToday) {
            if(!this.pathFinder.hasPath()){
                // If can't find Carcass, then look for Rabbit
                generatePathToFood.run();
            }else {
                final Location destinatinInPath = this.pathFinder.getFinalLocationInPath();

                // If path doesn't lead to Rabbit or Carcass, then generate new path
                if(!(world.getTile(destinatinInPath) instanceof Rabbit) 
                   && !( Utilities.locationContainsNonBlockingType(world, destinatinInPath, Carcass.class) ))
                    generatePathToFood.run();
            }

        }


        final Set<Location> surroundingLocations = world.getSurroundingTiles(currentLocation);
        // Only eat if energy isn't max or hasn't eaten today
        if(this.getEnergy() != this.maxEnergy || !this.hasEatenToday){
            // If Rabbit in attack range, then attack it
            this.attackIfInRange(world, surroundingLocations, 
                (obj) -> {
                    return /* Attack if */ obj instanceof Rabbit;
            });

            // Carcass is NonBlockable, so Wolf should also be allowed to stand on top
            surroundingLocations.add(currentLocation);
            this.eatCarcassIfInRange(world, surroundingLocations);
        }else {
            // Attack any wolf that isn't in this wolf's wolfpack and wanders nearby
            this.attackIfInRange(world, surroundingLocations, this::isEnemyWolf);
                        
        }

            // If wolf has no path, then seek nearest wolfpack member
        if(!this.pathFinder.hasPath() && this.hasWolfPack() )  {
            this.pathFinder.findPath(
            (location) -> {
                    for(Location t : world.getSurroundingTiles(location)) {
                        if(this.isFriendlyWolf(world.getTile(t)) && !t.equals(currentLocation) ) return true;
                    }
                    return false;

                }, world
            );

            if(this.pathFinder.getPath().size() < 2) this.pathFinder.clearPath();
        }

        

        if(this.pathFinder.hasPath()) {
            final Location nextStep = this.pathFinder.getPath().poll();
            if(world.isTileEmpty(nextStep)) world.move(this, nextStep);
        }else {
            this.wander(world);
        }
    }

    /**
     * Method to make wolf perform its night time behaviour
     *
     * @param world reference to the world
     */
    protected void nightTimeBehaviour(World world) {
        if(!this.isInHole()) {
            if(this.hasWolfPack()) {
                this.goToHole(world);
            }else{
                this.wander(world);
                this.searchForWolfPack(world);
            }
        }else {
            this.wolfPack.getHole().reproduce(world);
        }
    }

    /**
     * Determines if wolf is in its wolf pack wolf hole. Always returns false if wolf has no wolfpack.
     *
     * @return whether if wolf is currently inside hole. 
     */
    public boolean isInHole() {
        if(this.wolfPack == null) return false;
        return this.wolfPack.assignedHole.getInhabitants().contains(this);
    }

    /**
     * Wolfs act method to make it perform its routine for a single simulation step
     *
     * @param world reference to the world
     */
    @Override
    public void act(World world) {
        if(world.isDay()) {
            this.dayTimeBehaviour(world);
        }else {
            this.nightTimeBehaviour(world);
        }

        if(world.getCurrentTime() == 0) {
            this.decreaseEnergy(this.getAge() + (this.hasEatenToday ? 0 : 15), world);
            this.resetHunger();
            if(this.wolfPack == null) this.daysWithoutWolfPack++;
        }
    }

    /**
     * Returns the DisplayInformation for the wolf. Returns image of adult wolf if the wolf is older than 3.
     *
     * @return wolf display information image
     */
    @Override
    public DisplayInformation getInformation() {
        if(this.age > 3) return Wolf.adultWolf;
        return Wolf.babyWolf;
    }

    /**
     * Override of the super class killAnimal method. This kills the wolf.
     * This method removes the wolf from its wolfpack before calling the super class killAnimal method which then kills the wolf.
     *
     * @param world reference to the world
     */
    @Override
    public void killAnimal(World world) {
        if(this.wolfPack != null) this.wolfPack.removeMember(this);
        super.killAnimal(world);
    }


    /**
     * Predator interface attack implementation.
     * Given an animal, the wolf will perform 30 points of damage to it.
     *
     * @param prey the animal the wolf attacks
     * @param world reference to the world
     */
    @Override
    public void attack(Animal prey, World world) {
        prey.takeDamage(30, world);
    }

}

