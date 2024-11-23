package simulator.actors.exceptions;

/**
 * Currently unsused exception.
 *
 * Used to show that the dynamic type of a certain animal(actor) could not be resolved 
 */
public class UnknownActorException extends RuntimeException {

    private String unknownActor;

    public UnknownActorException(String unknownActor) {
        super("Unknown actor: " + unknownActor);
        this.unknownActor = unknownActor;
    }

    public String getUnknownActor() {
        return unknownActor;
    }
}
