package mxns.transport;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncSupplier<R> {
    Promise<R> get();
}
