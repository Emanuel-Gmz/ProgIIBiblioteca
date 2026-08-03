package exceptions;

public class AutorException extends RuntimeException {
    public AutorException(String message) {
        super(message);
    }

    public AutorException(String message, Throwable cause) {super(message, cause);}
}
