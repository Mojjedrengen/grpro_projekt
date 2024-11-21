package simulator.util;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.*;

//import simulator.util.Utilities;

public class PathFinder {

    private LinkedList<Location> path;
    private Location currentLocation;

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
     * Wrapper around findPath() method. Used to find path to specific instance of something
     *
     * @param type - The type of object you're looking to find the closest route to
     * @param world - Reference to the world
     * @return whether if a path was found or not
     */
    public <T> boolean findPathToNearest(Class<T> type, World world) {
        return this.findPath
        ( (location) -> { 
            Object obj = world.getTile(location);
            if(obj != null && type.isInstance(obj)) return true;
            return false;
        }, world);
    }

    public boolean isPathStillValid(World world) {

        for(Location loc : this.path) {
            if(!world.isTileEmpty(loc)) return false;
        }

        return true;
    }

    public boolean hasPath() {
        return !this.path.isEmpty();
    }

    public void fixObstructedPath(World world) {

        // Find obstruction
        //Location obstructionStart = null, obstructionEnd = null;

    }

    public Queue<Location> getPath() {
        return (Queue<Location>)this.path;
    }

    public void setLocation(Location location) {
        this.currentLocation = location;
    }

    /**
    * After BFS, the actual path needs to be traced out
    * This simply hops through the hashmap constructed by the BFS algorithm
    * to re-create the shortest path
    *
    * @param map - The hashmap constructed by BFS(findPathToLocation)
    * @param end - The final Location the BFS ended on
    */
    private void traceRoute(HashMap<Location, Location> map, Location end) {
        LinkedList<Location> pathBuilder = new LinkedList<>();
        pathBuilder.add(end);

        Location startLocation = this.currentLocation;
        while( !map.get(pathBuilder.getLast()).equals(startLocation) )  {
            pathBuilder.add( map.get( pathBuilder.getLast() ));
        }

        for(int i = pathBuilder.size() - 1; i >= 0; i--) {
//            System.out.println(pathBuilder.get(i));
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
    private boolean findPath(Function<Location, Boolean> condition, World world) {
        this.path.clear();

        Queue<Location> queue = new LinkedList<>();
        HashMap<Location, Location> pathTracer = new HashMap<>();

        queue.add(this.currentLocation);
        while(!queue.isEmpty()) {
            Location focusedLocation = queue.poll();

            // Goal is found, now re-trace the path taken
            //if(Utilities.locationEquals(focusedLocation, goal)) {
            if(condition.apply(focusedLocation)) {
                this.traceRoute(pathTracer, focusedLocation);
                return true;
            }
            for(Location neighbour : world.getEmptySurroundingTiles(focusedLocation) ) {
                // pathTracer is both used to trace out the paths taken AND it doubles as a visited set
                if(!pathTracer.containsKey(neighbour)) {
                    queue.add(neighbour);
                    pathTracer.put(neighbour, focusedLocation);
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

}
