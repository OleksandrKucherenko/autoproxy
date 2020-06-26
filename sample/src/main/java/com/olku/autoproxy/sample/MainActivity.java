package com.olku.autoproxy.sample;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import rx.Observable;

public class MainActivity extends AppCompatActivity implements MvpView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final RxJava1Sample instance = null /* ... */;
        final RxJava1Sample proxy = new Proxy_RxJava1Sample(instance) {
            @Override
            public boolean predicate(@NonNull String methodName, Object... args) {
                return true;
            }

            @Override
            public <T> T afterCall(@NonNull String methodName, T result) {
                if (M.CHAINEDCALLSKIP.equals(methodName)) {
                    return (T) computeSomething();
                }

                return result;
            }
        };
        assert false == proxy.chainedCallSkip();
    }

    private Boolean computeSomething() {
        return false;
    }

    @NonNull
    public MvpView getProxy() {
        return Proxy_MvpView.create(this, (methodName, args) -> !isFinishing());
//        return new Proxy_MvpView(this) {
//            @Override
//            public boolean predicate(@M @NonNull String methodName, Object... args) {
//                return !isFinishing();
//            }
//        };
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
