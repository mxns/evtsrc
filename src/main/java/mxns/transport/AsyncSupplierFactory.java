package mxns.transport;

@FunctionalInterface
public interface AsyncSupplierFactory<C, R> {
    AsyncSupplier<R> get(C context);
}
