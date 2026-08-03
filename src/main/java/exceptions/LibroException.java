package exceptions;

public class LibroException extends RuntimeException {
    public LibroException(String message) {
        super(message);
    }

    // Constructor que recibe el mensaje y la causa original (ej. una SQLException)
    public LibroException(String message, Throwable cause) {
        super(message, cause);
    }
}
