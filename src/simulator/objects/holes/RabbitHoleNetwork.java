package simulator.objects.holes;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Animal;

import java.util.HashSet;
import java.util.Set;

public class RabbitHoleNetwork {
    Set<RabbitHole> entrances;
    Set<Animal> inhabitants;

    public RabbitHoleNetwork(){
        this.entrances = new HashSet<>();
        this.inhabitants = new HashSet<>();
    }
    public RabbitHoleNetwork(RabbitHole initialHole) {
        this();
        this.entrances.add(initialHole);
    }

    public void createHole(World world, Location location) {
        if (world.containsNonBlocking(location)) return;
        RabbitHole hole = new RabbitHole();
        this.entrances.add(hole);
        world.setTile(location, hole);
    }
    public void destroyHole(World world, RabbitHole hole) {
        if (this.entrances.contains(hole)) return;
        this.entrances.remove(hole);
        hole.destroyHole(world);
    }

    public void animalEnters(Animal animal) {
        this.inhabitants.add(animal);
    }
    public void animalExits(Animal animal) {
        this.inhabitants.remove(animal);
    }
}
