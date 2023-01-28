package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncFunction;

public interface ConnectionManager<P> {
    <X> Promise<X> withConnection(AsyncFunction<P, X> function);
    <X> Promise<X> withTransaction(AsyncFunction<P, X> function);
}
