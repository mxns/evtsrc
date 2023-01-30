package mxns.function;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncBiConsumer<T, U> {
    Promise<Void> accept(T t, U u);
}
