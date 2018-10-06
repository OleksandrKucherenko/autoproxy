package com.olku.annotations;

import java.lang.annotation.Retention;

import androidx.annotation.StringDef;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/** Boolean value returns. */
@Retention(SOURCE)
@StringDef({RetBool.TRUE, RetBool.FALSE})
public @interface RetBool {
    /** True value. */
    String TRUE = "true";
    /** False value. */
    String FALSE = "false";
}
