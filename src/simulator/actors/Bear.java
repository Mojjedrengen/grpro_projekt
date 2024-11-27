package simulator.actors;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.World;
import java.awt.Color;
import simulator.objects.plants.Bush;

public class Bear extends Animal implements DynamicDisplayInformationProvider, Predator {

    static final DisplayInformation adultBear = new DisplayInformation(Color.black, "bear");
    static final DisplayInformation babyBear = new DisplayInformation(Color.black, "bear-small");

    public Bear() {
        // 50 max energy, lives for 25 years, gets 100 health
        // Bears eat mostly berries from bushes, but can also hunt so we need to take that into accout
        super(50, 25, Bush.class, 100);
    }

    @Override
    public void act(World world) {

    }

    @Override
    public DisplayInformation getInformation() {
        if(this.age > 10) return Bear.adultBear;
        return Bear.babyBear;
    }

    @Override
    public void attack(Object prey, World world) {
        //if in surrounding tiles??
        //target.takeDamage(x, world))
    }

}

