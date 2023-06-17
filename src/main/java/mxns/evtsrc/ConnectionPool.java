package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncFunction;

public interface ConnectionPool<C> {
    <X> Promise<X> withConnection(AsyncFunction<C, X> function);
    <X> Promise<X> withTransaction(AsyncFunction<C, X> function);
}
