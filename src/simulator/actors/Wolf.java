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

    protected void searchForWolfPack(World world) {
        Set<Location> locations = world.getSurroundingTiles(3);
        locations.remove(world.getLocation(this));

        Set<Wolf> nearbyWolves = world.getAll(Wolf.class, locations);
        for(Wolf wolf : nearbyWolves) {
            if(wolf.hasWolfPack()) {
                this.wolfPack = wolf.wolfPack;
                this.wolfPack.addMember(this);
            }
        }
    }

    protected boolean hasWolfPack() {
        return this.wolfPack != null;
    }

    protected void goToHole(World world) {
        if(!this.hasWolfPack()) return;

        this.pathFinder.setLocation(world.getLocation(this));
        this.pathFinder.findPathToLocation(this.wolfPack.assignedHole.getLocation(world), world);
    }

    protected boolean isInHole() {
        if(this.wolfPack == null) return false;
        return this.wolfPack.assignedHole.getInhabitants().contains(this);
    }

    protected void exitHole() {
        // Exit hole logic
    }

    protected void dayTimeBehaviour(World world) {
        if(this.isInHole()) {
            this.exitHole();
        }else if(!this.hasWolfPack()) { // Can't have hole and not have wolf pack at same time
            this.searchForWolfPack(world);
        }
    }

    protected void nightTimeBehaviour(World world) {
        if(!this.isInHole() && this.hasWolfPack()) {
            this.goToHole(world);
        }
    }

    @Override
    public void act(World world) {
        if(world.isDay()) {
            this.dayTimeBehaviour(world);
        }

        //Hunt rabbits
        //Look for wolf pack if this wolf doesn't have one
        //Fight wolves from other wolf packs 
    }

    @Override
    public DisplayInformation getInformation() {
        if(this.age > 3) return Wolf.adultWolf;
        return Wolf.babyWolf;
    }

}

