package simulator;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.io.*;

import itumulator.executable.DisplayInformation;
import itumulator.executable.Program;
import itumulator.world.Location;
import itumulator.world.World;

import simulator.util.WorldLoader;
import simulator.actors.*;
import simulator.objects.*;

public class Main {

    public static void main(String[] args) {

        WorldLoader wl = null;
        try {
            wl = new WorldLoader("resources/inputs/week-1/new_input.txt");
        }catch(FileNotFoundException e) {
            System.out.println("Input file not found:");
            System.out.println(e.getMessage());
            System.exit(0);
        }
        final int worldSize = wl.getWorldSize();

        Program p = new Program(worldSize, 800, 200);
        World w = p.getWorld();
        p.setDisplayInformation(Rabbit.class, new DisplayInformation(Color.red, "rabbit-large"));
        p.setDisplayInformation(RabbitHole.class, new DisplayInformation(Color.black, "hole"));

        List<Animal> animals = wl.getAnimals();
        List<NonBlockable> nonBlockables = wl.getNonBlockables();

        System.out.println("world size: " + wl.getWorldSize());
        System.out.println("Animals size: " + animals.size());
        System.out.println("NonBlockabes size: " + nonBlockables.size());

        Random random = new Random();
        Location location = new Location( random.nextInt(worldSize), random.nextInt(worldSize) );

        for(Animal animal : animals) {
            while(!w.isTileEmpty(location)) {
                location = new Location( random.nextInt(worldSize), random.nextInt(worldSize) );
            }
            w.setTile(location, animal);
        }

        for(NonBlockable nonBlockable : nonBlockables) {
            while(w.containsNonBlocking(location)) {
                location = new Location( random.nextInt(worldSize), random.nextInt(worldSize) );
            }
            w.setTile(location, nonBlockable);
        }

        p.show();
    }
}
