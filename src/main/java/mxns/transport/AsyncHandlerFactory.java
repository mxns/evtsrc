package mxns.transport;

@FunctionalInterface
public interface AsyncHandlerFactory<C, H, R> {
    AsyncHandler<H, R> get(C context);
}
