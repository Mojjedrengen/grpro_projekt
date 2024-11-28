package simulator.objects;

import itumulator.world.World;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import java.awt.*;


/**
 * Represents a carcass left behind when an animal dies.
 */
public class Carcass extends NonBlockable implements DynamicDisplayInformationProvider{

    // A predator can call the consume method on a Carcass multiple times.
    private int meatLeft;
    private final DisplayInformation carcassImage;

    public Carcass(final DisplayInformation carcassImage) {
        if(carcassImage == bigCarcass) meatLeft = 4;
        else meatLeft = 2;

        this.carcassImage = carcassImage;
    }
    
    public static final DisplayInformation smallCarcass = new DisplayInformation(Color.yellow, "carcass-small");
    public static final DisplayInformation bigCarcass = new DisplayInformation(Color.green, "carcass");


    public DisplayInformation getInformation() {
        return this.carcassImage;
    }

    public void consume(World world) {
        this.meatLeft--;
        if(meatLeft <= 0) world.remove(this);
    }

    public int getMeatLeft() {
        return this.meatLeft;
    }
}
