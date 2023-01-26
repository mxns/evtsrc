package mxns.transport;

import com.englishtown.promises.HandlerState;
import com.englishtown.promises.Promise;
import com.englishtown.promises.State;
import com.englishtown.promises.When;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AsyncExecutor<T> implements AsyncHandler<Collection<T>, Void> {
    private final When when;
    private final AsyncHandler<T, Void> executor;

    public AsyncExecutor(When when, AsyncHandler<T, Void> executor) {
        this.when = when;
        this.executor = executor;
    }

    @Override
    public Promise<Void> handle(Collection<T> collection) {
        List<Promise<Void>> promises = collection.stream()
                .map(executor::handle)
                .collect(Collectors.toList());
        return when.settle(promises)
                .then(result -> {
                    handleResult(result);
                    return null;
                });
    }

    private void handleResult(List<State<Void>> results) {
        List<Throwable> rejected = results.stream()
                .filter(state -> state.getState().equals(HandlerState.REJECTED))
                .map(State::getReason).toList();
        if (rejected.size() > 0) {
            RuntimeException e = new RuntimeException("Error happened");
            rejected.forEach(e::addSuppressed);
            throw e;
        }
    }

    public Promise<List<T>> settle(List<Promise<T>> list) {
        return when.settle(list)
                .then(results -> {
                    List<T> fulfilled = results.stream()
                            .filter(state -> state.getState().equals(HandlerState.FULFILLED))
                            .map(State::getValue)
                            .collect(Collectors.toList());
                    List<Throwable> rejected = results.stream()
                            .filter(state -> state.getState().equals(HandlerState.REJECTED))
                            .map(State::getReason).toList();
                    return when.resolve(fulfilled);
                });
    }
}
