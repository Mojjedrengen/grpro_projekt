package simulator.actors.cordyceps;

import itumulator.world.World;
import org.jetbrains.annotations.NotNull;
import simulator.actors.Animal;

public interface Cordyceps {

    /**
     * Method for the infected animal to spread when dead.
     * @param world The current world
     * @param hostsKind The animal it can infect.
     */
    void spread(@NotNull World world, Class<? extends Animal> hostsKind);

    /**
     * The mushroom slowly decomposes the host animal.
     * This is the method for that
     * @param world The current world.
     */
    void decompose(@NotNull World world);
}
