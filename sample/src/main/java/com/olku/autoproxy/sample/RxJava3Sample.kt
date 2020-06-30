package com.olku.autoproxy.sample

import android.net.Uri
import com.olku.annotations.*
import com.olku.generators.JustRx3Generator
import com.olku.generators.RetRx3Generator
import io.reactivex.rxjava3.core.*

@AutoProxy(flags = AutoProxy.Flags.CREATOR)
abstract class RxJava3Sample {
    /** Returns NULL if predicate returns False. */
    @AutoProxy.Yield(Returns.NULL)
    abstract fun dummyCall(): Observable<Boolean?>?

    /** Returns Observable.empty() */
    @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.EMPTY)
    abstract fun dummyCall(generic: List<String?>?): Observable<Boolean?>?

    /** Throws exception on False result from predicate. */
    @AutoProxy.Yield
    abstract fun dummyCall(message: String?, args: List<String?>?): Observable<Boolean?>?

    /** Returns Observable.error(...) on False result from predicate. */
    @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
    abstract fun dummyCall(message: String?, vararg args: Any?): Observable<Boolean?>?

    /** Returns ZERO on False result from predicate. */
    @AutoProxy.Yield(RetNumber.ZERO)
    abstract fun numericCall(): Double

    /** Returns FALSE on False result from predicate. */
    @AutoProxy.Yield(RetBool.FALSE)
    abstract fun booleanCall(): Boolean

    /** Does direct call independent to predicate result. */
    @AutoProxy.Yield(Returns.DIRECT)
    abstract fun dispatchDeepLink(deepLink: Uri): Boolean

    /** Returns Observable.just(true) on False result from predicate. */
    @AutoProxy.Yield(adapter = JustRx3Generator::class, value = "true")
    abstract fun startHearthAnimation(): Observable<Boolean?>?

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Empty {
        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.EMPTY)
        abstract fun flowableMethod(): Flowable<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.EMPTY)
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.EMPTY)
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.EMPTY)
        abstract fun maybeMethod(): Maybe<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.EMPTY)
        abstract fun completableMethod(): Completable
    }

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Error {
        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
        abstract fun flowableMethod(): Flowable<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
        abstract fun maybeMethod(): Maybe<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
        abstract fun completableMethod(): Completable
    }

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Never {
        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.NEVER)
        abstract fun flowableMethod(): Flowable<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.NEVER)
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.NEVER)
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.NEVER)
        abstract fun maybeMethod(): Maybe<Boolean>

        @AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.NEVER)
        abstract fun completableMethod(): Completable
    }

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Just {
        @AutoProxy.Yield(adapter = JustRx3Generator::class, value = "false")
        abstract fun flowableMethod(): Flowable<Boolean>

        @AutoProxy.Yield(adapter = JustRx3Generator::class, value = "false")
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = JustRx3Generator::class, value = "false")
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = JustRx3Generator::class, value = "false")
        abstract fun maybeMethod(): Maybe<Boolean>

        @AutoProxy.Yield(adapter = JustRx3Generator::class, value = "ignored")
        abstract fun completableMethod(): Completable
    }
}