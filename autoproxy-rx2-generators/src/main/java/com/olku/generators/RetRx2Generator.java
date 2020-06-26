package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.olku.annotations.RetRx;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import io.reactivex.*;

/** RxJava return values generator. */
public class RetRx2Generator implements ReturnsPoet {
    /** Base classes supported by rxJava v2.xx */
    private static final Class<?>[] BASE_CLASSES = {
            Flowable.class,
            Observable.class,
            Single.class,
            Completable.class,
            Maybe.class
    };

    @NonNull
    public static RetRx2Generator getInstance() {
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
        System.out.print("type: " + returnTypeName);

        for (Class<?> clazz : BASE_CLASSES) {
            final String name = clazz.getCanonicalName();
            if (returnTypeName.startsWith(name)) {
                System.out.println(" resolve to: " + name);
                return clazz;
            }
        }

        System.out.println(" fallback to: io.reactivex.Observable");

        return Observable.class;
    }

    private static final class Singleton {
        /* package */ static final RetRx2Generator INSTANCE = new RetRx2Generator();
    }
}
