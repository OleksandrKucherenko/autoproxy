package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.olku.annotations.RetRx;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import rx.Completable;
import rx.Observable;
import rx.Single;

/** RxJava return values generator. */
public class RetRxGenerator implements ReturnsPoet {
    private static final boolean IS_DEBUG = false;

    /** Base classes supported by rxJava v2.xx */
    private static final Class<?>[] BASE_CLASSES = {
            Observable.class,
            Single.class,
            Completable.class,
    };


    @NonNull
    public static RetRxGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable @RetRx final String type,
                           @NonNull final MethodSpec.Builder builder) {
        final Class<?> rxType = resolveReturnType(returnType);
        final String generics = extractGenericsFromReturnType(returnType);

        if (RetRx.EMPTY.equals(type)) {
            //  rx.Observable.empty();
            if (rxType.isAssignableFrom(Single.class)) {
                // return Observable.<Boolean>empty().toSingle();

                builder.addComment("will be equivalent to: `io.reactivex.Single.error(...)`");
                builder.addStatement("return $T.<$L>empty().toSingle()", Observable.class, generics);
            } else if (rxType.isAssignableFrom(Completable.class)) {
                builder.addComment("will be equivalent to: `io.reactivex.Completable.complete()`");
                builder.addStatement("return $T.fromObservable($T.empty())", rxType, Observable.class);
            } else {
                builder.addStatement("return $T.empty()", rxType);
            }
            return true;
        }

        if (RetRx.ERROR.equals(type)) {
            //  rx.Observable.error(new UnsupportedOperationException("unsupported method call"));
            builder.addStatement("return $T.error(new $T($S))",
                    rxType,
                    UnsupportedOperationException.class,
                    "unsupported method call");

            return true;
        }

        if (RetRx.NEVER.equals(type)) {
            //  rx.Observable.never();
            //  Observable.<Boolean>never().toSingle();
            if(rxType.isAssignableFrom(Single.class)){
                builder.addStatement("return $T.<$L>never().toSingle()", Observable.class, generics);
            } else {
                builder.addStatement("return $T.never()", rxType);
            }
            return true;
        }

        return false;
    }

    @SuppressWarnings("NewApi")
    @NonNull
    /* package */ static String extractGenericsFromReturnType(@NonNull final Type returnType) {
        final String returnTypeName = returnType.toString();
        final String[] parts = returnTypeName.split("<");

        final StringBuilder result = new StringBuilder();
        String prefix = "";
        for (int i = 1, len = parts.length; i < len; i++) {
            final String part = parts[i].trim()
                    .replaceAll("[ \\t]*<[ \\t]*", "<")
                    .replaceAll("[ \\t]*>[ \\t]*", ">")
                    .replaceAll("[ \\t]*,[ \\t]*", ", ");
            final String element = (i == len - 1 && part.endsWith(">")) ?
                    part.substring(0, part.length() - 1) : part;
            result.append(prefix).append(element);
            prefix = "<";
        }

        return result.toString();
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
        /* package */ static final RetRxGenerator INSTANCE = new RetRxGenerator();
    }
}
