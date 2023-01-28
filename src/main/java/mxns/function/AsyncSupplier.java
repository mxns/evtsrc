package mxns.function;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncSupplier<R> {
    Promise<R> get();
}
