package com.olku.annotations;

import javax.annotation.processing.Filer;

import androidx.annotation.NonNull;

/** interface that all custom class generators should support. */
public interface AutoProxyClassGenerator {
    /** Compose java class. */
    boolean compose(@NonNull final Filer filer);

    /** Get errors captured during processing. */
    @NonNull
    String getErrors();
}
