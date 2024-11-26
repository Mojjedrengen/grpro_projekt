package simulator.util;

import itumulator.world.Location;
import itumulator.world.World;

import simulator.objects.NonBlockable;
import simulator.util.exceptions.FullWorldException;

import java.util.ArrayList;
import java.util.Set;
import java.util.Random;
import java.util.Iterator;

/**
 * The Utilities class functions more akin to a namespace rather than a class.
 * It only contains static functions that offer functionality that is often
 * needed throughout the program.
 */
public class Utilities {

    public static <T> T getRandomFromSet(Set<T> set, Random random) {
        final int setSize = set.size();
        if(setSize == 0) return null;

        T value = null;
        Iterator<T> it = set.iterator();
        
        // Pick random element from set
        final int randomTileIndex = random.nextInt(setSize);
        for(int i = 0; i < randomTileIndex; i++)
            value = it.next();

        return value;
    }

    public static <T> T getRandomFromSet(Set<T> set) {
        return getRandomFromSet(set, new Random());
    }

    public static <T> T getLast(ArrayList<T> array) {
        return array.get(array.size() - 1);
    }

    public static <T> boolean locationContainsNonBlockingType(World world, Location location, Class<T> type) {
        if(!world.containsNonBlocking(location)) return false;

        return type.isInstance(world.getNonBlocking(location));
    }


    // Two versions of the same function to reduce amount of times new instances
    // of random are created and destroyed
    public static Location getRandomLocation(final int worldSize) {
        Random r = new Random();
        return getRandomLocation(r, worldSize);
    }
    public static Location getRandomLocation(Random r, final int worldSize) {
        return new Location(r.nextInt(worldSize), r.nextInt(worldSize));
    }

    public static Location getRandomEmptyLocation(final Random random, final World world, final int worldSize) {
        Location location = getRandomLocation(random, worldSize);

        final int maxIterations = 100;
        int i = 0;
        while(!world.isTileEmpty(location)) {
            location = getRandomLocation(random, worldSize);
            i++;
            if(i > maxIterations) {
                throw new FullWorldException("Could not find empty tile");
            }
        }

        return location;
    }
    public static Location getRandomEmptyLocation(final World world, final int worldSize) {
        Random random = new Random();
        return getRandomEmptyLocation(random, world, worldSize);
    }

    public static Location getRandomEmptyNonBlockingLocation(final Random random, final World world, final int worldSize) {

        Location location = getRandomLocation(random, worldSize);

        final int maxIterations = 100;
        int i = 0;
        while(world.containsNonBlocking(location)) {
            location = getRandomLocation(random, worldSize);
            if(i > maxIterations) {
                throw new FullWorldException("Could not find nonblocking-free tile");
            }
        }

        return location;
    }

    public static Location getRandomEmptyNonBlockingLocation(final World world, final int worldSize) {
        Random random = new Random();
        return getRandomEmptyNonBlockingLocation(random, world, worldSize);
    }

}
