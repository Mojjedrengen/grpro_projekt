package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.World;
import itumulator.world.Location;

import java.awt.Color;
import java.util.Set;

import simulator.util.Utilities;
import simulator.objects.holes.WolfHole;

public class Wolf extends Animal implements DynamicDisplayInformationProvider {

    static final DisplayInformation adultWolf = new DisplayInformation(Color.black, "wolf");
    static final DisplayInformation babyWolf = new DisplayInformation(Color.black, "wolf-small");

    WolfPack wolfPack;

    public Wolf() {
        // 50 max energy, lives until age of 12, eats rabbits
        super(50, 12, Rabbit.class);
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


    protected void attackIfInRange(World world, Set<Location> surroundingLocations) {
        surroundingLocations.forEach( l -> {
            if(Utilities.locationContainsAnimal(world, l, this.foodType)) {

            ((Animal)world.getTile(l)).killAnimal(world);
                // For now, wolf will simply gain energy. Once carcass is added, wolf will eat that instead
                this.hasEatenToday = true;
                this.increaseEnergy(40);
                // Only kill one rabbit per tick
                return;
            }
        });
    }

    //    protected void eatCarcassIfInRange(World world, Set<Location> surroundingLocations) {
    //        surroundingLocations.forEach( l -> {
    //            if(Utilities.locationContainsAnimal(world, l, Carcass.class)) {
    //                ((Animal)world.getTile(l)).killAnimal(world);
    //                // Only kill one rabbit per tick
    //                return;
    //            }
    //        });
    //
    //    }

    protected void dayTimeBehaviour(World world) {
        if(this.isInHole()) {
            this.wolfPack.getHole().wolfExit(this, world);
        }else if(!this.hasWolfPack()) { // Can't have hole and not have wolf pack at same time
            this.searchForWolfPack(world);
        }

        // If hasn't eaten today and current path doesn't lead to rabbit,
        // then generate a path to nearest rabbit
        if(!this.hasEatenToday) {
            if(!this.pathFinder.hasPath()) {
                this.findPathToNearestFood(world);
            }else if(!Utilities.locationContainsAnimal(world, this.pathFinder.getFinalLocationInPath(), this.foodType)) {
                this.findPathToNearestFood(world);
            }
        }


        // Only eat if energy isn't max or hasn't eaten today
        if(this.getEnergy() != this.maxEnergy || !this.hasEatenToday){
            // If Rabbit in attack range, then attack it
            final Set<Location> surroundingLocations = world.getSurroundingTiles();
            this.attackIfInRange(world, surroundingLocations);
            // Carcass hasn't been added yet
            //this.eatCarcassIfInRange(world, surroundingLocations);
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
            // Attempt to reproduce
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


}

