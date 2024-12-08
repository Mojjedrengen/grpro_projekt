package simulator.objects.holes;

import itumulator.world.Location;
import itumulator.world.World;
import simulator.actors.Animal;
import simulator.actors.Rabbit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class RabbitHoleNetwork {
    private Set<RabbitHole> entrances;
    private Set<Animal> inhabitants;

    private static RabbitHoleNetwork instance;

    private RabbitHoleNetwork(){
        this.entrances = new HashSet<>();
        this.inhabitants = new HashSet<>();
    }

    public static synchronized RabbitHoleNetwork getInstance(){
        if(instance == null){
            instance = new RabbitHoleNetwork();
        }
        return instance;
    }

    public void addHole(RabbitHole hole) {
        this.entrances.add(hole);
    }

    public void createHole(World world, Location location) {
        if (world.containsNonBlocking(location)) return;
        RabbitHole hole = new RabbitHole();
        this.entrances.add(hole);
        world.setTile(location, hole);
    }
    public void destroyHole(World world, RabbitHole hole) {
        if (!this.entrances.contains(hole)) return;
        this.entrances.remove(hole);
        hole.destroyHole(world);
    }
    public void destroyAllHoles(World world) {
        Iterator<RabbitHole> it = this.entrances.iterator();
        while (it.hasNext()) {
            destroyHole(world, it.next());
        }
    }

    public Set<Location> getHoleLocation (World world) {
        Set<Location> locations = new HashSet<>();
        for (RabbitHole hole : this.entrances) {
            locations.add(hole.getLocation(world));
        }
        return locations;
    }

    public RabbitHole getHoleFromLocation(World world, Location location) {
        for (RabbitHole hole : this.entrances) {
            if (hole.getLocation(world).equals(location)) {
                return hole;
            }
        }
        return null;
    }

    public void animalEnters(Animal animal) {
        this.inhabitants.add(animal);
    }
    public void animalExits(Animal animal) {
        this.inhabitants.remove(animal);
    }

    public Set<Animal> getInhabitants() {return this.inhabitants;}
    public Set<RabbitHole> getEntrances() {return this.entrances;}

    public void reproduceInhabitant(World world) {
        Random rand = new Random();
        Set<Animal> offSprings = new HashSet<>();
        for (int i = 0; i < this.inhabitants.size(); i++){
            if (this.inhabitants.size() > 1 && rand.nextInt(20) == 4 && offSprings.size() < this.inhabitants.size()/2) {
                Animal offspring = new Rabbit();
                offSprings.add(offspring);
            }
        }
        if (!offSprings.isEmpty()) {
            for (Animal offspring : offSprings) {
                this.animalEnters(offspring);
                ((Rabbit)offspring).assignNetwork(this);
                world.add(offspring);
            }
        }
    }
}
