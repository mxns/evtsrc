package mxns.evtsrc;

public class ConnectionContext<X, C> {
    private final X context;
    private final C connection;

    ConnectionContext(X context, C connection) {
        this.context = context;
        this.connection = connection;
    }

    public X getContext() {
        return context;
    }

    public C getConnection() {
        return connection;
    }
}
