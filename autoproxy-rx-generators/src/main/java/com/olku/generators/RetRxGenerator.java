package com.olku.generators;

import com.olku.annotations.RetRx;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import androidx.annotation.NonNull;

/** RxJava return values generator. */
public class RetRxGenerator implements ReturnsPoet {
    @NonNull
    public static RetRxGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @RetRx final String type,
                           @NonNull final MethodSpec.Builder builder) {
        if (RetRx.EMPTY.equals(type)) {
//            rx.Observable.empty();
            builder.addStatement("return $T.empty()", rx.Observable.class);
            return true;
        }

        if (RetRx.ERROR.equals(type)) {
//            rx.Observable.error(new UnsupportedOperationException("unsupported method call"));
            builder.addStatement("return $T.error(new $T($S))",
                    rx.Observable.class,
                    UnsupportedOperationException.class,
                    "unsupported method call");

            return true;
        }

        return false;
    }

    private static final class Singleton {
        /* package */ static final RetRxGenerator INSTANCE = new RetRxGenerator();
    }
}
