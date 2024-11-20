package simulator.util.exceptions;

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
