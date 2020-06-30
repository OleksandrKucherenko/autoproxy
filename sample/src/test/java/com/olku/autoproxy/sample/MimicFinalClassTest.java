package com.olku.autoproxy.sample;

import androidx.annotation.NonNull;

import com.olku.autoproxy.sample.$MimicFinalClass.M;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.BiFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class MimicFinalClassTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock BiFunction<String, Object, ?> mockAfterCall;
    @Mock BiFunction<String, Object, ?> mockAfterCall2;

    @Test
    public void simpleLogger() {
        final StringBuilder logger = new StringBuilder();
        final FinalClass instance = new FinalClass();
        final MimicFinalClass proxy = $MimicFinalClass.create(instance, (String m, Object[] objects) -> {
            logger.append("called: ").append(m).append("\n");
            return true;
        });

        proxy.bi_consumer("data", "options");
        proxy.bi_function("data", "options");
        proxy.consumer("data");
        proxy.et_function("data");
        proxy.dummyCall();
        proxy.returnBoolean();

        assertThat(logger.toString(), containsString(M.BI_CONSUMER_DATA_OPTIONS));
        assertThat(logger.toString(), containsString(M.BI_FUNCTION_DATA_OPTIONS));
        assertThat(logger.toString(), containsString(M.CONSUMER_DATA));
        assertThat(logger.toString(), containsString(M.ET_FUNCTION_DATA));
        assertThat(logger.toString(), containsString(M.DUMMYCALL));
        assertThat(logger.toString(), containsString(M.RETURNBOOLEAN));

        System.out.println(logger.toString());
    }

    @Test
    public void simpleWhenOverride() {
        final StringBuilder logger = new StringBuilder();
        final FinalClass instance = new FinalClass();
        final MimicFinalClass bindInstance = $MimicFinalClass.bind(instance);
        final MimicFinalClass proxy = when(M.ET_FUNCTION_DATA, (m, r) -> {
                    logger.append("afterCall ").append(m).append(" with pre-result: '").append(r).append("'\n");
                    return "result";
                },
                create(bindInstance,
                        (String m, Object[] objects) -> {
                            logger.append("  called: ").append(m).append("\n");
                            return true;
                        },
                        (String m, Object o) -> {
                            logger.append("  afterCall ").append(m).append(" with pre-result: '").append(o).append("'\n");
                            return o;
                        })
        );

        final String original = instance.et_function("data");
        final String result = proxy.et_function("data");

        assertThat(result, not(equalTo(original)));
        assertThat(result, equalTo("result"));

        System.out.println(logger.toString());
    }

    @Test
    public void chainedCalls() {
        final FinalClass instance = new FinalClass();
        final MimicFinalClass bindToInstance = $MimicFinalClass.bind(instance);

        Mockito.when(mockAfterCall.apply(anyString(), any())).thenReturn(null);
        Mockito.when(mockAfterCall2.apply(anyString(), any())).thenReturn(null);

        final MimicFinalClass proxy =
                when(M.RETURNBOOLEAN, (s, o) -> true,
                        when(M.DUMMYCALL, mockAfterCall,
                                when(M.BI_CONSUMER_DATA_OPTIONS, mockAfterCall2,
                                        bindToInstance
                                )
                        )
                );

        assertThat(proxy.returnBoolean(), equalTo(true));

        proxy.dummyCall();
        Mockito.verify(mockAfterCall).apply(anyString(), any());

        proxy.bi_consumer("data", "options");
        Mockito.verify(mockAfterCall2).apply(anyString(), any());
    }

    //region Helpers
    /** After call lambda injector. */
    @NonNull
    static MimicFinalClass create(MimicFinalClass instance,
                                  final BiFunction<String, Object, ?> afterCall) {
        return create(instance, (m, args) -> true, afterCall);
    }

    /** Lambda injection helper. */
    static MimicFinalClass create(final MimicFinalClass instance,
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
    static MimicFinalClass when(@NonNull @M String method,
                                @NonNull final BiFunction<String, Object, ?> afterCall,
                                @NonNull final MimicFinalClass instance) {
        return create(instance,
                // just forward result, or call afterCall
                (String m, Object r) -> (method.equals(m)) ? afterCall.apply(m, r) : r
        );
    }
    //endregion
}