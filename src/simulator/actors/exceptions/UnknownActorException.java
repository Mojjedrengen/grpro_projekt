package simulator.actors.exceptions;

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
