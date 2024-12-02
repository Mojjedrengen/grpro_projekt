package simulator.util;

import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.*;

import itumulator.executable.Program;
import itumulator.world.World;
import itumulator.world.Location;

import simulator.objects.Carcass;
import simulator.objects.NonBlockable;
import simulator.actors.*;
import simulator.objects.holes.RabbitHole;
import simulator.objects.holes.WolfHole;
import simulator.util.exceptions.*;
import simulator.objects.plants.Grass;
import simulator.objects.plants.Bush;

/**
 * This class is used to parse world input files
 */
public class WorldLoader {

    private Pattern rangePattern;
    private Pattern numberPattern;
    final int worldSize;
    final int windowResolution;
    final int delay;

    Program program;
    World world;
    List<Animal> animals;
    List<NonBlockable> nonblockables;

    /**
     * Parses world input file.
     * File format specification is included in project description
     *
     * @param filePath - Relative or full file path to input file
     */
    public WorldLoader(String filePath, final int windowResolution, final int delay) throws FileNotFoundException {
        // Match number ranges e.g. "24-35"
        this.rangePattern = Pattern.compile("^[0-9]+-[0-9]+$", Pattern.CASE_INSENSITIVE);
        // Simply match numbers e.g. "34"
        this.numberPattern = Pattern.compile("^[0-9]+$", Pattern.CASE_INSENSITIVE);

        if(windowResolution <= 0) throw new IllegalArgumentException("Resolution must be greater than 0");
        this.windowResolution  = windowResolution;

        if(delay <= 0) throw new IllegalArgumentException("Delay must be greater than 0");
        this.delay = delay;

        File inputFile = new File(filePath);
        if(!inputFile.isFile()) throw new IllegalArgumentException("Not a file or file not found");

        this.animals = new LinkedList<>();
        this.nonblockables = new LinkedList<>();

        String inputLine = null;
        int lineNumber = 1;
        Scanner scanner = new Scanner(inputFile);

        { // First line includes worldSize
            inputLine = scanner.nextLine().trim();

            Matcher matcher = this.numberPattern.matcher(inputLine);
            if(!matcher.find()) {
                scanner.close();
                throw new InvalidWorldInputFileException
                ("First line must be positive integer indicating world size", inputLine, lineNumber);
            }

            this.worldSize = Integer.parseInt(inputLine);
        }

        this.program = new Program(this.worldSize, this.windowResolution, this.delay);
        this.world = this.program.getWorld();

        final Random random = new Random();
        while(scanner.hasNextLine()) {
            inputLine = scanner.nextLine().trim();
            lineNumber++;

            // Ignore blank lines
            if(inputLine.length() == 0) continue;

            this.parseLine(inputLine, lineNumber, random);
        }

        scanner.close();
    }

    /**
     * Gets the world size parsed from the input file
     *
     * @return world size
     */
    public int getWorldSize() {
        return this.worldSize;
    }

    /**
     * Gets the list of animals parsed from the input file
     *
     * @return list of animals 
     */
    public List<Animal> getAnimals() {
        return this.animals;
    }

    /**
     * Gets the list of NonBlockables parsed from the input file
     *
     * @return list of NonBlockables 
     */
    public List<NonBlockable> getNonBlockables() {
        return this.nonblockables;
    }

    /**
     * Gets the program
     *
     * @return the program
     */
    public Program getProgram() {
        return this.program;
    }

    /**
     * Determines dynamic class type of animal from name of animal.
     * Returns supplier lambda function which calls the constructor of the animal
     *
     * @param unknownObjectString - string containing animal name
     * @return dynamic animal supplier or null if the animal does not exist
     */
    private Supplier<? extends Animal> parseAnimal(final String unknownObjectString) {
        switch(unknownObjectString) {
            case "rabbit":
                return () -> { return new Rabbit(); };
            case "wolf":
                return () -> { return new Wolf(); };
            case "bear":
                return () -> { return new Bear(); };
            default:
            return null;
        }
    }

