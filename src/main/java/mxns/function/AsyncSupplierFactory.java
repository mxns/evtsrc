package mxns.function;

@FunctionalInterface
public interface AsyncSupplierFactory<C, R> {
    AsyncSupplier<R> get(C context);
}
