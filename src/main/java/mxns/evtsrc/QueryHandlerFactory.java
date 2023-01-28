package mxns.evtsrc;

import mxns.function.AsyncFunction;

import java.util.function.Function;

class QueryHandlerFactory<P, I, H, C, R> {
    private final ConnectionManager<P> poolManager;
    private final HandlerFactory<I, H, C, R> handlerFactory;

    QueryHandlerFactory(
            HandlerFactory<I, H, C, R> handlerFactory,
            ConnectionManager<P> poolManager
    ) {
        this.handlerFactory = handlerFactory;
        this.poolManager = poolManager;
    }

    AsyncFunction<I, R> createQueryHandler(Function<P, AsyncFunction<H, R>> handlerFactory) {
        return this.handlerFactory.createHandler(
                ctx -> query ->
                        poolManager.withConnection(
                                connection -> {
                                    AsyncFunction<H, R> handler = handlerFactory.apply(connection);
                                    return handler.handle(query);
                                })
        );
    }
}
