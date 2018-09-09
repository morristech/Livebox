package com.creations.livebox.adapters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;

/**
 * @author Sérgio Serra.
 * sergioserra99@gmail.com
 * <p>
 * Converts an {@link Observable} to {@link LiveData} instance using {@link LiveDataReactiveStreams}
 */
public class LiveDataAdapter<T> implements ObservableAdapter<T, LiveData<T>> {

    @Override
    public LiveData<T> adapt(Observable<T> observable) {
        return LiveDataReactiveStreams.fromPublisher(observable.toFlowable(BackpressureStrategy.BUFFER));
    }

}
