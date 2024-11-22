package simulator.util;

import itumulator.world.Location;
import itumulator.world.World;

import simulator.objects.NonBlockable;

import java.util.ArrayList;

/**
 * The Utilities class functions more akin to a namespace rather than a class.
 * It only contains static functions that offer functionality that is often
 * needed throughout the program.
 */
public class Utilities {

    public static <T> T getLast(ArrayList<T> array) {
        return array.get(array.size() - 1);
    }

    public static <T> boolean locationContainsNonBlockingType(World world, Location location, Class<T> type) {
        if(!world.containsNonBlocking(location)) return false;

        return type.isInstance(world.getNonBlocking(location));
    }

}
