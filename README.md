# AutoProxy

Annotation Processing Library.

Generates proxy class on top of interface/abstract class, that allows to intercept calls.

Also known as a design pattern: proxy, delegate, interceptor.

```
Call --> Proxy
            |
            +----> predicate() ---true-------> Inner Call
                       |
                       +---------false-------> Yeild Result (Default)
```

Or:

```
+------------------+
| Proxy            |
|                  |
|  +----------+    |
|  | Inner    |    |
|  |       ^  |    |
|  +-------|--+    |
|          |       |
|  predicate(...)  |
|          |       |
+----------|-------+
           |
         Call
```

# Why should I use it?

Library solves common Mvp View problem: call of view from presenter when
view is already detached from Activity. (delayed updated call)

Library gives a little bigger freedom if you think for a second about this:

* predicate method allows to capture all calls. You can easily log all calls for example;
* auto-generated Proxy class is simple and does not have any performance impacts.
* used in library approach allows custom generators of code/results. Unknown types is super easy to support.

# Usage

You can use it as a submodule or as compiled libs.

## Step #1 : attach as a submodule

attach repository as a submodule:

```bash
# initialize project for submodules usage
git submodule init

# add submodule
git submodule add https://github.com/OleksandrKucherenko/autoproxy.git modules/autoproxy

# update project recursively and pull all submodules
git submodule update --init --recursive
```

## Step #2: include submodule into project

`app/build.gradle`:

```gradle
    /* AutoProxy generator */
    compileOnly project(':modules:autoproxy:autoproxy-annotations')
    compileOnly project(':modules:autoproxy:autoproxy-rx-annotations')
    compileOnly project(':modules:autoproxy:autoproxy-rx-generators')

    annotationProcessor project(':modules:autoproxy:autoproxy-rx-generators')
    annotationProcessor project(':modules:autoproxy:autoproxy-processor')
```

`settings.gradle`:

```gradle
include ':modules:autoproxy:autoproxy-annotations'
include ':modules:autoproxy:autoproxy-generators'
include ':modules:autoproxy:autoproxy-rx-annotations'
include ':modules:autoproxy:autoproxy-rx-generators'
include ':modules:autoproxy:autoproxy-processor'
```

## Step #3: Declare proxy class specifics

```java
@AutoProxy
public interface MvpView {
    /** Returns NULL if predicate returns False. */
    @AutoProxy.Yield(Returns.NULL)
    Observable<Boolean> dummyCall();

    /** Returns Observable.empty() */
    @AutoProxy.Yield(adapter = RetRx.class, value = RetRx.EMPTY)
    Observable<Boolean> dummyCall(final List<String> generic);

    /** Throws exception on False result from predicate. */
    @AutoProxy.Yield(Returns.THROWS)
    Observable<Boolean> dummyCall(final String message, final List<String> args);

    /** Returns Observable.error(...) on False result from predicate. */
    @AutoProxy.Yield(adapter = RetRx.class, value = RetRx.ERROR)
    Observable<Boolean> dummyCall(final String message, final Object... args);

    /** Returns ZERO on False result from predicate. */
    @AutoProxy.Yield(RetNumber.ZERO)
    double numericCall();

    /** Returns FALSE on False result from predicate. */
    @AutoProxy.Yield(RetBool.FALSE)
    boolean booleanCall();

    /** Does direct call independent to predicate result. */
    @AutoProxy.Yield(Returns.DIRECT)
    boolean dispatchDeepLink(@NonNull final Uri deepLink);

    /** Returns Observable.just(true) on False result from predicate. */
    @AutoProxy.Yield(adapter = JustRxGenerator.class, value = "true")
    Observable<Boolean> startHearthAnimation();
}
```

## Step 4: Code Generation Results

```java
public abstract class Proxy_MvpView implements MvpView {
    protected final MvpView inner;

    public Proxy_MvpView(@NonNull final MvpView instance){
        this.inner = instance;
    }

    public abstract boolean predicate(final String methodName, final Object... args);

    public final boolean dispatchDeepLink(@NonNull final Uri deepLink) {
        if (!predicate( "dispatchDeepLink", deepLink )) {
            // @com.olku.annotations.AutoProxy.Yield("direct")
            // direct call, ignore predicate result
        }
        return this.inner.dispatchDeepLink(deepLink);
    }

    /* ... Other methods ... */
    @NonNull
    public final Observable<Boolean> startHearthAnimation() {
        if (!predicate( "startHearthAnimation" )) {
            // @com.olku.annotations.AutoProxy.Yield(adapter=com.olku.generators.JustRxGenerator.class, value="true")
            return Observable.just(true);
        }
        return this.inner.startHearthAnimation();
    }
}

```

## Step #5: Usage in project

```java
        /** Get instance of View. */
        @Provides
        @PerFragment
        MvpView providesView() {
            // Proxy class isolates Presenter from direct View calls. View
            // will receive calls only when Fragment that implements that
            // interface is in updateable state (attached to activity, not
            // destroyed or any other inproper state).
            return new Proxy_MvpView(this.view) {
                @Override
                public boolean predicate(final String methodName, final Object... args) {
                    return ((BaseFragment) inner).isUpdatable();
                }
            };
        }

```

# Troubles

http://www.vogella.com/tutorials/GitSubmodules/article.html

## Reset submodule to remote repository version

```
git submodule foreach 'git reset --hard'
# including nested submodules
git submodule foreach --recursive 'git reset --hard'
```