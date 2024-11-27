package simulator.objects;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import java.awt.*;

/**
 * Represents a carcass left behind when an animal dies.
 */
public class Carcass extends NonBlockable implements DynamicDisplayInformationProvider{
    private final String image;

    public Carcass(String imagePath) {
        this.image = imagePath;
    }
    
    static final DisplayInformation smallCarcass = new DisplayInformation(Color.yellow, "carcass-small");
    static final DisplayInformation bigCarcass = new DisplayInformation(Color.green, "carcass");


    public DisplayInformation getInformation() {
        if ("carcass-small".equals(image)) {return Carcass.smallCarcass;}
        return Carcass.bigCarcass;
    }
}
