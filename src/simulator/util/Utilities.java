package simulator.util;

import itumulator.world.Location;

public class Utilities {

    public static boolean locationEquals(Location a, Location b) {
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

}
