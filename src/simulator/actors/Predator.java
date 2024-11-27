package simulator.actors;
import itumulator.world.World;

/**
 * The Predator interface defines behavior for actors capable of attacking (bears/wolves).
 */
public interface Predator {
    /**
     * Defines the behavior for attacking.
     * 
     * @param prey the prey duh
     */
    void attack(Object prey, World world);
}
