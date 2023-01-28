package mxns.evtsrc;

import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import mxns.function.AsyncExceptionHandler;
import mxns.function.AsyncFunction;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SqlHandlerRegistry<P, I, H, C, R> {
    private final CommandHandlerFactory<P, I, H, C, R> commandHandlers;
    private final QueryHandlerFactory<P, I, H, C, R> queryHandlers;
    private final Registry<I> registry;
    private final String namespace;

    public SqlHandlerRegistry(
            When when,
            Registry<I> registry,
            String namespace,
            HandlerFactory<I, H, C, R> handlerFactory,
            ConnectionManager<P> poolManager,
            Function<I, C> contextFactory,
            Function<I, H> payloadExtractor,
            AsyncExceptionHandler<C, H> exceptionHandler
    ) {
        this.namespace = namespace;
        this.registry = registry;
        this.commandHandlers = new CommandHandlerFactory<>(contextFactory, payloadExtractor, exceptionHandler, poolManager) {
            @Override
            Promise<List<Event<R>>> insertEvents(P connection, List<Event<R>> events) {
                return when.resolve(events);
            }
        };
        this.queryHandlers = new QueryHandlerFactory<>(handlerFactory, poolManager);
    }

    public void registerCommandHandler(String address, BiFunction<P, C, AsyncFunction<H, List<Event<R>>>> handlerFactory) {
        AsyncFunction<I, List<Event<R>>> handler = commandHandlers.createHandler(handlerFactory);
        registry.register(addressOf(address), request -> handler.handle(request)
                .then(events -> {
                    // publish events
                    return null;
                }));
    }

    public void registerQueryHandler(String address, Function<P, AsyncFunction<H, R>> handlerFactory) {
        AsyncFunction<I, R> handler = queryHandlers.createQueryHandler(handlerFactory);
        registry.register(addressOf(address), handler);
    }

    private String addressOf(String address) {
        if (Objects.isNull(namespace) || namespace.isBlank()) {
            return address;
        }
        return namespace + "." + address;
    }
}
