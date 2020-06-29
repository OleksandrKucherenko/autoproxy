package com.olku.annotations;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/** Ask for generation of auto-proxy class for interface. */
@Retention(CLASS)
@Target(value = TYPE)
public @interface AutoProxy {
    /** Default prefix for auto-generated class. */
    String PROXY = "Proxy_";

    /** Type generator class. */
    Class<? extends AutoProxyClassGenerator> value() default Common.class;

    /** Ask generator to compose static methods for simplified instance creation. */
    int flags() default Flags.NONE;

    /** Default Yield return policy. */
    String defaultYield() default Returns.THROWS;

    /** Class used as a inner variable type. By default will be used annotated class/interface type. */
    Class<?> innerType() default Defaults.class;

    /** Auto-generated class name prefix. Examples: Stub_, Fake_, Mediator_ */
    String prefix() default PROXY;

    /** Represents DEFAULT class generator. CommonClassGenerator class in processors module. */
    abstract class Common implements AutoProxyClassGenerator {
    }

    /** Represents DEFAULT inner data type value. */
    abstract class Defaults {
        /** Name of the annotation method {@link #innerType()}. */
        /* package */ static final String INNER_TYPE = "innerType";
        /** Name of the annotation method {@link #defaultYield()}. */
        /* package */ static final String DEFAULT_YIELD = "defaultYield";
        /** Name of the annotation method {@link #flags()}. */
        /* package */ static final String FLAGS = "flags";
        /** Name of the annotation method {@link #value()}. */
        /* package */ static final String VALUE = "value";
        /** Name of the annotation method {@link #prefix()}. */
        /* package */ static final String PREFIX = "prefix";
    }

    /** Customize return value of the method if call was canceled by predicate. Only for PUBLIC methods. */
    @Retention(CLASS)
    @Target(value = ElementType.METHOD)
    @interface Yield {
        /** Default return value generator adapter. */
        Class<?> adapter() default Returns.class;

        /** Adapter code generation customization. */
        String value() default Returns.THROWS;
    }

    /** Required real call return value post-processing. */
    @Retention(CLASS)
    @Target(value = ElementType.METHOD)
    @interface AfterCall {
    }

    /** Special code generation modifier flags. */
    @interface Flags {
        /** Default value. */
        int NONE = 0x0000;
        /** Compose static method for easier instance creation. */
        int CREATOR = 0x0001;
        /** Compose afterCall(...) method for all methods in class. */
        int AFTER_CALL = 0x0002;
        /** Compose callByName(...) method that maps string name to a method call. */
        int MAPPING = 0x004;

        /** Compose all additional methods. */
        int ALL = CREATOR | AFTER_CALL | MAPPING;
    }

    /**
     * Default implementation of annotation interface. Used for simplified extraction of default values of
     * annotation during code generation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    abstract class DefaultAutoProxy implements AutoProxy {
        @Override
        public final Class<? extends AutoProxyClassGenerator> value() {
            return Common.class;
        }

        @Override
        public final int flags() {
            return Flags.NONE;
        }

        @Override
        public final String defaultYield() {
            return Returns.THROWS;
        }

        /** create instance with field-to-default mapping. */
        @NonNull
        public static Map<String, Object> asMap() {
            final Map<String, Object> map = new HashMap<>();

            // map field/method name of annotation to it default value
            map.put(Defaults.VALUE, Common.class);
            map.put(Defaults.FLAGS, AutoProxy.Flags.NONE);
            map.put(Defaults.DEFAULT_YIELD, Returns.THROWS);
            map.put(Defaults.INNER_TYPE, Defaults.class);
            map.put(Defaults.PREFIX, PROXY);

            return map;
        }
    }

    /** Default implementation of Yield annotation interface. Used for simplifying default annotations values extracting. */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    abstract class DefaultYield implements Yield {
        @Override
        public final Class<?> adapter() {
            return Returns.class;
        }

        @Override
        public final String value() {
            return Returns.THROWS;
        }

        /** create instance with field-to-default mapping. */
        @NonNull
        public static Map<String, Object> asMap() {
            final Map<String, Object> map = new HashMap<>();

            map.put("adapter", Returns.class);
            map.put("value", Returns.THROWS);

            return map;
        }

    }
}
