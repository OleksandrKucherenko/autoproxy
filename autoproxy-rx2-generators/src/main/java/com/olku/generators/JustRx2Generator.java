package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import io.reactivex.Completable;

/** RxJava return values generator. */
public class JustRx2Generator implements ReturnsPoet {
    @NonNull
    public static JustRx2Generator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable final String type,
                           @NonNull final MethodSpec.Builder builder) {
        System.out.print("just '" + type + "' ");
        final Class<?> rxType = RetRx2Generator.resolveReturnType(returnType);

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
        /* package */ static final JustRx2Generator INSTANCE = new JustRx2Generator();
    }
}
