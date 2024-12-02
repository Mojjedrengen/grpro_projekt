package simulator.objects;

import itumulator.simulator.Actor;
import itumulator.world.Location;
import itumulator.world.World;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import simulator.objects.plants.Fungi;

import java.awt.*;


/**
 * Represents a carcass left behind when an animal dies.
 */
public class Carcass extends NonBlockable implements DynamicDisplayInformationProvider, Actor {

    // A predator can call the consume method on a Carcass multiple times.
    private int meatLeft;
    private final DisplayInformation carcassImage;
    private final int maxAge;
    private int currentAge;
    private boolean hasFungi;

    public Carcass(final DisplayInformation carcassImage, boolean hasFungi) {
        if (carcassImage == bigCarcass) {
            meatLeft = 4;
            maxAge = 4;
        } else {
            meatLeft = 2;
            maxAge = 2;
        }

        this.carcassImage = carcassImage;
        this.currentAge = 0;
        this.hasFungi = hasFungi;

        if(this.hasFungi) System.out.println("Carcass Constructor: Carcass WITH fungi created");
        else System.out.println("Carcass Constructor: Carcass WITHOUT fungi created");
    }

    public static final DisplayInformation smallCarcass = new DisplayInformation(Color.yellow, "carcass-small");
    public static final DisplayInformation bigCarcass = new DisplayInformation(Color.green, "carcass");

    public DisplayInformation getInformation() {
        return this.carcassImage;
    }

    public void consume(World world) {
        this.meatLeft--;
        if (meatLeft <= 0) world.delete(this);
    }

    public int getMeatLeft() {
        return this.meatLeft;
    }

    public boolean hasFungi() {
        return hasFungi;
    }

    public void age(World world) {

        currentAge++;
        if (currentAge >= maxAge) {
            Location current = world.getLocation(this);
            if (this.hasFungi()) {
                world.delete(this);
                System.out.println("Removed carcass due to age");
                if(this.carcassImage == bigCarcass) {
                    world.setTile(current, new Fungi(true));
                    System.out.println("Placed large fungi");
                } else {
                    world.setTile(current, new Fungi(false));
                    System.out.println("Placed small fungi");
                }

                //Set a fungi in current Location to replace the infected carcass
            } else {
                world.delete(this);
            }
        }
    }

    @Override
    public void act(World world) {
        if(world.getCurrentTime() == 0) {
            if (currentAge < maxAge) {
                this.age(world);
            }
        }
    }

    public void infectWithFungi(World world) {
        this.hasFungi = true;
    }
}
