package exceptions;

public class PrestamoException extends RuntimeException {
    // Constructor básico con mensaje
    public PrestamoException(String message) {super(message);}

    // Constructor que recibe el mensaje y la causa original (ej. una SQLException)
    public PrestamoException(String message, Throwable cause) {
        super(message, cause);
    }
}
