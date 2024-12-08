package simulator.util;

import itumulator.world.Location;
import itumulator.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import simulator.util.exceptions.FullWorldException;

/**
 * The Utilities class functions more akin to a namespace rather than a class.
 * It only contains static functions that offer functionality that is often
 * needed throughout the program.
 */
public class Utilities {

    public static <T> boolean objectExistsOnSurroundingTiles(Class<T> type, Location location, World world) {
        Set<Location> tiles = world.getSurroundingTiles(location);
        for(Location l : tiles) {
            if(type.isInstance(world.getTile(l))) return true;
        }

        return false;
    }

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

    public static double getDistance(Location loc1, Location loc2) {
        return Math.sqrt(
            Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2)
        );
    }

    public static <T> T getRandomFromSet(Set<T> set) {
        return getRandomFromSet(set, new Random());
    }

    public static <T> T getLast(ArrayList<T> array) {
        return array.get(array.size() - 1);
    }

    public static <T> boolean locationContainsAnimal(World world, Location location, Class<T> type) {
        if(world.isTileEmpty(location)) return false;

        return type.isInstance(world.getTile(location));
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
        return getRandomEmptyLocation(new Random(), world, worldSize);
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
        return getRandomEmptyNonBlockingLocation(new Random(), world, worldSize);
    }


    public static Location getClosestLocationFromSet( Set<Location> set, Location startLocation) {
        Location closest = null;
        int closestDistance = Integer.MAX_VALUE;
        int x1 = startLocation.getX();
        int y1 = startLocation.getY();
        for (Location location : set) {
            int x2 = location.getX();
            int y2 = location.getY();
            int distanceSquard = (int) (Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
            if (distanceSquard < closestDistance) {
                closest = location;
                closestDistance = distanceSquard;
            }
        }
        return closest;
    }

    public static Boolean worldContainsTypeOfEntities(World world, Class<?> type) {
        AtomicBoolean result = new AtomicBoolean(false);
        Map<Object, Location> entities = world.getEntities();
        entities.forEach((key, value) -> {
            if (key.getClass().equals(type)) {
                if (value != null) {
                    result.set(true);
                }
            }
        });
        return result.get();
    }
}
