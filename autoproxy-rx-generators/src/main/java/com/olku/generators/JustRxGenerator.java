package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import rx.Completable;

/** RxJava return values generator. */
public class JustRxGenerator implements ReturnsPoet {
    private static final boolean IS_DEBUG = false;

    @NonNull
    public static JustRxGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable final String type,
                           @NonNull final MethodSpec.Builder builder) {
        if(IS_DEBUG) System.out.print("just '" + type + "' ");
        final Class<?> rxType = RetRxGenerator.resolveReturnType(returnType);

        if (null != type) {
            //    rx.Observable.just(...);
            //    rx.Single.just(...);
            //    rx.Completable.complete();
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
        /* package */ static final JustRxGenerator INSTANCE = new JustRxGenerator();
    }
}
