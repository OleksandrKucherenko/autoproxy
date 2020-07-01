package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.olku.annotations.RetRx;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/** RxJava return values generator. */
public class RetRx3Generator implements ReturnsPoet {
    private static final boolean IS_DEBUG = false;

    /** Base classes supported by rxJava v2.xx */
    private static final Class<?>[] BASE_CLASSES = {
            Flowable.class,
            Observable.class,
            Single.class,
            Completable.class,
            Maybe.class
    };

    @NonNull
    public static RetRx3Generator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable @RetRx final String type,
                           @NonNull final MethodSpec.Builder builder) {
        final Class<?> rxType = resolveReturnType(returnType);

        if (RetRx.EMPTY.equals(type)) {
            //    io.reactivex.Flowable.empty();
            //    io.reactivex.Observable.empty();
            //    io.reactivex.Single.fromObservable(Observable.empty()); == io.reactivex.Single.error(...)
            //    io.reactivex.Completable.fromObservable(Observable.empty()); == io.reactivex.Completable.complete();
            //    io.reactivex.Maybe.empty();
            if (rxType.isAssignableFrom(Single.class)) {
                builder.addComment("will be equivalent to: `io.reactivex.Single.error(...)`");
                builder.addStatement("return $T.fromObservable($T.empty())", rxType, Observable.class);
            } else if (rxType.isAssignableFrom(Completable.class)) {
                builder.addComment("will be equivalent to: `io.reactivex.Completable.complete()`");
                builder.addStatement("return $T.fromObservable($T.empty())", rxType, Observable.class);
            } else {
                builder.addStatement("return $T.empty()", rxType);
            }
            return true;
        }

        if (RetRx.ERROR.equals(type)) {
            //    io.reactivex.Flowable.error(new UnsupportedOperationException("unsupported method call"));
            //    io.reactivex.Observable.error(new UnsupportedOperationException("unsupported method call"));
            //    io.reactivex.Single.error(new UnsupportedOperationException("unsupported method call"));
            //    io.reactivex.Completable.error(new UnsupportedOperationException("unsupported method call"));
            //    io.reactivex.Maybe.error(new UnsupportedOperationException("unsupported method call"));
            builder.addStatement("return $T.error(new $T($S))",
                    rxType,
                    UnsupportedOperationException.class,
                    "unsupported method call");

            return true;
        }

        if (RetRx.NEVER.equals(type)) {
            //    io.reactivex.Flowable.never();
            //    io.reactivex.Observable.never();
            //    io.reactivex.Single.never();
            //    io.reactivex.Completable.never();
            //    io.reactivex.Maybe.never();
            builder.addStatement("return $T.never()", rxType);
            return true;
        }

        return false;
    }

    @NonNull
    /* package */ static Class<?> resolveReturnType(@NonNull final Type returnType) {
        final String returnTypeName = returnType.toString();
        if(IS_DEBUG) System.out.print("type: " + returnTypeName);

        for (Class<?> clazz : BASE_CLASSES) {
            final String name = clazz.getCanonicalName();
            if (returnTypeName.startsWith(name)) {
                if(IS_DEBUG) System.out.println(" resolve to: " + name);
                return clazz;
            }
        }

        if(IS_DEBUG) System.out.println(" fallback to: io.reactivex.Observable");

        return Observable.class;
    }

    private static final class Singleton {
        /* package */ static final RetRx3Generator INSTANCE = new RetRx3Generator();
    }
}
