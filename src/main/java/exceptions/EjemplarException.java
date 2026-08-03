package exceptions;

public class EjemplarException extends RuntimeException {
    public EjemplarException(String message) {
        super(message);
    }

    // Constructor que recibe el mensaje y la causa original (ej. una SQLException)
    public EjemplarException(String message, Throwable cause) {
        super(message, cause);
    }
}
