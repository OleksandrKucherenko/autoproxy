package com.olku.autoproxy.sample;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.olku.annotations.AutoProxy;
import com.olku.autoproxy.sample.$MimicFinalClass.M;

import java.util.function.BiFunction;

@AutoProxy(innerType = FinalClass.class, flags = AutoProxy.Flags.ALL, prefix = "$")
@RequiresApi(api = Build.VERSION_CODES.N)
public interface MimicFinalClass {
    void dummyCall();

    boolean returnBoolean();

    /** After call lambda injector. */
    @NonNull
    static MimicFinalClass create(FinalClass instance,
                                  final BiFunction<String, Object, ?> afterCall) {
        return MimicFinalClass.create(instance, (m, args) -> true, afterCall);
    }

    /** Lambda injection helper. */
    static MimicFinalClass create(final FinalClass instance,
                                  final BiFunction<String, Object[], Boolean> predicate,
                                  final BiFunction<String, Object, ?> afterCall) {
        return new $MimicFinalClass(instance) {

            @Override
            public boolean predicate(@NonNull @M final String methodName, final Object... args) {
                return predicate.apply(methodName, args);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T afterCall(@NonNull @M final String methodName, final T result) {
                return (T) afterCall.apply(methodName, result);
            }
        };
    }

    /** Customize return of the specific method. */
    @NonNull
    static MimicFinalClass returns(@NonNull @M String method,
                                   @NonNull final FinalClass instance,
                                   @NonNull final BiFunction<String, Object, ?> afterCall) {
        return create(instance,
                // skip all except specific method
                (String m, Object[] a) -> !method.equals(m),
                // just forward result, or call afterCall
                (String m, Object r) -> (method.equals(m)) ? afterCall.apply(m, r) : r
        );
    }
}
