package simulator.actors;

import itumulator.world.World;

import simulator.objects.Grass;

public class Wolf extends Animal {

    WolfPack wolfPack;

    Wolf() {
        super(50, 12, Rabbit.class);
        wolfPack = null;
    }

    private void huntForRabbits(World world) {
        this.pathFinder.setLocation(world.getLocation(this));

        // Note, maybe we can have a more general method in Animal that both
        // wolf and rabbit can use to find path to their nearest prefered food type
        this.pathFinder.findPathToNearest(this.foodType, world);
    }

    private void searchForWolfPack(World world) {

    }

    @Override
    public void act(World world) {
        //Hunt rabbits
        //Look for wolf pack if this wolf doesn't have one
        //Fight wolves from other wolf packs 
    }

}

