package be.mormont.iacf.boncom.exceptions;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class FxmlLoadingException extends RuntimeException {
    public FxmlLoadingException() { super(); }
    public FxmlLoadingException(String s) { super(s); }
    public FxmlLoadingException(Throwable t) { super(t); }
}
