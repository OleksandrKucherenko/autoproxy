package com.olku.autoproxy.sample;

import androidx.annotation.NonNull;

import com.olku.annotations.AutoProxy;

@AutoProxy(innerType = FinalClass.class, flags = AutoProxy.Flags.ALL)
public interface MimicFinalClass {
    void dummyCall();

    boolean returnBoolean();

    @NonNull
    static MimicFinalClass proxy(FinalClass instance) {
        return Proxy_MimicFinalClass.create(instance, (m, args) -> true);
    }
}
