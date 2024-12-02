package simulator.actors.cordyceps;

import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import itumulator.world.Location;
import itumulator.world.World;
import org.jetbrains.annotations.NotNull;
import simulator.actors.Animal;
import simulator.actors.Bear;
import simulator.actors.Rabbit;
import simulator.actors.Wolf;
import simulator.util.Utilities;

import java.awt.*;
import java.util.Random;
import java.util.Set;

/**
 * A generic class for animals that are infected by Cordyceps.
 * @param <T> This is the type of animal it was before it was infected.
 */
public class InfectedAnimal<T extends Animal> extends Animal implements Cordyceps, DynamicDisplayInformationProvider {
    static DisplayInformation largeRabbit = new DisplayInformation(Color.magenta, "rabbit-large-fungi");
    static DisplayInformation smallRabbit = new DisplayInformation(Color.magenta, "rabbit-fungi-small");
    static final DisplayInformation adultBear = new DisplayInformation(Color.magenta, "bear-fungi");
    static final DisplayInformation babyBear = new DisplayInformation(Color.magenta, "bear-small-fungi");
    static final DisplayInformation adultWolf = new DisplayInformation(Color.magenta, "wolf-fungi");
    static final DisplayInformation babyWolf = new DisplayInformation(Color.magenta, "wolf-fungi-small");

    private final Class<? extends Animal> hostKind;
    private final Class<T> t;

    /**
     * Constructor used to infect a certain animal
     * @param hostKind This is the type of animal the host was
     * @param world The current world
     * @param host This is the host
     */
    public InfectedAnimal(Class<T> hostKind, @NotNull World world, Animal host) {
        this(hostKind, world);
        world.delete(host);
        this.age = host.getAge();
    }

    /**
     * Constructor used to create an infected animal without a stating host
     * @param hostKind This is the type of animal the infected is infecting
     * @param world The current world
     */
    public InfectedAnimal(Class<T> hostKind, @NotNull World world) {
        super(100, 100 , hostKind, 5);
        this.hostKind = hostKind;
        this.t = hostKind;
        this.age = 1;
    }

    @Override
    public void spread(@NotNull World world, Class<? extends Animal> hostKind) {
        Set<Location> neighbours = world.getSurroundingTiles(3);
        Set<T> nearbyKind = world.getAll(t, neighbours);
        if (nearbyKind.isEmpty()) return;
        Random rand = new Random();
        for (T animal : nearbyKind) {
            if (rand.nextInt(101) > 80) {
                Location l = world.getLocation(animal);
                InfectedAnimal<? extends Animal> newInfected = new InfectedAnimal<>(hostKind, world, animal);
                world.setTile(l, newInfected);
            }
        }
    }

    @Override
    public void decompose(@NotNull World world) {
        if (world.getCurrentTime() == World.getTotalDayDuration()/2) {
            this.takeDamage(1, world);
            this.age++;
        }
    }

    @Override
    public void killAnimal(@NotNull World world) {
        this.spread(world, hostKind);
        world.delete(this);
    }

    @Override
    public void act(@NotNull World world){
        if (!Utilities.worldContainsTypeOfEntities(world, this.foodType)) {
            this.wander(world);
            return;
        }
        if (!this.pathFinder.hasPath()) {
            this.pathFinder.setLocation(world.getLocation(this));
            this.pathFinder.findPathToNearestBlocking(this.foodType, world);
        }
        if (world.isDay()){
            Location nextStep = this.pathFinder.getPath().poll();
            if (nextStep == null) this.wander(world);
            if (world.isTileEmpty(nextStep)) world.move(this, nextStep);
            else this.wander(world);
        }

        decompose(world);
    }

    @Override
    public DisplayInformation getInformation() {
        if (hostKind == Rabbit.class) {
            if (age > 2) return InfectedAnimal.largeRabbit;
            return InfectedAnimal.smallRabbit;
        } else if (hostKind == Bear.class) {
            if (this.age > 10) return InfectedAnimal.adultBear;
            return InfectedAnimal.babyBear;
        } else if (hostKind == Wolf.class) {
            if(this.age > 3) return InfectedAnimal.adultWolf;
            return InfectedAnimal.babyWolf;
        } else {
            return new DisplayInformation(Color.magenta, "fungi");
        }
    }
}
