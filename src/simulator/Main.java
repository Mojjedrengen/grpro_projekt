package simulator;

import java.awt.Color;
import java.util.List;
import java.io.*;

import itumulator.executable.DisplayInformation;
import itumulator.executable.Program;

import simulator.util.WorldLoader;
import simulator.util.exceptions.InvalidWorldInputFileException;
import simulator.actors.*;
import simulator.objects.*;
import simulator.objects.holes.*;

/**
 * Main entry point of the program
 */
public class Main {

    public static void main(String[] args) {

        final int windowResolution = 800;
        final int delay = 400;
        WorldLoader wl = null;
        try {
            wl = new WorldLoader("resources/inputs/week-1/wolf_testing.txt", windowResolution, delay);
        }catch(InvalidWorldInputFileException e) {
            System.out.println("WorldLoader: Syntax error in input file");
            System.out.println("Line number: " + e.getLineNumber());
            System.out.println("Offending line: " + e.getInvalidLine());
            System.out.println("Error: " + e.getError());
            // TODO: GUI widget/file explorer to allow the user to select new input file
            System.exit(0);
        }catch(IllegalArgumentException e) {
            System.out.println("WorldLoader: Invalid argument");
            System.out.println(e.getMessage());
            System.exit(0);
        }catch(FileNotFoundException e) {
            System.out.println("WorldLoader: File not found");
            System.out.println(e.getMessage());
            System.exit(0);
        }

        Program p = wl.getProgram();
        p.setDisplayInformation(RabbitHole.class, new DisplayInformation(Color.black, "hole"));
        p.setDisplayInformation(WolfHole.class, new DisplayInformation(Color.black, "hole"));

        List<Animal> animals = wl.getAnimals();
        List<NonBlockable> nonBlockables = wl.getNonBlockables();

        System.out.println("world size: " + wl.getWorldSize());
        System.out.println("Animals size: " + animals.size());
        System.out.println("NonBlockabes size: " + nonBlockables.size());

        p.show();
    }
}
