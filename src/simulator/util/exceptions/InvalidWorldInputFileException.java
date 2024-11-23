package simulator.util.exceptions;

/** 
 * This exeption is thrown by the WorldLoader class in the event that there's
 * a syntax error in the world input file. This exception exists to provide
 * more verbose error logging. It will keep track of the line number of the
 * syntax error and also hold onto the offending line in the input file.
 */
public class InvalidWorldInputFileException extends RuntimeException {

    private int lineNumber;
    private String invalidLine;

    public InvalidWorldInputFileException(String invalidLine, int lineNumber) {
        super("Invalid input on line number (" + lineNumber + "): " + invalidLine);
        this.lineNumber = lineNumber;
        this.invalidLine = invalidLine;
    }

    public InvalidWorldInputFileException(String message, String invalidLine, int lineNumber) {
        super("Invalid input on line number (" + lineNumber + "): " + invalidLine + "\n" + message);
        this.lineNumber = lineNumber;
        this.invalidLine = invalidLine;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getInvalidLine() {
        return this.invalidLine;
    }

}
