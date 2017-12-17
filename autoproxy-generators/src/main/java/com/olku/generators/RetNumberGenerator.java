package com.olku.generators;

import android.support.annotation.NonNull;

import com.olku.annotations.RetNumber;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import java.util.Map;
import java.util.TreeMap;

/** Compose return types for boolean. */
public class RetNumberGenerator implements ReturnsPoet {
    private static final Map<String, Class<?>> PRIMITIVES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @NonNull
    public static RetNumberGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    static {
        PRIMITIVES.put("boolean", Boolean.class);
        PRIMITIVES.put("int", Integer.class);
        PRIMITIVES.put("long", Long.class);
        PRIMITIVES.put("byte", Byte.class);
        PRIMITIVES.put("short", Short.class);
        PRIMITIVES.put("float", Float.class);
        PRIMITIVES.put("double", Double.class);
    }

    public boolean compose(@NonNull final Type returnType,
                           @NonNull @RetNumber final String type,
                           @NonNull final MethodSpec.Builder builder) {
        final Class<?> output = PRIMITIVES.get(returnType.toString());

        if (RetNumber.MAX.equals(type)) {
            builder.addStatement("return $T.MAX_VALUE", output);

            return true;
        } else if (RetNumber.MIN.equals(type)) {
            builder.addStatement("return $T.MIN_VALUE", output);

            return true;
        } else if (RetNumber.MINUS_ONE.equals(type)) {
            builder.addStatement("return -1");

            return true;
        } else if (RetNumber.ZERO.equals(type)) {
            builder.addStatement("return 0");

            return true;
        }

        return false;
    }

    private static final class Singleton {
        /* package */ static final RetNumberGenerator INSTANCE = new RetNumberGenerator();
    }
}
