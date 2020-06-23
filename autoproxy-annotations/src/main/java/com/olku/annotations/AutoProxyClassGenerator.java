package com.olku.annotations;

import androidx.annotation.NonNull;

import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;

/** interface that all custom class generators should support. */
public interface AutoProxyClassGenerator {
    /** Compose java class. */
    boolean compose(@NonNull final Filer filer);

    /** Get errors captured during processing. */
    @NonNull
    String getErrors();

    @NonNull
    String getName();

    @NonNull
    List<Element> getOriginating();
}
