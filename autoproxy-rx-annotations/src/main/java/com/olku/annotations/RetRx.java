package com.olku.annotations;

import java.lang.annotation.Retention;

import androidx.annotation.StringDef;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/** Rx Observable results. */
@Retention(SOURCE)
@StringDef({RetRx.EMPTY, RetRx.ERROR})
public @interface RetRx {
    /** empty observable. */
    String EMPTY = "empty";
    /** error raising. */
    String ERROR = "error";
}
