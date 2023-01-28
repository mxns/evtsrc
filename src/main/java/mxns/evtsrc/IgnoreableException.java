package mxns.evtsrc;

public class IgnoreableException extends RuntimeException {
    public IgnoreableException(Throwable error) {
        super(error);
    }
}
