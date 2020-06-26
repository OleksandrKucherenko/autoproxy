package com.olku.generators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.olku.annotations.Returns;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

/** Compose return types for boolean. */
public class ReturnsGenerator implements ReturnsPoet {
    @NonNull
    public static ReturnsGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @Nullable @Returns final String type,
                           @NonNull final MethodSpec.Builder builder) {
        // empty string
        if (Returns.EMPTY.equals(type)) {
            builder.addStatement("return $S", "");
            return true;
        }

        // null value
        if (Returns.NULL.equals(type)) {
            builder.addStatement("return ($T)null", returnType);
            return true;
        }

        // throw exception
        if (Returns.THROWS.equals(type)) {
            builder.addStatement("throw new $T($S)", UnsupportedOperationException.class, "cannot resolve return value.");
            return true;
        }

        // implement direct call with ignore of predicate result
        if (Returns.DIRECT.equals(type)) {
            builder.addComment("direct call, ignore predicate result");
            return true;
        }

        // implement skipped call with ignore of predicate result
        if (Returns.SKIP.equals(type)) {
            builder.addComment("skipped call, ignore predicate result. afterCall will be used for return composing.");
            return true;
        }

        // Builders support, return instance for chained calls
        if (Returns.THIS.equals(type)) {
            builder.addComment("return current instance");
            builder.addStatement("return ($T)this", returnType);
            return true;
        }

        return false;
    }

    private static final class Singleton {
        /* package */ static final ReturnsGenerator INSTANCE = new ReturnsGenerator();
    }
}
