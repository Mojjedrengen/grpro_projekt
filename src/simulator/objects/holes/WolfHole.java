package simulator.objects.holes;

import java.util.Set;
import java.util.Random;
import java.awt.Color;

import itumulator.world.World;
import itumulator.world.Location;
import itumulator.executable.DisplayInformation;
import itumulator.executable.DynamicDisplayInformationProvider;

import simulator.actors.Animal;
import simulator.actors.Wolf;


public class WolfHole extends Hole {

    private boolean hasAttemptedToReproduce;

    public WolfHole() {
        super();
        this.hasAttemptedToReproduce = false;
    }

    /**
     * Makes wolf enter hole.
     * NOTE: We have some redundancy across RabbitHole And WolfHole, let's fix that at some point.
     *
     * @param wolf reference to the wolf entering the hole
     * @param world reference to the world
     */
    public void wolfEnter(Wolf wolf, World world) {
        // Wolf is already in the hole
        if(wolf.isInHole()) return;
        Location holeLocation = super.getLocation(world);
        
        // Can't enter hole unless above it
        if(!holeLocation.equals(world.getLocation(wolf))) return;

        world.remove(wolf);
        super.animalAdd(wolf);

    }

    /**
     * Makes wolf exit hole.
     * NOTE: We have some redundancy across RabbitHole And WolfHole, let's fix that at some point.
     *
     * @param wolf reference to the wolf leaving the hole
     * @param world reference to the world
     */
    public void wolfExit(Wolf wolf, World world) {
        // Wolf isn't in the hole to begin with
        if(!wolf.isInHole()) return;

        Location holeLocation = super.getLocation(world);

        // If nothing is above the hole, then simply exit above the hole
        if(world.isTileEmpty(holeLocation)) {
            world.setTile(holeLocation, wolf);
            world.setCurrentLocation(holeLocation);
            super.animalRemove(wolf);
        }else { // Otherwise, attempt to exit onto surrounding tile
            Set<Location> surroundingLocations = world.getEmptySurroundingTiles(holeLocation);
            // Hole exit is blocked
            if(surroundingLocations.size() == 0) return;

            final Location exitLocation = surroundingLocations.iterator().next();
            world.setCurrentLocation(exitLocation);
            world.setTile(exitLocation, wolf);
            super.animalRemove(wolf);

        }

        // Wolves only exit once it's day.
        // We can therefore assume that the night is over once wolves start leaving
        this.hasAttemptedToReproduce = false;
    }

    /**
     * Checks if reproduction has been attmpted in this hole
     * @return if reproduction has been attempted in this wolf hole
     */
    public boolean reproductionAttempted() {
        return this.hasAttemptedToReproduce;
    }

    /**
     * Resets reproduction attempt to false
     */
    public void resetReproductionAttempt() {
        this.hasAttemptedToReproduce = false;
    }

    public void reproduce(World world) {
        if(this.hasAttemptedToReproduce) return;
        else if(this.getInhabitants().size() < 2) return;
        else if( (new Random()).nextInt(10) != 0 ) return;

        Wolf newWolf = new Wolf();
        newWolf.joinWolfPack(((Wolf)this.getInhabitants().iterator().next()).getWolfPack());
        this.animalAdd(newWolf);

        this.hasAttemptedToReproduce = true;
    }

}

