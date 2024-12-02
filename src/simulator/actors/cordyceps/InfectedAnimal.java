package simulator.actors.cordyceps;

import itumulator.world.Location;
import itumulator.world.World;
import org.jetbrains.annotations.NotNull;
import simulator.actors.Animal;
import simulator.actors.Rabbit;

import java.util.Random;
import java.util.Set;


public class InfectedAnimal<T extends Animal> extends Animal implements Cordyceps {
    //static DisplayInformation largeRabbit = new DisplayInformation(Color.magenta, "rabbit-large-fungi");
    //static DisplayInformation smallRabbit = new DisplayInformation(Color.magenta, "rabbit-fungi-small");

    private final Class<? extends Animal> hostKind;
    private final Class<T> t;
    public Location intitLocation;

    public InfectedAnimal(Class<T> hostKind, @NotNull World world, Animal host) {
        super(100, 10 , hostKind, 5);
        intitLocation = world.getLocation(host);
        world.delete(host);
        this.age = host.getAge();
        this.hostKind = hostKind;
        this.t = hostKind;
    }

    @Override
    public void spread(@NotNull World world, Class<? extends Animal> hostKind) {
        Set<Location> neighbours = world.getSurroundingTiles(3);
        Set<T> nearbyKind = world.getAll(t, neighbours);
        if (nearbyKind.isEmpty()) return;
        Random rand = new Random();
        for (T animal : nearbyKind) {
            if (rand.nextInt(101) > 80) {
                InfectedAnimal<? extends Animal> newInfected = new InfectedAnimal<>(hostKind, world, animal);
                world.setTile(newInfected.intitLocation, newInfected);
            }
        }
    }

    @Override
    public void decompose(@NotNull World world) {
        if (world.getCurrentTime() == World.getTotalDayDuration()/2) {
            this.takeDamage(1, world);
        }
    }

    @Override
    public void killAnimal(@NotNull World world) {
        this.spread(world, hostKind);
        world.delete(this);
    }

    @Override
    public void act(@NotNull World world){
        if (!this.pathFinder.hasPath()) {
            this.findPathToNearestFood(world);
        }
        if (world.isDay()){
            Location nextStep = this.pathFinder.getPath().poll();
            if (world.isTileEmpty(nextStep)) world.move(this, nextStep);
            else this.wander(world);
        }

        decompose(world);
    }
}
