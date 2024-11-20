package simulator.util;

import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simulator.objects.NonBlockable;
import simulator.actors.*;
import simulator.actors.exceptions.*;

import simulator.util.exceptions.*;
import simulator.objects.Grass;

/**
 * This class is used to parse world input files
 */
public class WorldLoader {

    private Pattern rangePattern;
    private Pattern numberPattern;
    final int worldSize;

    List<Animal> animals;
    List<NonBlockable> nonblockables;

    /**
     * Parses world input file.
     * File format specification is included in project description
     *
     * @param filePath - Relative or full file path to input file
     */
    public WorldLoader(String filePath) throws FileNotFoundException {
        // Match number ranges e.g. "24-35"
        this.rangePattern = Pattern.compile("^[0-9]+-[0-9]+$", Pattern.CASE_INSENSITIVE);
        // Simply match numbers e.g. "34"
        this.numberPattern = Pattern.compile("^[0-9]+$", Pattern.CASE_INSENSITIVE);

        File inputFile = new File(filePath);
        if(!inputFile.isFile()) throw new IllegalArgumentException("Not a file");

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
                ("First line must be number indicating world size", inputLine, lineNumber);
            }

            this.worldSize = Integer.parseInt(inputLine);
        }

        while(scanner.hasNextLine()) {
            inputLine = scanner.nextLine();
            lineNumber++;

            this.parseLine(inputLine.trim(), lineNumber);
        }

        scanner.close();
    }

    public int getWorldSize() {
        return this.worldSize;
    }

    public List<Animal> getAnimals() {
        return this.animals;
    }

    public List<NonBlockable> getNonBlockables() {
        return this.nonblockables;
    }

    /**
     * Determines dynamic class type of animal from name of animal
     *
     * @param unknownObjectString - string containing animal name
     * @return dynamic animal type or null if the animal does not exist
     */
    private Class<? extends Animal> parseAnimal(String unknownObjectString) {
        switch(unknownObjectString) {
            case "rabbit":
                return Rabbit.class;
            default:
                return null;
        }
    }

    /**
     * Determines dynamic class type of NonBlockable from name of NonBlockable
     *
     * @param unknownObjectString - string containing NonBlockable name
     * @return dynamic NonBlockable type or null if the NonBlockable does not exist
     */
    private Class<? extends NonBlockable> parseNonBlockable(String unknownObjectString) {
        switch(unknownObjectString) {
            case "grass":
                return Grass.class;
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
    private int parseObjectNumber(String unknownNumberOrRange) {

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
            return (new Random()).nextInt(min, max + 1);
        }
        return -1;
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
    private void parseLine(String line, int lineNumber) { 
        line = line.toLowerCase();
        final String[] tokens = line.split(" ");

        if(tokens.length != 2) 
            throw new InvalidWorldInputFileException("Unrecongized input format", line, lineNumber);

        // Parses the number or range that comes after object name, e.g. "Rabbit 2" or "Rabbit 5-10"
        final int numberOfObjects = parseObjectNumber(tokens[1].trim());
        if(numberOfObjects == -1)
            throw new InvalidWorldInputFileException("Invalid number or range of objects", line, lineNumber);

        try {
            String unknownObjectString = tokens[0].trim();

            // Check if object is an animal
            Class<? extends Animal> animal = parseAnimal(unknownObjectString);
            // Check if object is an nonblockable
            Class<? extends NonBlockable> nonBlockable = parseNonBlockable(unknownObjectString);

            if(animal != null) {
                for(int i = 0; i < numberOfObjects; i++) {
                    this.animals.add( animal.getDeclaredConstructor().newInstance() );
                }
            }else if(nonBlockable != null) {
                for(int i = 0; i < numberOfObjects; i++) { 
                    this.nonblockables.add( nonBlockable.getDeclaredConstructor().newInstance() );
                }
            }else throw new InvalidWorldInputFileException("Unknown Animal/NonBlockable", line, lineNumber);

        }catch(Exception e) { // Catch checked exception caused by reflection. Provide more informative exception that logs line and line number
            throw new InvalidWorldInputFileException("Unknown Animal/NonBlockable", line, lineNumber);
        }

    }

}


