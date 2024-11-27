package simulator.actors;

import java.util.Set;
import java.util.HashSet;

import itumulator.world.World;

import simulator.objects.holes.WolfHole;
import simulator.util.Utilities;

public class WolfPack {

    protected Set<Wolf> members;
    protected WolfHole assignedHole;

    public WolfPack(WolfHole assignedHole) {
        super();
        this.members = new HashSet<>();
        this.assignedHole = assignedHole;
    }

    public WolfPack(World world) {
        this(new WolfHole());
        world.setTile(Utilities.getRandomEmptyNonBlockingLocation(world, world.getSize()), this.assignedHole);
    }

    public Set<Wolf> getMembers() {
        return this.members;
    }

    public WolfHole getHole() {
        return this.assignedHole;
    }

    public void addMember(Wolf wolf) {
        this.members.add(wolf);
    }

    public boolean removeMember(Wolf wolf) {
        if(!this.members.contains(wolf)) return false;

        this.members.remove(wolf);
        return true;
    }

} 

