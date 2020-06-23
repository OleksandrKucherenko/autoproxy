package net.easypark.annotations;

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
    /** Type generator class. */
    Class<? extends AutoProxyClassGenerator> value() default Common.class;

    /** Ask generator to compose static methods for simplified instance creation. */
    int flags() default Flags.NONE;

    /** Default Yield return policy. */
    String defaultYield() default Returns.THROWS;

    /** Represents DEFAULT class generator. CommonClassGenerator class in processors module. */
    abstract class Common implements AutoProxyClassGenerator {
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

            // map field name to default value
            map.put("value", Common.class);
            map.put("flags", AutoProxy.Flags.NONE);
            map.put("defaultYield", Returns.THROWS);

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
