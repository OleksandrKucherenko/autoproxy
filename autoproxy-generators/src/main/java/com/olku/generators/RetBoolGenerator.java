package net.easypark.generators;

import androidx.annotation.*;

import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

import net.easypark.annotations.RetBool;

/** Compose return types for boolean. */
public class RetBoolGenerator implements ReturnsPoet {
    @NonNull
    public static RetBoolGenerator getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean compose(@NonNull final Type returnType,
                           @RetBool  final String type,
                           @NonNull final MethodSpec.Builder builder) {
        if (RetBool.FALSE.equals(type)) {
            builder.addStatement("return false");
            return true;
        } else if (RetBool.TRUE.equals(type)) {
            builder.addStatement("return true");
            return true;
        }

        return false;
    }

    private static final class Singleton {
        static final RetBoolGenerator INSTANCE = new RetBoolGenerator();
    }
}