    /**
     * Determines dynamic class type of NonBlockable from name of NonBlockable
     * Returns supplier lambda functoin which calls constructor of the nonblockable
     *
     * @param unknownObjectString - string containing NonBlockable name
     * @return dynamic NonBlockable supplier or null if the NonBlockable does not exist
     */
    private Supplier<? extends NonBlockable> parseNonBlockable(final String unknownObjectString) {
        switch(unknownObjectString) {
            case "grass":
                return () -> { return new Grass(); };
            case "bush":
                return () -> { return new Bush(); };
            case "berry":
                return () -> { return new Bush(); };
            case "burrow":
                return () -> { return new RabbitHole(); };
            case "carcass":
                return () -> { return new Carcass(Carcass.smallCarcass, false); };
            case "carcass fungi":
                return () -> { return new Carcass(Carcass.smallCarcass, true); };
            default:
            return null;
        }
    }


    /** 
     * Parses the number or range from string. Generates random number within range given range
     * 
     * @param unknownNumberOrRange - String that contains trimmed number or range to parse such as "2" or "3-5"
     * @return the parsed number or -1 on error
     */
    private int parseObjectNumber(final String unknownNumberOrRange, final Random random) {

        // Check if single number or range

        Matcher numberMatch = this.numberPattern.matcher(unknownNumberOrRange);
        Matcher numberRangeMatch = this.rangePattern.matcher(unknownNumberOrRange);

        if(numberMatch.find()) {
            return Integer.parseInt(unknownNumberOrRange);
        }else if(numberRangeMatch.find()) {
            final String[] rangeString = unknownNumberOrRange.split("-");

            final int min = Integer.parseInt(rangeString[0]);
            final int max = Integer.parseInt(rangeString[1]);

            if(min > max) return -1;
            return random.nextInt(min, max + 1);
        }
        return -1;
    }

