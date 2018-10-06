package com.olku.autoproxy.sample;

import android.net.Uri;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import rx.Observable;

public class MainActivity extends AppCompatActivity implements MvpView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @NonNull
    public MvpView getProxy() {
        return new Proxy_MvpView(this) {
            @Override
            public boolean predicate(String methodName, Object... args) {
                return !isFinishing();
            }
        };
    }

    //region View interface
    @Override
    public Observable<Boolean> dummyCall() {
        return Observable.empty();
    }

    @Override
    public Observable<Boolean> dummyCall(List<String> generic) {
        return Observable.empty();
    }

    @Override
    public Observable<Boolean> dummyCall(String message, List<String> args) {
        return Observable.empty();
    }

    @Override
    public Observable<Boolean> dummyCall(String message, Object... args) {
        return Observable.empty();
    }

    @Override
    public double numericCall() {
        return 0;
    }

    @Override
    public boolean booleanCall() {
        return false;
    }

    @Override
    public boolean dispatchDeepLink(@NonNull Uri deepLink) {
        return false;
    }

    @Override
    public Observable<Boolean> startHearthAnimation() {
        return Observable.empty();
    }
    //endregion
}
