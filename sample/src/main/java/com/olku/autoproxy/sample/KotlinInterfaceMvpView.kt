package com.olku.autoproxy.sample

import android.net.Uri
import com.olku.annotations.*
import com.olku.generators.JustRxGenerator
import com.olku.generators.RetRxGenerator
import rx.Observable

@AutoProxy
interface KotlinInterfaceMvpView {
    /** Returns NULL if predicate returns False.  */
    @AutoProxy.Yield(Returns.NULL)
    fun dummyCall(): Observable<Boolean?>?

    /** Returns Observable.empty()  */
    @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.EMPTY)
    fun dummyCall(generic: List<String?>?): Observable<Boolean?>?

    /** Throws exception on False result from predicate.  */
    @AutoProxy.Yield
    fun dummyCall(message: String?, args: List<String?>?): Observable<Boolean?>?

    /** Returns Observable.error(...) on False result from predicate.  */
    @AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.ERROR)
    fun dummyCall(message: String?, vararg args: Any?): Observable<Boolean?>?

    /** Returns ZERO on False result from predicate.  */
    @AutoProxy.Yield(RetNumber.ZERO)
    fun numericCall(): Double

    /** Returns FALSE on False result from predicate.  */
    @AutoProxy.Yield(RetBool.FALSE)
    fun booleanCall(): Boolean

    /** Does direct call independent to predicate result.  */
    @AutoProxy.Yield(Returns.DIRECT)
    fun dispatchDeepLink(deepLink: Uri): Boolean

    /** Returns Observable.just(true) on False result from predicate.  */
    @AutoProxy.Yield(adapter = JustRxGenerator::class, value = "true")
    fun startHearthAnimation(): Observable<Boolean?>?

}