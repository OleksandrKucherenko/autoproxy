package com.olku.autoproxy.sample;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.olku.annotations.AutoProxy;

@AutoProxy(innerType = FinalClass.class, flags = AutoProxy.Flags.ALL, prefix = "$")
@RequiresApi(api = Build.VERSION_CODES.N)
public interface MimicFinalClass {
    void dummyCall();

    boolean returnBoolean();

    void consumer(String data);

    void bi_consumer(String data, String options);

    String et_function(String data);

    String bi_function(String data, String options);
}
