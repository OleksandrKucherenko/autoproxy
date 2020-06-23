package com.olku.generators;

import androidx.annotation.*;

import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Type;

/** Code generator interface. */
public interface ReturnsPoet {
    /** Compose return statement for provided method based on return type and modifier. */
    boolean compose(@NonNull final Type returnType,
                    @NonNull final String modifier,
                    @NonNull final MethodSpec.Builder builder);
}
