package com.olku.annotations;

import java.lang.annotation.Retention;

import androidx.annotation.StringDef;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by alexk on 10/11/2017.
 */
@Retention(SOURCE)
@StringDef({RetNumber.MIN, RetNumber.MAX, RetNumber.ZERO, RetNumber.MINUS_ONE})
public @interface RetNumber {
    /** Minimal value for numbers. */
    String MIN = "MIN_VALUE";
    /** Maximum value for numbers. */
    String MAX = "MAX_VALUE";
    /** Zero value for numbers. */
    String ZERO = "0";
    /** -1 as a result value. */
    String MINUS_ONE = "-1";
}
