package com.olku.autoproxy.sample;

import android.net.Uri;

import com.olku.annotations.AutoProxy;
import com.olku.annotations.RetBool;
import com.olku.annotations.RetNumber;
import com.olku.annotations.RetRx;
import com.olku.annotations.Returns;
import com.olku.generators.JustRxGenerator;
import com.olku.generators.RetRxGenerator;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;

/** MVP view interface. */
@AutoProxy
public interface MvpView {
    /** Returns NULL if predicate returns False. */
    @AutoProxy.Yield(Returns.NULL)
    Observable<Boolean> dummyCall();

    /** Returns Observable.empty() */
    @AutoProxy.Yield(adapter = RetRxGenerator.class, value = RetRx.EMPTY)
    Observable<Boolean> dummyCall(final List<String> generic);

    /** Throws exception on False result from predicate. */
    @AutoProxy.Yield(/* Returns.THROWS */)
    Observable<Boolean> dummyCall(final String message, final List<String> args);

    /** Returns Observable.error(...) on False result from predicate. */
    @AutoProxy.Yield(adapter = RetRxGenerator.class, value = RetRx.ERROR)
    Observable<Boolean> dummyCall(final String message, final Object... args);

    /** Returns ZERO on False result from predicate. */
    @AutoProxy.Yield(RetNumber.ZERO)
    double numericCall();

    /** Returns FALSE on False result from predicate. */
    @AutoProxy.Yield(RetBool.FALSE)
    boolean booleanCall();

    /** Does direct call independent to predicate result. */
    @AutoProxy.Yield(Returns.DIRECT)
    boolean dispatchDeepLink(@NonNull final Uri deepLink);

    /** Returns Observable.just(true) on False result from predicate. */
    @AutoProxy.Yield(adapter = JustRxGenerator.class, value = "true")
    Observable<Boolean> startHearthAnimation();

}
