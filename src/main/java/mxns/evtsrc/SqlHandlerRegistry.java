package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncExceptionHandler;
import mxns.function.AsyncFunction;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class SqlHandlerRegistry<P, I, H, C, R> {
    private final CommandHandlerFactory<P, I, H, C, R> commandHandlers;
    private final QueryHandlerFactory<P, I, H, C, R> queryHandlers;
    private final String namespace;

    public SqlHandlerRegistry(
            String namespace,
            ConnectionManager<P> poolManager,
            HandlerFactory<I, H, C, R> handlerFactory,
            Function<I, C> contextFactory,
            Function<I, H> payloadExtractor,
            AsyncExceptionHandler<C, H> exceptionHandler
    ) {
        this.namespace = namespace;
        this.commandHandlers = new CommandHandlerFactory<>(contextFactory, payloadExtractor, exceptionHandler, poolManager);
        this.queryHandlers = new QueryHandlerFactory<>(handlerFactory, poolManager);
    }

    abstract Promise<List<Event<R>>> insertEvents(P connection, List<Event<R>> events);

    abstract void registerHandler(String address, AsyncFunction<I, Event<R>> handler);

    public void registerCommandHandler(String address, BiFunction<P, C, AsyncFunction<H, List<Event<R>>>> handlerFactory) {
        AsyncFunction<I, List<Event<R>>> handler = commandHandlers.createHandler(
                (connection, context) -> message ->
                        handlerFactory
                                .apply(connection, context)
                                .handle(message)
                                .then(events -> insertEvents(connection, events))
        );
        registerHandler(addressOf(address), request ->
                handler.handle(request)
                        .then(events -> {
                            // publish events
                            return null;
                        })
        );
    }

    private String addressOf(String address) {
        if (Objects.isNull(namespace) || namespace.isBlank()) {
            return address;
        }
        return namespace + "." + address;
    }
}
