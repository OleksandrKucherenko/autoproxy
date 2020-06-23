package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

/** RxJava return values generator. */
public class JustRxGenerator implements ReturnsPoet {
    @NonNull
    public static JustRxGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable final String type,
                           @NonNull final MethodSpec.Builder builder) {
        if (null != type && type.length() > 0) {
            builder.addStatement("return $T.just($L)", rx.Observable.class, type);
            return true;
        }

        return false;
    }

    private static final class Singleton {
        /* package */ static final JustRxGenerator INSTANCE = new JustRxGenerator();
    }
}
