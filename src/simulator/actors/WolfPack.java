package simulator.actors;

import java.util.Set;
import java.util.HashSet;

import simulator.objects.holes.WolfHole;

public class WolfPack {

    protected Set<Wolf> members;
    protected WolfHole assignedHole;

    WolfPack() {
        this.members = new HashSet<>();
        this.assignedHole = null; // Something here
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

} 

