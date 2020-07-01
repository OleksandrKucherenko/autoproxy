package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import io.reactivex.rxjava3.core.Completable;

/** RxJava return values generator. */
public class JustRx3Generator implements ReturnsPoet {
    private static final boolean IS_DEBUG = false;

    @NonNull
    public static JustRx3Generator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable final String type,
                           @NonNull final MethodSpec.Builder builder) {
        if(IS_DEBUG) System.out.print("just '" + type + "' ");
        final Class<?> rxType = RetRx3Generator.resolveReturnType(returnType);

        if (null != type) {
            //    io.reactivex.Flowable.just(...);
            //    io.reactivex.Observable.just(...);
            //    io.reactivex.Single.just(...);
            //    io.reactivex.Completable.complete();
            //    io.reactivex.Maybe.just(...);
            if (rxType.isAssignableFrom(Completable.class)) {
                builder.addStatement("return $T.complete()", rxType);
            } else if (type.length() > 0) {
                builder.addStatement("return $T.just($L)", rxType, type);
            }

            return true;
        }

        return false;
    }

    private static final class Singleton {
        /* package */ static final JustRx3Generator INSTANCE = new JustRx3Generator();
    }
}
