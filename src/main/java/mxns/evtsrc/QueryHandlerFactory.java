package mxns.evtsrc;

import mxns.function.AsyncFunction;

import java.util.function.BiFunction;

class QueryHandlerFactory<P, I, C, H, R> {
    private final ConnectionPool<P> connectionPool;
    private final HandlerFactory<I, C, H, R> handlerFactory;

    QueryHandlerFactory(
            HandlerFactory<I, C, H, R> handlerFactory,
            ConnectionPool<P> connectionPool
    ) {
        this.handlerFactory = handlerFactory;
        this.connectionPool = connectionPool;
    }

    AsyncFunction<I, R> createHandler(BiFunction<P, C, AsyncFunction<H, R>> handlerFactory) {
        return this.handlerFactory.createHandler(context -> message ->
                connectionPool.withConnection(
                        connection -> handlerFactory.apply(connection, context).handle(message))
        );
    }
}
