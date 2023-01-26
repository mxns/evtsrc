package mxns.evtsrc;

import mxns.transport.AsyncExceptionHandler;
import mxns.transport.AsyncHandler;
import mxns.transport.Registry;

import java.util.Objects;
import java.util.function.Function;

public class EventHandlerRegistry<I, H, C> {
    private final EventHandlerFactory<I, H, C> eventHandlers;
    private final Registry<I> registry;
    private final String namespace;

    public EventHandlerRegistry(
            Function<I, C> contextFactory,
            Function<I, Event<H>> payloadExtractor,
            AsyncExceptionHandler<C, Event<H>> exceptionHandler,
            Registry<I> registry,
            String namespace
    ) {
        this.namespace = namespace;
        this.registry = registry;
        this.eventHandlers = new EventHandlerFactory<>(contextFactory, payloadExtractor, exceptionHandler);
    }

    public void registerEventHandler(String address, Function<C, EventHandler<H>> handlerFactory) {
        AsyncHandler<I, Void> handler = eventHandlers.createHandler(ctx -> m -> handlerFactory.apply(ctx).handle(m));
        registry.register(addressOf(address), handler);
    }

    private String addressOf(String address) {
        if (Objects.isNull(namespace) || namespace.isBlank()) {
            return address;
        }
        return namespace + "." + address;
    }
}
