package simulator.actors;

import itumulator.world.Location;
import itumulator.world.World;
import java.util.Random;
import java.util.Set;
import simulator.objects.Grass;
import simulator.objects.NonBlockable;
import simulator.objects.RabbitHole;
import simulator.util.PathFinder;

public class Rabbit extends Animal {

    private RabbitHole assignedHole; // The hole assigned to this rabbit
    private PathFinder pathFinder;

    public Rabbit() {
        super(20, 9); // Call the Animal superclass constructor
        this.assignedHole = null; // No hole assigned initially
        // PathFinder expects starting location, setting to null for now
        this.pathFinder = new PathFinder(null);
    }

    // Check if the rabbit has an assigned hole
    public boolean hasHole() {
        return assignedHole != null;
    }

    public boolean isInHole() {
        if(this.assignedHole == null) return false;
        return this.assignedHole.getInhabitants().contains(this);
    }

    @Override
    public void reproduce(World world) {
        Random random = new Random();
        if(this.assignedHole.getInhabitants().size() > 1) {
            if(random.nextInt(20) == 5) {
                this.assignedHole.animalAdd(new Rabbit());
            }
        }
    }

    // Assign a hole to the rabbit
    public void assignHole(RabbitHole rabbitHole) {
        this.assignedHole = rabbitHole;
    }

    // Move towards the assigned hole
    public void goHole(World world) {
        if (!hasHole()) { //Checks if rabit has a hole to return to
            return;
        }else if( assignedHole.getInhabitants().contains(this) ) {
            // Rabbit is already in its hole
            return;
        }

        Location currentLocation = world.getLocation(this);
        Location rabbitHoleLocation = assignedHole.getLocation(world);
        if (currentLocation.equals(rabbitHoleLocation)) { // Rabbit enters hole
            assignedHole.animalEnters(this);
            world.remove(this);
            return;
        }
        //if statement to only go towards hole if it's evening time...
        // Find path to the hole and move one step towards it

        this.pathFinder.setLocation(currentLocation);
        Location nextStep = null;
        if(pathFinder.hasPath() && 
           pathFinder.isFinalLocationInPath(rabbitHoleLocation) ) {

            nextStep = (this.pathFinder.getPath()).poll(); // Move closer to goal
        }else { // Path hasn't been found yet

            // findPathToLocation returns false if no route was found
            if(!this.pathFinder.findPathToLocation(rabbitHoleLocation, world)) {
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

    private void exitHole(World world) {
        if (this.assignedHole != null) {
            this.assignedHole.exitRabbit(this, world);
        }
    }

    private void tryToMakeHole(World world) {

        Location currentLocation = world.getLocation(this);
        if(world.containsNonBlocking(currentLocation)) return;

        Random random = new Random();
        if(random.nextInt(101) <= 95) return;

        // TODO find a way to add this to our WorldLoader list
        RabbitHole rabbitHole = new RabbitHole();
        world.setTile(currentLocation, rabbitHole);
        this.assignHole(rabbitHole);
    }

    @Override
    public void act(World world) {
        // Rabbit-specific behavior
        if(world.isNight()) {
            if(this.hasHole()) this.goHole(world); // Try moving towards its hole if it has one
            if(this.isInHole()) {
                this.reproduce(world);
            }
            else {
                this.tryToMakeHole(world);
                if(!this.hasHole()) this.wander(world);
            }
        }else if( this.isInHole() && world.isDay() ) {
            // Try to exit hole since it's day again
            this.exitHole(world);
        }else {
            this.wander(world); // Wander randomly if no hole
        }

        if( !this.isInHole() ) {
            this.eat(world); // have eat before hole assignment so it can make tile empty for easier tilecheck
            
            // Bad practice by using instanceof, we have disapointed Claus, but this will have to do for now
            // Potential fix would be keeping a separate list containing all rabbit holes in the world
            Location currentLocation = world.getLocation(this);
            if(world.containsNonBlocking(currentLocation)) {
                NonBlockable nonBlock = (NonBlockable)world.getNonBlocking(world.getLocation(this));
                if(nonBlock instanceof RabbitHole rabbitHole) {
                    this.assignHole(rabbitHole);
                }
            }
        }

        // Lose amount of energy corresponding to the rabbits age
        // As the rabbit ages, it loses energy faster
        // Lose 10 extra if the rabbit hasn't eaten at all today
        if(world.getCurrentTime() == 0) {
            // Decrease energy also causes aging
            this.decreaseEnergy(this.getAge() + (this.hasEatenToday ? 0 : 10), world); 
            this.resetHunger();
            System.out.println("Energy levels end of day: " + this.getEnergy());
        }

    }

    @Override
    public void eat(World world) {
        Location currentLocation = world.getLocation(this);

        if(world.containsNonBlocking(currentLocation)) {
            NonBlockable nonBlockable = (NonBlockable)world.getNonBlocking(currentLocation);
            if(nonBlockable instanceof Grass grass) {
                grass.consume(world);
                this.hasEatenToday = true;
                this.increaseEnergy(1);
            }
        }
    }

}
