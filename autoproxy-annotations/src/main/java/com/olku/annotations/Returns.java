package com.olku.annotations;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by alexk on 10/1/2017.
 */
@Retention(SOURCE)
@StringDef({Returns.EMPTY, Returns.THROWS, Returns.NULL})
public @interface Returns {
    /** Return empty string as a result. */
    String EMPTY = "empty";
    /** Throw exception on missed call. */
    String THROWS = "throws";
    /** Return null as a result. */
    String NULL = "null";
    /** Direct call with ignore of predicate method result. */
    String DIRECT = "direct";
}


