package com.olku.annotations;

import android.support.annotation.NonNull;

import javax.annotation.processing.Filer;

/** interface that all custom class generators should support. */
public interface AutoProxyClassGenerator {
    /** Compose java class. */
    boolean compose(@NonNull final Filer filer);

    /** Get errors captured during processing. */
    @NonNull
    String getErrors();
}
