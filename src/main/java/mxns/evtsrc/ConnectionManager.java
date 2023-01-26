package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.transport.AsyncHandler;

public interface ConnectionManager<P> {
    <X> Promise<X> withConnection(AsyncHandler<P, X> function);
    <X> Promise<X> withTransaction(AsyncHandler<P, X> function);
}
