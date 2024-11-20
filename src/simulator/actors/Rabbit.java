package simulator.actors;

import java.util.List;
import itumulator.world.World;
import itumulator.world.Location;

import simulator.util.PathFinder;
import simulator.objects.Grass;

public class Rabbit extends Animal {

    private Location assignedHole; // The hole assigned to this rabbit
    private PathFinder pathFinder;

    public Rabbit(int startEnergy) {
        super(startEnergy); // Call the Animal superclass constructor
        this.assignedHole = null; // No hole assigned initially
        // PathFinder expects starting location, setting to null for now
        this.pathFinder = new PathFinder(null);
    }


    // NOTE: Current implementation of WorldLoader cannot handle constructor arguments.
    // Will be fixed soon. For the meanwhile, give default energy levels to all animals
    // and have a constructor with no arguments
    public Rabbit() {
        super(100); // Call the Animal superclass constructor
        this.assignedHole = null; // No hole assigned initially
        // PathFinder expects starting location, setting to null for now
        this.pathFinder = new PathFinder(null);
    }

    // Check if the rabbit has an assigned hole
    public boolean hasHole() {
        return assignedHole != null;
    }

    // Assign a hole to the rabbit
    public void assignHole(Location holeLocation) {
        this.assignedHole = holeLocation;
    }

    // Move towards the assigned hole
    public void goHole(World world) {
        if (!hasHole()) { //Checks if rabit has a hole to return to
            return;
        }

        Location currentLocation = world.getLocation(this);
        if (currentLocation.equals(assignedHole)) { //Checks if rabbit is already at hole
            return;
        }
        //if statement to only go towards hole if it's evening time...
        // Find path to the hole and move one step towards it

        this.pathFinder.setLocation(currentLocation);
        Location nextStep = null;
        if(this.pathFinder.hasPath() && this.pathFinder.isFinalLocationInPath(this.assignedHole) ) {
            nextStep = (this.pathFinder.getPath()).poll(); // Move closer to goal
        }else { // Path hasn't been found yet

            // findPathToLocation returns false if no route was found
            if(!this.pathFinder.findPathToLocation(this.assignedHole, world)) {
                // No route to hole
                return;
            }
            nextStep = this.pathFinder.getPath().poll(); // Move closer to goal
        }

        if(world.isTileEmpty(nextStep)) {
            world.move(this, nextStep); // Move rabbit to the next step
        } else {
            //Path is blocked
            // pathFinder obstruction fixer method is underway!
            // pathFinder.fixObstructedPath();
        }
    }

    @Override
    public void act(World world) {
        // Rabbit-specific behavior
        if (hasHole() && world.isNight()) {
            goHole(world); // Try moving towards its hole if it has one
        } else {
            wander(world); // Wander randomly if no hole
        }

        eat(world); // have eat before hole assignment so it can make tile empty for easier tilecheck
        if(world.getNonBlocking(world.getLocation(this)) == null || hasHole() == true) {
            assignHole(world.getLocation(this)); //if spot is empty and if they dont already have a hole
        }
        decreaseEnergy(1, world); // Lose 1 energy per act step
    }

    @Override
    public void eat(World world) {
        Location currentLocation = world.getLocation(this);
        if(world.getNonBlocking(currentLocation) instanceof Grass) {
            //Grass eating method from grass class??
            // TODO
//            Grass.consume();
            //Increase energy??
        } else {
            //no grass to eat :(
        }
    }

}
