package exceptions;

public class CategoriaException extends RuntimeException {
    public CategoriaException(String message) {
        super(message);
    }
    public CategoriaException(String message, Throwable cause) {super(message, cause);}
}
