package mxns.evtsrc;

import mxns.function.AsyncSupplier;

import java.util.List;
import java.util.function.BiFunction;

public class TaskHandlerFactory<P, C, R> {
    private final ConnectionPool<P> connectionPool;
    private final SupplierFactory<C, List<Event<R>>> handlerFactory;

    protected TaskHandlerFactory(
            SupplierFactory<C, List<Event<R>>> handlerFactory,
            ConnectionPool<P> connectionPool
    ) {
        this.handlerFactory = handlerFactory;
        this.connectionPool = connectionPool;
    }

    public AsyncSupplier<List<Event<R>>> createHandler(BiFunction<P, C, AsyncSupplier<List<Event<R>>>> handlerFactory) {
        return this.handlerFactory.createHandler(context -> () ->
                connectionPool.withTransaction(
                        connection -> handlerFactory.apply(connection, context).get()
                ));
    }
}
