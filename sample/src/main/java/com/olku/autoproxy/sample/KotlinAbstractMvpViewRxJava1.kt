package com.olku.autoproxy.sample

import android.net.Uri
import com.olku.annotations.*
import com.olku.generators.JustRxGenerator
import com.olku.generators.RetRxGenerator
import rx.*
import rx.Observable
import java.util.*

@AutoProxy(flags = AutoProxy.Flags.CREATOR)
abstract class KotlinAbstractMvpViewRxJava1 {
    /** Returns NULL if predicate returns False. */
    @AutoProxy.Yield(Returns.NULL)
    abstract fun dummyCall(): Observable<Boolean?>?

    /** Does direct call independent to predicate result. */
    @AutoProxy.Yield(Returns.DIRECT)
    abstract fun dispatchDeepLink(deepLink: Uri): Boolean

    @AutoProxy.Yield(Returns.THIS)
    abstract fun chainedCall(): KotlinAbstractMvpViewRxJava1

    @AutoProxy.Yield(Returns.SKIP)
    abstract fun chainedCallSkip(): KotlinAbstractMvpViewRxJava1

    /** Throws exception on False result from predicate. */
    @AutoProxy.Yield
    abstract fun dummyCall(message: String?, args: List<String?>?): Observable<Boolean?>?

    /** Returns ZERO on False result from predicate. */
    @AutoProxy.Yield(RetNumber.ZERO)
    abstract fun numericCall(): Double

    /** Returns FALSE on False result from predicate. */
    @AutoProxy.Yield(RetBool.FALSE)
    abstract fun booleanCall(): Boolean

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Empty {
        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.EMPTY)
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.EMPTY)
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.EMPTY)
        abstract fun completableMethod(): Completable
    }

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Error {
        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.ERROR)
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.ERROR)
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.ERROR)
        abstract fun completableMethod(): Completable
    }

    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Never {
        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.NEVER)
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.NEVER)
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.NEVER)
        abstract fun completableMethod(): Completable
    }

    /** Returns Observable.error(...) on False result from predicate. */
    @AutoProxy(flags = AutoProxy.Flags.CREATOR)
    abstract class Just {
        @AutoProxy.Yield(adapter = JustRxGenerator::class, value = "false")
        abstract fun observableMethod(): Observable<Boolean>

        @AutoProxy.Yield(adapter = JustRxGenerator::class, value = "false")
        abstract fun singleMethod(): Single<Boolean>

        @AutoProxy.Yield(adapter = JustRxGenerator::class, value = "ignored")
        abstract fun completableMethod(): Completable

        @AutoProxy.Yield(adapter = JustRxGenerator::class, value = "java.util.Collections.emptyMap()")
        abstract fun complexMethod(): Single<Map<String, Pair<String, Boolean>>>
    }

}