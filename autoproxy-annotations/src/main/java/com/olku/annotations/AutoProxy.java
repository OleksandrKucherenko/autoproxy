package com.olku.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/** Ask for generation of auto-proxy class for interface. */
@Retention(CLASS)
@Target(value = TYPE)
public @interface AutoProxy {
    /** Type generator class. */
    Class<? extends AutoProxyClassGenerator> value() default AutoProxy.Default.class;

    /** Represents DEFAULT class generator. CommonClassGenerator class in processors module. */
    abstract class Default implements AutoProxyClassGenerator {
    }

    /** Customize return value of the method if call was canceled by predicate. Only for PUBLIC methods. */
    @Retention(CLASS)
    @Target(value = ElementType.METHOD)
    @interface Yield {
        /** Return value adapter. */
        Class<?> adapter() default Returns.class;

        /** Value to extract. */
        String value() default Returns.THROWS;
    }
}
