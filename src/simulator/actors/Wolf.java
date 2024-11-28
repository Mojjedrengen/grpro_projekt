package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;

import java.awt.Color;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Consumer;

import simulator.util.Utilities;
import simulator.objects.Carcass;

public class Wolf extends Animal implements DynamicDisplayInformationProvider, Predator {

    static final DisplayInformation adultWolf = new DisplayInformation(Color.black, "wolf");
    static final DisplayInformation babyWolf = new DisplayInformation(Color.black, "wolf-small");

    WolfPack wolfPack;

    public Wolf() {
        // 50 max energy, lives until age of 12, eats rabbits, 50 health
        super(50, 12, Rabbit.class, 50);
        wolfPack = null;
    }

    public void joinWolfPack(WolfPack wolfPack) {
        // Already in wolfpack
        if(this.wolfPack != null) return;

        this.wolfPack = wolfPack;
        wolfPack.addMember(this);
    }

    public WolfPack getWolfPack() {
        return this.wolfPack;
    }

    protected void searchForWolfPack(World world) {
        Set<Location> locations = world.getSurroundingTiles(3);
        locations.remove(world.getLocation(this));

        final Set<Wolf> nearbyWolves = world.getAll(Wolf.class, locations);
        for(final Wolf nearbyWolf : nearbyWolves) {
            if(nearbyWolf.hasWolfPack()) {
                this.joinWolfPack(nearbyWolf.wolfPack);
                return;
            }
        }
    }

    protected boolean hasWolfPack() {
        return this.wolfPack != null;
    }

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

    protected void eatCarcassIfInRange(World world, Set<Location> surroundingLocations) {
        surroundingLocations.forEach( l -> {
            if(world.containsNonBlocking(l) && world.getNonBlocking(l) instanceof Carcass carcass) {
                carcass.consume(world);
                this.ate();
                this.increaseEnergy(30);
                // Only eat once per time step 
                return;
            }
        });

    }

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
                return obj instanceof Rabbit;
            });

            // Carcass is NonBlockable, so Wolf should also be allowed to stand on top
            surroundingLocations.add(currentLocation);
            this.eatCarcassIfInRange(world, surroundingLocations);
        }else {
            // TODO make this more readable
            // Attack any wolf that isn't in this wolf's wolfpack and wanders nearby
            this.attackIfInRange(world, surroundingLocations, 
            (obj) -> {
                    if(obj instanceof Wolf wolf && wolf.wolfPack != this.wolfPack) {
                        return true;
                    }
                    return false;
            });

            // TODO make this more readable
            // If wolf has no path, then seek nearest wolfpack member
            if(!this.pathFinder.hasPath() && this.hasWolfPack() )  {
                boolean res = this.pathFinder.findPath(
                (location) -> {
                        for(Location t : world.getSurroundingTiles(location)) {
                            if(world.getTile(t) instanceof Wolf wolf && wolf != this && wolf.wolfPack == this.wolfPack) return true;
                        }
                        return false;

                    }, 
                    world 
                );
            }

        }

        if(this.pathFinder.hasPath()) {
            final Location nextStep = this.pathFinder.getPath().poll();
            if(world.isTileEmpty(nextStep)) world.move(this, nextStep);
        }else {
            this.wander(world);
        }


    }

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

    public boolean isInHole() {
        if(this.wolfPack == null) return false;
        return this.wolfPack.assignedHole.getInhabitants().contains(this);
    }

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
        }
    }

    @Override
    public DisplayInformation getInformation() {
        if(this.age > 3) return Wolf.adultWolf;
        return Wolf.babyWolf;
    }

    @Override
    public void killAnimal(World world) {
        if(this.wolfPack != null) this.wolfPack.removeMember(this);
        super.killAnimal(world);
    }


    @Override
    public void attack(Animal prey, World world) {
        prey.takeDamage(30, world);
    }

}

