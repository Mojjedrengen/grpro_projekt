package simulator.objects.plants;

import itumulator.world.Location;
import itumulator.world.World;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;
import simulator.objects.Carcass;
import simulator.objects.NonBlockable;

import java.awt.*;
import java.util.Random;
import java.util.Set;

public class Fungi extends Plant implements DynamicDisplayInformationProvider {

    private final boolean isLarge;
    private int currentAge;
    private final int maxAge;

    static final DisplayInformation fungiLarge = new DisplayInformation(Color.yellow, "fungi");
    static final DisplayInformation fungiSmall = new DisplayInformation(Color.yellow, "fungi-small");

    public Fungi(boolean isLarge) {
        super(10, 25, () -> new Fungi(false)); // Spreading chance, growth chance, and default to small fungi for dynamic spreading
        this.isLarge = isLarge;
        this.currentAge = 0;
        if (isLarge) this.maxAge = 5;
        else this.maxAge = 3;  // Large fungi lasts longer than small fungi
    }

    @Override
    public DisplayInformation getInformation() {
        if (isLarge) return fungiLarge;
        return fungiSmall;
    }

    public int getCurrentAge() {
        return currentAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    @Override
    public void act(World world) {
        if (!world.contains(this)) {
            return; // Exit early if the Fungi is no longer in the world
        }
        Random random = new Random();

        // Attempt to spread to nearby carcasses
        boolean spreadSuccessful = this.spreadToNearbyCarcass(world, random);

        // If it isnt surrounded by carcass' age fungi
        if (!CarcassNearby(world)) {
            this.currentAge++;
            System.out.println("Aged a fungi");
            if (this.currentAge >= this.maxAge) {
                System.out.println("Removed a fungi due to age");
                world.delete(this); // Fungi dies and is removed
            }
        }
    }

    public boolean CarcassNearby(World world) {
        if (!world.contains(this)) {
            return false;
        }
        Set<Location> nearbyLocations = world.getSurroundingTiles(world.getLocation(this), 2);
        boolean carcassNearby = false;
        for (Location location : nearbyLocations) {
            if (world.containsNonBlocking(location) && world.getNonBlocking(location) instanceof Carcass) {
                carcassNearby = true;
            }
        }
        return carcassNearby;
    }

    private boolean spreadToNearbyCarcass(World world, Random random) {
        if (!world.contains(this)) {
            return false;
        }
            Location currentLocation = world.getLocation(this);
            Set<Location> nearbyLocations = world.getSurroundingTiles(currentLocation, 2); // Get tiles in a 2-tile radius
            boolean spread = false;

            for (Location location : nearbyLocations) {
                if (world.containsNonBlocking(location) &&
                        world.getNonBlocking(location) instanceof Carcass carcass &&
                        !carcass.hasFungi()) {
                    if (random.nextInt(1, 101) <= this.spreadChance) {
                        carcass.infectWithFungi(world); // Infect the carcass with fungi
                        System.out.println("Infected a carcass");
                        spread = true;
                    }
                }
            }
            return spread; // No suitable carcass to spread to
        }
}
