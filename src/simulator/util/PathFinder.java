package simulator.util;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.function.*;
import java.util.Iterator;

import simulator.util.Utilities;

/**
 * The PathFinder class can be used by animals to generate paths between two
 * points. This could for example be a rabbit trying to find the shortest route
 * to its own rabbit hole when it turns night.
 */
public class PathFinder {

    private LinkedList<Location> path;
    private Location currentLocation;

    /**
     * PathFinder constructor
     *
     * @param currentLocation - starting point from which path finding will be made from. May be null if location is later set with setLocation()
     */
    public PathFinder(Location currentLocation) {
        this.path = new LinkedList<>();
        this.currentLocation = currentLocation;
    }


    /** 
     * Wrapper around findPath() method. Used to find path to specific location on map
     *
     * @param goal - The goal location that you're looking to find a route to
     * @param world - Reference to the world
     * @return whether if a path was found or not
     */
    public boolean findPathToLocation(Location goal, World world) {
        return this.findPath
        ( (location) -> { return location.equals(goal);}, world);
    }


    /** 
     * Wrapper around findPath() method. Used to find path to specific instance of nonblocking
     *
     * @param type - The type of object you're looking to find the closest route to
     * @param world - Reference to the world
     * @return whether if a path was found or not
     */
    public <T> boolean findPathToNearest(Class<T> type, World world) {
        return this.findPath
        ( (location) -> { 
            // isInstance is null-safe, it returns false if argument is null, which is exactly what we want
            return type.isInstance(world.getTile(location));
        }, world);
    }

    /**
     * Wrapper around findPath() method. Used to find path to tile *surrounding* the blocking tile
     *
     * @param type - The type of object you're looking to find the closest route to
     * @param world - Reference to the world
     * @return whether if a path was found or not
     */
    public <T> boolean findPathToNearestBlocking(Class<T> type, World world) {
        return this.findPath
        ( (location) -> { 
            for(Location t : world.getSurroundingTiles(location)) {
                if(type.isInstance(world.getTile(t))) return true;
            }
            return false;
        }, world);
    }

    /**
     * Valdiates that the entire route is not obstructed.
     * Note that this can drastically change as other animals step in and out of the path.
     * Therefore, it is not worth file re-generating a new path simply because this returns false, as
     * the path can quickly become valid again after a single time step.
     *
     * @param world - Reference to the world
     * @return whether if the entire path is not obstructed
     */
    public boolean isPathStillValid(World world) {
        for(Location loc : this.path) {
            if(!world.isTileEmpty(loc)) return false;
        }

        return true;
    }

    /**
     * Checks whether if the current path isn't empty
     *
     * @return if the path is not empty
     */
    public boolean hasPath() {
        return !this.path.isEmpty();
    }

    // TODO not finished
    public boolean fixObstructedPath(World world) {

        if(this.path.isEmpty()) return false;
        else if(this.currentLocation == null) return false;

        Iterator<Location> iter = this.path.iterator();

        // Hack to get around Javas rule that prevents mutation of primitives in lambdas
        // Who cares? This isn't Haskell
        int[] indexWrapper = {0};

        BiFunction<Iterator<Location>, Function<Location, Boolean>, Integer> findObstruction = (it, cond) -> {
            Location location = this.currentLocation;
            while(it.hasNext()) {
                location = it.next();
                if(cond.apply(location)) {
                    return indexWrapper[0];
                }
                indexWrapper[0]++;
            }
            return -1;
        };

        final Location previousCurrent = this.currentLocation;
        int start = findObstruction.apply(iter, (l) -> { return !world.isTileEmpty(l); });

        this.currentLocation = this.path.get(start);
        int end = findObstruction.apply(iter, (l) -> { return world.isTileEmpty(l); });

        if(start <= 0 || end == -1 || start == end) return false;
        start -= 1;
        end += 1;

        this.setLocation(this.path.get(start));

        final boolean result = this.findPathToLocation(this.path.get(end), world);
        this.setLocation(previousCurrent);
        return result;
    }

    /**
     * Returns the current path created by the path finder as a queue
     *
     * @return the current path as queue
     */
    public Queue<Location> getPath() {
        return (Queue<Location>)this.path;
    }

    /**
     * Sets the current location of the PathFinder.
     * The current location is used by the path finder as the starting location
     *
     * @param location - current location or starting location of path finder
     */
    public void setLocation(Location location) {
        this.currentLocation = location;
    }

    /**
    * After BFS, the actual path needs to be traced out
    * This simply hops through the hashmap constructed by the BFS algorithm
    * to re-create the shortest path
    *
    * @param map - The hashmap constructed by BFS(findPath method)
    * @param end - The final Location the BFS ended on
    */
    private void traceRoute(HashMap<Location, Location> map, Location end) {
        ArrayList<Location> pathBuilder = new ArrayList<>();
        pathBuilder.add(end);

        Location startLocation = this.currentLocation;
        while( !map.get(Utilities.getLast(pathBuilder)).equals(startLocation) )  {
            pathBuilder.add( map.get( Utilities.getLast(pathBuilder) ));
        }

        // Path generated is backwards, we need to reverse it
        for(int i = pathBuilder.size() - 1; i >= 0; i--) {
            this.path.add(pathBuilder.get(i));
        }

    }

    /** 
     * BFS implementation that finds shortest route given that currentLocation
     * is set and a goal location is given
     *
     * @param goal - The goal location that you're looking to find a route to
     * @param world - Reference to the world
     * @return whether if a path was found or not
     */
    public boolean findPath(Function<Location, Boolean> condition, World world) {
        this.path.clear();
        // No starting point is set
        if(this.currentLocation == null) return false;

        // Queue contains the next location to visit
        Queue<Location> queue = new LinkedList<>();
        // pathTracer is used for two things:
        // 1. to keep track of how the BFS algorithm reached each location, which can then be used to re-construct the path
        // 2. it is used as a visited set to prevent cycles
        HashMap<Location, Location> pathTracer = new HashMap<>();
        pathTracer.put(this.currentLocation, this.currentLocation); // Mark start location as visisted

        queue.add(this.currentLocation);
        while(!queue.isEmpty()) {
            Location focusedLocation = queue.poll();

            // condition is a lambda function that checks if the focused location is the goal
            if(condition.apply(focusedLocation)) {
                // Goal is found, now re-trace the path taken
                this.traceRoute(pathTracer, focusedLocation);
                return true;
            }
            for(Location neighbour : world.getEmptySurroundingTiles(focusedLocation) ) {
                // pathTracer is both used to trace out the paths taken AND it doubles as a visited set
                if(!pathTracer.containsKey(neighbour)) {
                    pathTracer.put(neighbour, focusedLocation);
                    queue.add(neighbour);
                }
            }
        }

        return false;
    }

    /**
     * Determining if the given location is in fact the final point of the current path
     *
     * @param location - The location we want to be the final location
     * @return whether if the location given is the final location
     */
    public boolean isFinalLocationInPath(Location location) {
        if( this.path.getLast().equals(location) ) return true;
        return false;
    }

    /**
     * Returns final location in path
     *
     * @return final location in path
     */
    public Location getFinalLocationInPath() {
        return this.path.getLast();
    }

    /**
     * Simply resets the path and deletes every location in the path
     */
    public void clearPath() {
        this.path.clear();
    }

}
