package mxns.function;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncSupplier<T> {
    Promise<T> get();
}
