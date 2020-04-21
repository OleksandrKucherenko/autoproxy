package com.olku.autoproxy.sample;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.olku.annotations.AutoProxy;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Abstract class. */
@AutoValue
public abstract class ParkingArea {

    /** excluded from serialization. */
    /* package */ transient List<String> runtimeData = new ArrayList<>();

    /** One of immutable fields. */
    public abstract long id();

    /** Hidden from developer auto-value builder from instance creation. */
    protected abstract ParkingArea.Builder toBuilderInner();

    /**
     * Special override that creates from current instance of ParkingArea a builder with cloned data.
     * On build() call runtime data from parkingArea will be assigned to it clone.
     */
    public ParkingArea.Builder toBuilder() {
        return new Proxy_ParkingArea$Builder(toBuilderInner()) {
            @Override
            public boolean predicate(@NotNull final String methodName, final Object... args) {
                return true; /* allow all calls */
            }

            @Override
            public <R> R afterCall(@NotNull @Methods final String methodName, final R result) {
                // copy runtime fields from instance after clone creation
                if (Methods.BUILD.equals(methodName) && result instanceof ParkingArea) {
                    ((ParkingArea) result).runtimeData = runtimeData;
                    return result;
                }

                return (R) this; // return own instance of Builder instead of inner
            }
        };
    }

    /** Unique IDs generator. */
    private static final AtomicInteger sIndex = new AtomicInteger();

    /** Create instance of builder. */
    public static ParkingArea.Builder builder() {
        return new AutoValue_ParkingArea.Builder().id(sIndex.decrementAndGet());
    }

    @AutoValue.Builder
    @AutoProxy
    public static abstract class Builder {

        @AutoProxy.AfterCall
        public abstract ParkingArea build();

        @AutoProxy.AfterCall
        @NonNull
        public abstract Builder id(final long id);
    }
}
