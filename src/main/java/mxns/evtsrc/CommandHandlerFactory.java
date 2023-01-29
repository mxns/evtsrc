package mxns.evtsrc;

import mxns.function.AsyncExceptionHandler;
import mxns.function.AsyncFunction;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandHandlerFactory<P, I, H, C, R> {
    private final ConnectionManager<P> poolManager;
    private final HandlerFactory<I, H, C, List<Event<R>>> handlerFactory;

    protected CommandHandlerFactory(
            HandlerFactory<I, H, C, List<Event<R>>> handlerFactory,
            ConnectionManager<P> poolManager
    ) {
        this.handlerFactory = handlerFactory;
        this.poolManager = poolManager;
    }

    public AsyncFunction<I, List<Event<R>>> createHandler(BiFunction<P, C, AsyncFunction<H, List<Event<R>>>> handlerFactory) {
        return this.handlerFactory.createHandler(context -> message ->
                poolManager.withTransaction(
                        connection -> {
                            AsyncFunction<H, List<Event<R>>> payloadHandler = handlerFactory.apply(connection, context);
                            return payloadHandler.handle(message);
                        }
                ));
    }
}
