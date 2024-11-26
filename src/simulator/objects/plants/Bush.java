package simulator.objects.plants;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.World;

import java.awt.*;
import java.util.Random;

/**
 * Bush is a plant that grows berries after time.
 * When an animal "consumes a bush", the growth stage of the bush goes back, but
 * the bush itself does not get deleted from the world, as the animals eat
 * the fruits/berries on the bush but not the bush itself.
 *
 * Bushes can in fact spread. But due to the fact that there's no natural way
 * of them to be removed, the spread chance is very low(5%).
 *
 */
public class Bush extends Plant implements DynamicDisplayInformationProvider {

    static final DisplayInformation regularBush = new DisplayInformation(Color.red, "bush");
    static final DisplayInformation ripeBush = new DisplayInformation(Color.red, "bush-berries");

    static final int bushMaxAge = 6;
    protected int bushAge;

    public Bush() {
        super(3, 40, () -> {return new Bush();});
        this.bushAge = 0;
    }

    @Override
    public void act(World world) {
        Random rand = new Random();
        
        this.spread(world, rand);
        this.grow(rand);

        if(world.getCurrentTime() == 0)
            this.bushAge++;

        // Bushes get deleted after certain number of days to prevent bushes
        // from over-spreading and filling up the whole world
        if(this.bushAge > Bush.bushMaxAge) {
            world.delete(this);
        }

    }

    @Override
    public DisplayInformation getInformation() {
        if(this.getCurrentStage() != Plant.Stage.RIPE) return Bush.regularBush;
        return Bush.ripeBush;
    }

}

