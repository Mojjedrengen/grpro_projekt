package simulator.actors.cordyceps;

import itumulator.world.World;
import org.jetbrains.annotations.NotNull;
import simulator.actors.Animal;

public interface Cordyceps {

    void spread(@NotNull World world, Class<? extends Animal> hostsKind);
    void decompose(@NotNull World world);
}
