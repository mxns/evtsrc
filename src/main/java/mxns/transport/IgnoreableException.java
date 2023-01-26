package mxns.transport;

public class IgnoreableException extends RuntimeException {
    public IgnoreableException(Throwable error) {
        super(error);
    }
}
