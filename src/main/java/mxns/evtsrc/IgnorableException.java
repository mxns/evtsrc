package mxns.evtsrc;

public class IgnorableException extends RuntimeException {
    public IgnorableException(Throwable error) {
        super(error);
    }
}