    /** Parses location from following type of string "(x,y)" 
    * no whitespace is allowed around parantheses but there may be whitespace within
    *
    * @param unknownCoordinateString - string containing Location coordinate in the following format: "(x,y)"
    * @return Location if coordinate string is valid, null otherwise
    */
    private Location parseCoordinate(String unknownCoordinateString) {

        // String length should at minimum be 5
        final int stringLength = unknownCoordinateString.length();
        if(stringLength < 5 || 
        unknownCoordinateString.charAt(0) != '(' || 
        unknownCoordinateString.charAt(stringLength - 1) != ')')
        return null;

        // Remove parantheses that are around the coordinate
        unknownCoordinateString = unknownCoordinateString.substring(1, stringLength - 1);

        String[] tokens = unknownCoordinateString.split(",");
        // We expect two components. x and y and nothing else
        if(tokens.length != 2) return null;
        
        // Remove whitespace around coordinates
        tokens[0] = tokens[0].trim();
        tokens[1] = tokens[1].trim();

        // Ensure that both coordinates are in fact postive numbers
        if(!(this.numberPattern.matcher(tokens[0])).find() || 
        !(this.numberPattern.matcher(tokens[1])).find())
        return null;

        // Input has been checked, we can now return the coordinate as a location
        return new Location(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
    }

    /**
     * If Carcass has fungi, then it is always the second token that specifies that  
     * Example:
     * Carcass fungi 1 (2,3)
     *
     * @param tokens the input tokens split by white space
     * @return whether if input line specifies fungi
     */
    private boolean hasFungi(final String[] tokens) {
        if(tokens.length < 2) return false;
        return tokens[1].trim().equals("fungi");
    }

    /** 
     * Used to parse an individual line of the world input file. Except for
     * the very first line containing the world size number.
     *
     * Adds objects to their correspdoning list.
     *
     * @param line - the line to parse
     * @param lineNumber - the line number for the line. Only used for debugging purposes
     */
    private void parseLine(String line, final int lineNumber, final Random random) { 
        line = line.toLowerCase();
        String[] tokens = line.split(" ");

        // Smallest type of input looks something like this:
        // Rabbit 1
        // Therefore, token length must at least be 2
        // Longest type of input looks like this
        // Carcass fungi 1 (1,2)
        // Therefore, token length cannot be greater than 4
        if(tokens.length < 2 || tokens.length > 4) 
        throw new InvalidWorldInputFileException("Unrecongized input format, expecting 2-4 tokens", line, lineNumber);

        // Check if input involves fungi, in which case we need to ensure it's a carcass fungi.
        // If so, then transform the tokens into something we can parse
        if( this.hasFungi(tokens) ) {

            if(!tokens[0].trim().equals("carcass"))
            throw new InvalidWorldInputFileException("Fungi can only exist on a carcass", line, lineNumber);

            tokens[0] = "carcass fungi";

            // Now that the first two tokens have been merged, move all other tokens back
            for(int i = 1; i < tokens.length - 1; i++) {
                tokens[i] = tokens[i+1];
            }

            // New array without the last element since we don't need that anymore
            // Some logic further down requires the length to be 3 if a coordinate is specified
            if(tokens.length == 4)
                tokens = new String[]{tokens[0], tokens[1], tokens[2]};
            else if(tokens.length == 3)
                tokens = new String[]{tokens[0], tokens[1]};
            else {
                throw new InvalidWorldInputFileException("Unrecongized input format, expecting 2-4 tokens", line, lineNumber);
            }
        }

        // Parses the number or range that comes after object name, e.g. "Rabbit 2" or "Rabbit 5-10"
        // In case of range, parseObjectNumber returns random number within that range
        final int numberOfObjects = parseObjectNumber(tokens[1].trim(), random);
        if(numberOfObjects == -1)
        throw new InvalidWorldInputFileException("Invalid number or range of objects", line, lineNumber);

        final Location objectLocation;
        // Check if line is long enough to contain a specific coordinate.
        // If so, make sure that the length of the line isn't simply due to line containing fungi token
        if(tokens.length == 3) {
            if(numberOfObjects != 1)
            throw new InvalidWorldInputFileException("Cannot place multiple objects of the same type on the same tile", line, lineNumber);

            objectLocation = this.parseCoordinate(tokens[2].trim());
        }else objectLocation = null;

        final String unknownObjectString = tokens[0].trim();

        { // Intentional useless identation for readability. 

            // Check if object is an animal
            Supplier<? extends Animal> animalConstructor = parseAnimal(unknownObjectString);
            // Check if object is an nonblockable
            Supplier<? extends NonBlockable> nonBlockableConstructor = parseNonBlockable(unknownObjectString);

            if(animalConstructor != null) {
                Animal newAnimal = animalConstructor.get();
                WolfPack wolfPack = null;

                if(newAnimal instanceof Wolf && numberOfObjects > 1) {
                    wolfPack = new WolfPack(this.world);
                }

                // Already cheked that if objectLocation is not null, then we know for sure that numberOfObjects is 1
                for(int i = 0; i < numberOfObjects; i++) {
                    newAnimal = animalConstructor.get();
                    this.animals.add(newAnimal);
                    this.world.setTile((objectLocation != null && this.world.isTileEmpty(objectLocation)) ? 
                        objectLocation : 
                        Utilities.getRandomEmptyLocation(random, this.world, this.worldSize), 
                        newAnimal
                    );

                    if(wolfPack != null) {
                    ((Wolf)newAnimal).joinWolfPack(wolfPack);
                    } 
                }
            }else if(nonBlockableConstructor != null) {

                NonBlockable newNonBlockable;
                for(int i = 0; i < numberOfObjects; i++) {
                    newNonBlockable = nonBlockableConstructor.get();
                    this.nonblockables.add(newNonBlockable);
                    this.world.setTile((objectLocation != null && !this.world.containsNonBlocking(objectLocation)) ? 
                        objectLocation :  
                        Utilities.getRandomEmptyNonBlockingLocation(random, this.world, this.worldSize), newNonBlockable);
                }
            }else throw new InvalidWorldInputFileException("Unknown Animal/NonBlockable", line, lineNumber);


        }
    }

}


// Alternative we can use during construction of animals/nonblockabes to reduce repetion of code.
// Just  not very readable
//    private <T> void addNewObject(
//        final int numberOfObjects,
//        final Location objectLocation,
//        final Supplier<T> objectSupplier,
//        final Function<Location, Boolean> objectLocationCondition,
//        final Consumer<T> objectListAdder,
//        final Supplier<Location> randomLocationSupplier ) {
//
//        for(int i = 0; i < numberOfObjects; i++) {
//            T newObject = objectSupplier.get();
//            objectListAdder.accept(newObject);
//            this.world.setTile(
//                objectLocationCondition.apply(objectLocation) ? objectLocation : randomLocationSupplier.get(),
//                newObject );
//        }
//
//    }
