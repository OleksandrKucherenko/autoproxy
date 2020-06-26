package com.olku.annotations;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/** Different types of return's.  */
@Retention(SOURCE)
@StringDef({Returns.EMPTY, Returns.THROWS, Returns.NULL, Returns.DIRECT, Returns.THIS, Returns.SKIP})
public @interface Returns {
    /** Return empty string as a result. */
    String EMPTY = "empty";
    /** Throw exception on missed call. */
    String THROWS = "throws";
    /** Return null as a result. */
    String NULL = "null";
    /** Direct call with ignore of predicate method result. */
    String DIRECT = "direct";
    /** Default value is reference on current instance. */
    String THIS = "this";
    /** Skip inner call. `afterCall` call will be used for return composing. */
    String SKIP = "skipped";
}


