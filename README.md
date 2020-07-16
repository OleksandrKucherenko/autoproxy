# 1. AutoProxy

[![Build Status](https://travis-ci.org/OleksandrKucherenko/autoproxy.svg?branch=master)](https://travis-ci.org/OleksandrKucherenko/autoproxy)
[ ![Download](https://api.bintray.com/packages/kucherenko-alex/android/com.olku%3Aautoproxy/images/download.svg) ](https://bintray.com/kucherenko-alex/android/com.olku%3Aautoproxy/_latestVersion)

Annotation Processing Library.

Generates proxy class on top of interface/abstract class, that allows to intercept calls.

Also known as a design pattern: proxy, delegate, interceptor.

[![Diagram](https://i.imgur.com/vvgUtw7h.png)](https://imgur.com/vvgUtw7)

<details>
<summary>UML PROXY PATTERN:</summary>

[Wikipedia Proxy Pattern](https://en.wikipedia.org/wiki/Proxy_pattern)

[![Proxy Pattern](https://i.imgur.com/EYDGCB1h.png)](https://imgur.com/EYDGCB1)
</details>

- [1. AutoProxy](#1-autoproxy)
- [2. Why should I use it?](#2-why-should-i-use-it)
  - [2.1. Use Cases](#21-use-cases)
- [3. Concepts](#3-concepts)
  - [3.1. Predicate](#31-predicate)
  - [3.2. AfterCall](#32-aftercall)
- [4. Setup Guide](#4-setup-guide)
  - [4.1. Configure Dependencies](#41-configure-dependencies)
  - [4.2. Make Proxy Class Specification](#42-make-proxy-class-specification)
  - [4.3. Usage in project (aka usage with Dagger)](#43-usage-in-project-aka-usage-with-dagger)
  - [4.4. Customization of Generated Code](#44-customization-of-generated-code)
- [5. Advanced Usage (Patterns)](#5-advanced-usage-patterns)
  - [5.1. Mimic final class interface](#51-mimic-final-class-interface)
  - [5.2. Side effects](#52-side-effects)
  - [5.3. Runtime defined output value](#53-runtime-defined-output-value)
    - [5.3.1. Skip Inner Class Call and Simple Return](#531-skip-inner-class-call-and-simple-return)
    - [5.3.2. Skip Inner Class Call and Return Self Reference](#532-skip-inner-class-call-and-return-self-reference)
  - [5.4. Customize Yield Return Types (Custom return type adapter)](#54-customize-yield-return-types-custom-return-type-adapter)
  - [5.5. Decouple MVC, MVP, MVVM patterns (inject mediator)](#55-decouple-mvc-mvp-mvvm-patterns-inject-mediator)
  - [5.6. Compose FAKE class (experimental)](#56-compose-fake-class-experimental)
- [6. Troubles](#6-troubles)
  - [6.1. Reset submodule to remote repository version](#61-reset-submodule-to-remote-repository-version)
  - [6.2. Enable Tracing](#62-enable-tracing)
  - [6.3. How to Debug?](#63-how-to-debug)
  - [6.4. How to Change/Generate GPG signing key?](#64-how-to-changegenerate-gpg-signing-key)
  - [6.5. How to Publish?](#65-how-to-publish)
- [7. Roadmap](#7-roadmap)
- [8. License](#8-license)

# 2. Why should I use it?

## 2.1. Use Cases

1. create proxy class that ignore all calls till UI is in a right lifecycle state; Library solves common Mvp View problem: call of view from presenter when
view is already detached from Activity. (delayed updated call)
2. inject side-effects on top of existing implementation. Example: log all the calls to proxy, control sequence and performance;
3. Lazy initialization, record all calls and playback them on instance that reach a specific state;
4. Mutability injection into AutoValue Builder (complex data migration from one AutoValue instance to another). AutoValue classes as DB with automatic Primary Key auto-increment;
5. Mocks vs Fakes. Generate a fake implementation by annotating primary interface/abstract class
6. Composing Adapter (Frontend) for another API 

Library gives a bigger freedom if you think for a second about it:

- auto-generated Proxy class is simple and does not have any performance impacts. No Reflection. All resolved during the compilation time. 
- Survive ProGuard optimization/obfuscation. 
- used in library approach allows custom generators of code/results. Unknown types is super easy to support.
- Allows to decouple main application business logic from different side-effects and dependencies (mediator injecting in code)

# 3. Concepts

## 3.1. Predicate

Intercept call before the inner instance call. If returns TRUE, than allowed inner instance call.
On FALSE developer should decide what to do. Default behavior: throw exception.

```java
  public final Observable<Boolean> dummyCall(final List<String> generic) {
    if (!predicate( "dummyCall", generic )) {
      // @com.olku.annotations.AutoProxy.Yield(adapter=com.olku.generators.RetRxGenerator.class, value="empty")
      return Observable.empty();
    }
    return this.inner.dummyCall(generic);
  }
```

## 3.2. AfterCall

From time to time exists situations when we need to intercept and modify results of the inner call.

In that case library provides `@AutoProxy.AfterCall` annotation, it allows to mark specific method that requires this feature. `@AutoProxy(flags = AutoProxy.Flags.AFTER_CALL)` enables that for all methods in class/interface.

<details>
<summary>AutoValue Builder Sample</summary>

Declaration:

```java
/** Abstract class. */
@AutoValue
public abstract class ParkingArea {

    @AutoValue.Builder
    @AutoProxy
    public static abstract class Builder {
        @AutoProxy.AfterCall
        @NonNull
        public abstract Builder id(final long id);
    }
}
```

Generated class will contains after that two methods:

```java
public abstract class Proxy_ParkingArea$Builder extends ParkingArea.Builder {
  protected final ParkingArea.Builder inner;

  public abstract boolean predicate(final String methodName, final Object... args);

  public abstract <R> R afterCall(final String methodName, final R result);

  /* ... other methods ... */
}
```
</details>

Change of internal proxy pattern:

```java
  public final ParkingArea build() {
    if (!predicate( "build" )) {
      throw new UnsupportedOperationException("cannot resolve return type.");
    }

    return afterCall("build", this.inner.build());
  }
```

# 4. Setup Guide

You can use it as a submodule or as compiled libs.

## 4.1. Configure Dependencies

```groovy
/* include repository */
repositories {
    maven {
        url  "https://dl.bintray.com/kucherenko-alex/android"
    }
}
```

```groovy
/* add dependencies */
dependencies{
    /* AutoProxy generator */
    compileOnly 'com.olku:autoproxy-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-annotations:+'
    compileOnly 'com.olku:autoproxy-rx3-generators:+'

    annotationProcessor 'com.olku:autoproxy-rx3-generators:+'
    annotationProcessor 'com.olku:autoproxy-processor:+'
}
```

<details>
<summary>With RxJava v1.xx Support</summary>

```groovy
/* add dependencies */
dependencies{
    /* AutoProxy generator */
    compileOnly 'com.olku:autoproxy-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-generators:+' /* RxJava v1.xx */

    annotationProcessor 'com.olku:autoproxy-rx-generators:+' /* RxJava v1.xx */
    annotationProcessor 'com.olku:autoproxy-processor:+'

}
```

</details>

<details>
<summary>With RxJava v2.xx Support</summary>

```groovy
/* add dependencies */
dependencies{
    /* AutoProxy generator */
    compileOnly 'com.olku:autoproxy-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-annotations:+'
    compileOnly 'com.olku:autoproxy-rx2-generators:+' /* RxJava v2.xx */

    annotationProcessor 'com.olku:autoproxy-rx2-generators:+' /* RxJava v2.xx */
    annotationProcessor 'com.olku:autoproxy-processor:+'
}
```
</details>

<details>
<summary>With RxJava v3.xx Support</summary>

```groovy
/* add dependencies */
dependencies{
    /* AutoProxy generator */
    compileOnly 'com.olku:autoproxy-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-annotations:+'
    compileOnly 'com.olku:autoproxy-rx3-generators:+' /* RxJava v3.xx */

    annotationProcessor 'com.olku:autoproxy-rx3-generators:+' /* RxJava v3.xx */
    annotationProcessor 'com.olku:autoproxy-processor:+'
}
```
</details>

<details>
<summary>OR attach as a submodule</summary>

attach repository as a submodule:

```bash
# initialize project for submodules usage
git submodule init

# add submodule
git submodule add https://github.com/OleksandrKucherenko/autoproxy.git modules/autoproxy

# update project recursively and pull all submodules
git submodule update --init --recursive
```

include submodule into project

`app/build.gradle`:

```groovy
    /* AutoProxy generator */
    compileOnly project(':modules:autoproxy:autoproxy-annotations')
    compileOnly project(':modules:autoproxy:autoproxy-rx-annotations')
    compileOnly project(':modules:autoproxy:autoproxy-rx-generators') /* RxJava v1.xx */
    compileOnly project(':modules:autoproxy:autoproxy-rx2-generators') /* RxJava v2.xx */

    /* For Java Projects */
    annotationProcessor project(':modules:autoproxy:autoproxy-rx-generators') /* RxJava v1.xx */
    annotationProcessor project(':modules:autoproxy:autoproxy-rx2-generators') /* RxJava v2.xx */
    annotationProcessor project(':modules:autoproxy:autoproxy-processor')

    /* OR for Kotlin Projects */
    kapt project(':modules:autoproxy-rx-generators') /* RxJava v1.xx */
    kapt project(':modules:autoproxy-rx2-generators') /* RxJava v2.xx */
    kapt project(':modules:autoproxy-processor')

```

`settings.gradle`:

```groovy
include ':modules:autoproxy:autoproxy-annotations'
include ':modules:autoproxy:autoproxy-generators'
include ':modules:autoproxy:autoproxy-rx-annotations'
include ':modules:autoproxy:autoproxy-rx-generators'
include ':modules:autoproxy:autoproxy-rx2-generators'
include ':modules:autoproxy:autoproxy-processor'
```

</details>


## 4.2. Make Proxy Class Specification

```java
/** Simplest case */
@AutoProxy
public interface MvpView {
  /* ... declare method ... */
}
```

OR check the bigger example bellow.

<details>
<summary>Declarations - Show code</summary>

```java
@AutoProxy(flags = AutoProxy.Flags.ALL)
public interface MvpView {
    /** Returns NULL if predicate returns False. */
    @AutoProxy.Yield(Returns.NULL)
    Observable<Boolean> dummyCall();

    /** Returns Observable.empty() */
    @AutoProxy.Yield(adapter = RetRxGenerator.class, value = RetRx.EMPTY)
    Observable<Boolean> dummyCall(final List<String> generic);

    /** Throws exception on False result from predicate. */
    @AutoProxy.Yield(Returns.THROWS)
    Observable<Boolean> dummyCall(final String message, final List<String> args);

    /** Returns Observable.error(...) on False result from predicate. */
    @AutoProxy.Yield(adapter = RetRxGenerator.class, value = RetRx.ERROR)
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
</details>

<details>
<summary>AutoGenerate Code - Show code</summary>

```java
public abstract class Proxy_MvpView implements MvpView {
  protected final MvpView inner;

  public Proxy_MvpView(@NonNull final MvpView instance) {
    this.inner = instance;
  }

  public abstract boolean predicate(@Methods @NonNull final String methodName,
      final Object... args);

  public final Observable<Boolean> dummyCall() {
    if (!predicate( Methods.DUMMYCALL )) {
      // @com.olku.annotations.AutoProxy.Yield("null")
      return (Observable<Boolean>)null;
    }
    return this.inner.dummyCall();
  }

  public final Observable<Boolean> dummyCall(final List<String> generic) {
    if (!predicate( M.DUMMYCALL, generic )) {
      // @com.olku.annotations.AutoProxy.Yield(adapter=com.olku.generators.RetRxGenerator.class, value="empty")
      return Observable.empty();
    }
    return this.inner.dummyCall(generic);
  }

  public final Observable<Boolean> dummyCall(final String message, final List<String> args) {
    if (!predicate( M.DUMMYCALL, message, args )) {
      // @com.olku.annotations.AutoProxy.Yield
      throw new UnsupportedOperationException("cannot resolve return value.");
    }
    return this.inner.dummyCall(message, args);
  }

  public final Observable<Boolean> dummyCall(final String message, final Object... args) {
    if (!predicate( M.DUMMYCALL, message, args )) {
      // @com.olku.annotations.AutoProxy.Yield(adapter=com.olku.generators.RetRxGenerator.class, value="error")
      return Observable.error(new UnsupportedOperationException("unsupported method call"));
    }
    return this.inner.dummyCall(message, args);
  }

  public final double numericCall() {
    if (!predicate( M.NUMERICCALL )) {
      // @com.olku.annotations.AutoProxy.Yield("0")
      return 0;
    }
    return this.inner.numericCall();
  }

  public final boolean booleanCall() {
    if (!predicate( M.BOOLEANCALL )) {
      // @com.olku.annotations.AutoProxy.Yield("false")
      return false;
    }
    return this.inner.booleanCall();
  }

  public final boolean dispatchDeepLink(@NonNull final Uri deepLink) {
    if (!predicate( M.DISPATCHDEEPLINK, deepLink )) {
      // @com.olku.annotations.AutoProxy.Yield("direct")
      // direct call, ignore predicate result
    }
    return this.inner.dispatchDeepLink(deepLink);
  }

  public final Observable<Boolean> startHearthAnimation() {
    if (!predicate( M.STARTHEARTHANIMATION )) {
      // @com.olku.annotations.AutoProxy.Yield(adapter=com.olku.generators.JustRxGenerator.class, value="true")
      return Observable.just(true);
    }
    return this.inner.startHearthAnimation();
  }

  /* ... other generated helpers customized by flags ... */
}
```
</details>


## 4.3. Usage in project (aka usage with Dagger)

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
        public boolean predicate(@Methods @NonNull final String methodName, final Object... args) {
            return ((BaseFragment) inner).isUpdatable();
        }
    };
}
```

Simplified Java8 lambda version (from sample above):

```java
@NonNull
public MvpView getProxy() {
    return Proxy_MvpView.create(this.view, (m, args) -> ((BaseFragment) inner).isUpdatable());
}
```

## 4.4. Customization of Generated Code

By providing special flags you can customize output of AutoProxy generator:

```java
@AutoProxy(flags = AutoProxy.Flags.ALL)
```

<details>
<summary>Supported Flags - Show code</summary>

```java
/** Special code generation modifier flags. */
@interface Flags {
    /** Default value. */
    int NONE = 0x0000;
    /** Compose static method for easier instance creation. */
    int CREATOR = 0x0001;
    /** Compose afterCall(...) method for all methods in class. */
    int AFTER_CALL = 0x0002;
    /** Compose dispatchByName(...) method that maps string name to a method call. */
    int MAPPING = 0x004;

    /** Compose all additional methods. */
    int ALL = CREATOR | AFTER_CALL | MAPPING;
}
```
</details>

<details>
<summary>Auto Generated Code (CREATOR) - Show code</summary>

Outputs:

```java
  /**
   * Copy this declaration to fix method demands for old APIs:
   *
   * <pre>
   * package java.util.function;
   *
   * public interface BiFunction&lt;T, U, R&gt; {
   *     R apply(T t, U u);
   * }
   * </pre>
   */
  public static KotlinAbstractMvpView create(final KotlinAbstractMvpView instance,
      final BiFunction<String, Object[], Boolean> action) {
    return new Proxy_KotlinAbstractMvpView(instance) {

      @Override
      public boolean predicate(final String methodName, final Object... args) {
        return action.apply(methodName, args);
      }

      @Override
      public <T> T afterCall(final String methodName, final T result) {
        return result;
      };
    };
  }
```
</details>

<details>
<summary>Auto Generated Code (AFTER_CALL & MAPPING) - Show code</summary>

Outputs:

```java
  public abstract <T> T afterCall(@M @NonNull final String methodName, final T result);

  public <T> T dispatchByName(@M @NonNull final String methodName, final Object... args) {
    final Object result;
    if(M.BOOLEANCALL.equals(methodName)) {
      return (T)(result = this.inner.booleanCall());
    }
    if(M.DISPATCHDEEPLINK_DEEPLINK.equals(methodName)) {
      return (T)(result = this.inner.dispatchDeepLink((android.net.Uri)args[0] /*deepLink*/));
    }
    if(M.DUMMYCALL.equals(methodName)) {
      return (T)(result = this.inner.dummyCall());
    }
    if(M.DUMMYCALL_GENERIC.equals(methodName)) {
      return (T)(result = this.inner.dummyCall((java.util.List<java.lang.String>)args[0] /*generic*/));
    }
    if(M.DUMMYCALL_MESSAGE_ARGS.equals(methodName)) {
      return (T)(result = this.inner.dummyCall((java.lang.String)args[0] /*message*/, (java.lang.Object[])args[1] /*args*/));
    }
    if(M.NUMERICCALL.equals(methodName)) {
      return (T)(result = this.inner.numericCall());
    }
    if(M.STARTHEARTHANIMATION.equals(methodName)) {
      return (T)(result = this.inner.startHearthAnimation());
    }
    return (T)null;
  }
```
</details>

In addition generated several parts of the code depends on other options:

<details>
<summary>Auto Generated Code (@Proxy_MvpView.M) - Show code</summary>

```java
@Generated("AutoProxy Auto Generated Code")
public abstract class Proxy_MvpView implements MvpView {
  /* ... other code ... */

  @StringDef({M.BOOLEANCALL, M.DISPATCHDEEPLINK_DEEPLINK, M.DUMMYCALL, M.DUMMYCALL_GENERIC, M.DUMMYCALL_MESSAGE_ARGS, M.NUMERICCALL, M.STARTHEARTHANIMATION})
  public @interface M {
    /** {@link #booleanCall()} */
    String BOOLEANCALL = "booleanCall";

    /** {@link #dispatchDeepLink(android.net.Uri)} */
    String DISPATCHDEEPLINK_DEEPLINK = "dispatchDeepLink_deepLink";

    /** {@link #dummyCall()} */
    String DUMMYCALL = "dummyCall";

    /** {@link #dummyCall(java.util.List<java.lang.String>)} */
    String DUMMYCALL_GENERIC = "dummyCall_generic";

    /** {@link #dummyCall(java.lang.String, java.lang.Object[])} */
    String DUMMYCALL_MESSAGE_ARGS = "dummyCall_message_args";

    /** {@link #numericCall()} */
    String NUMERICCALL = "numericCall";

    /** {@link #startHearthAnimation()} */
    String STARTHEARTHANIMATION = "startHearthAnimation";
  }
}
```

</details>

Binder is a simple method that wrap final class into proxy interface, make it available for chained calls and other features.

<details>
<summary>Auto Generated Code (BINDER) - Show code</summary>

```java
@NonNull
protected static MimicFinalClass bind(@NonNull final FinalClass instance) {
  return new MimicFinalClass() {
    @Override
    public final void dummyCall() {
      instance.dummyCall();
    }

    @Override
    public final boolean returnBoolean() {
      return instance.returnBoolean();
    }

    @Override
    public final void consumer(final String data) {
      instance.consumer(data);
    }

    @Override
    public final void bi_consumer(final String data, final String options) {
      instance.bi_consumer(data, options);
    }

    @Override
    public final String et_function(final String data) {
      return instance.et_function(data);
    }

    @Override
    public final String bi_function(final String data, final String options) {
      return instance.bi_function(data, options);
    }
  };
}
```

</details>



# 5. Advanced Usage (Patterns)

## 5.1. Mimic final class interface

[![Mimic Final Class Feature](https://i.imgur.com/E8u3ir0l.png)](https://imgur.com/E8u3ir0)

**Problem:** system provides class that is impossible to inherit, override or mock (fake). Often it means that class declared as `final` and method of it declared as `final`.

**Solution:** Mimic Final Class Feature. It can be used for decoupling the code implementation from system (or 3rd party) final class that is impossible to mock or fake.

Generated code can be treated as a `binding by method name and signature` when proxy class resolves which method to call of the inner instance in last possible moment (runtime).

`@AutoProxy(innerType = FinalClass.class)` - enables this approach.

<details>
<summary>Code Sample</summary>

```java
/** Step 0: identify final class for proxy */
public final class FinalClass {
    final void dummyCall() { /* do something */ }

    final boolean returnBoolean() { /* do something */ }
}

/** Step 1: create interface with all needed methods that we will need. */
@AutoProxy(innerType = FinalClass.class, flags = AutoProxy.Flags.ALL)
public interface MimicFinalClass {
  /* declare methods of final class that you plan to use.
     Annotate each method by AutoProxy.Yield customization */
}

/** Step 2: replace usage of FinalClass by MimicFinalClass in code. */

/** Step 3: Compose instance of proxy. */
@NonNull
static MimicFinalClass proxy(FinalClass instance) {
    // simplest way to forward all to inner instance
    return Proxy_MimicFinalClass.create(instance, (m, args) -> true);
    // alternative:
    // return Proxy_MimicFinalClass.create(Proxy_MimicFinalClass.bind(instance), (m, args) -> true);
}
```

Code:
- [FinalClass.java](sample/src/main/java/com/olku/autoproxy/sample/FinalClass.java)
- [MimicFinalClass.java](sample/src/main/java/com/olku/autoproxy/sample/MimicFinalClass.java)


</details>

## 5.2. Side effects

[![Chained Side Effects](https://i.imgur.com/akZIN6jl.png)](https://imgur.com/akZIN6j)

How it works? Each instance of customized proxy wrap the instance of another proxy. 
As result we have `wrapped calls` that is the another representation of the `chain of calls`. Think about it as `diving into call-stack`, on each wrapper we go deeper and deeper.

`@AutoProxy(flags = AutoProxy.Flags.CREATE)` feature simplifying wrap's customization.

<details>
<summary>Code sample</summary>

Declaration:

```java
/** Step #0: Create class declaration */
@AutoProxy(flags = AutoProxy.Flags.CREATE)
public abstract class MyClass {
  // Default AutoProxy behavior is THROW exception when predicate() returns FALSE
  void firstMethod();

  @AutoProxy.Yield(RetBool.FALSE)
  boolean secondMethod();
}
```

Customization: 

```java
/** Step #1: Compose side effect */
static MyClass withLogging(MyClass instance) {
  return Proxy_MyClass.create(instance, (m, args) -> {
      Log.i("Called method: " + m + "with args: " + Arrays.toString(args));
      return true;
    });
}

/** Step #2: Another side effect */
static MyClass withFailFirstMethod(MyClass instance) {
  return Proxy_MyClass.create(instance, (m, args) -> {
    return (!M.FIRSTMETHOD.equals(m));
  });
}

/** Step #3: Another side effect */
static MyClass withFalseSecondMethod(MyClass instance) {
  return Proxy_MyClass.create(instance, (m, args) -> {
    return (M.SECONDMETHOD.equals(m) ? false : true);
  });
}
```

Usage:

```java
/* Usage of chain. */
final MyClass instance = 
  withLogging(
    withFailFirstMethod(
      withFalseSecondMethod(
        new MyClassImpl()
  )));

/* ... Use instance in code ... */    
```


</details>

## 5.3. Runtime defined output value

[![Runtime defined output value](https://i.imgur.com/pLznX1Cl.png)](https://imgur.com/pLznX1C)

`@AutoProxy.Yield(Returns.SKIP)` - feature

Feature enables:
- Skipped inner class call
- `predicate` result ignored, yield section is empty
- `afterCall` is responsible for return value

### 5.3.1. Skip Inner Class Call and Simple Return

<details>
<summary>Code sample</summary>

```kotlin
@AutoProxy(flags = AutoProxy.Flags.CREATOR)
abstract class RxJava1Sample {
    @AutoProxy.Yield(Returns.SKIP)
    abstract fun chainedCallSkip(): Boolean?

    /* ... */
}
```

```java
/** Usage: simple case */
final RxJava1Sample instance = /* ... */;
final RxJava1Sample proxy = new Proxy_RxJava1Sample(instance) {
    /* ... */

    @Override
    public <T> T afterCall(@NonNull String methodName, T result) {
        if (M.CHAINEDCALLSKIP.equals(methodName)) {
            return (T) computeSomething();
        }

        return result;
    }
};

private Boolean computeSomething() {
    /* TODO: do own code here */
}
```

</details>

### 5.3.2. Skip Inner Class Call and Return Self Reference

<details>
<summary>Code sample</summary>

```kotlin
@AutoProxy(flags = AutoProxy.Flags.CREATOR)
abstract class KotlinAbstractMvpViewRxJava1 {
    @AutoProxy.Yield(Returns.SKIP)
    abstract fun chainedCallSkip(): KotlinAbstractMvpViewRxJava1

    /* ... */
}
```

```java
/** Usage: of custom return type. */
final AtomicReference<RxJava1Sample> proxy = new AtomicReference<>();
final RxJava1Sample instance = /* ... */;
proxy.set(new Proxy_RxJava1Sample(instance) {
    /* ... */

    @Override
    public <T> T afterCall(@NonNull String methodName, T result) {
        if (Proxy_RxJava1Sample.M.CHAINEDCALLSKIP.equals(methodName)) {
            return (T) proxy.get();
        }

        return result;
    }
});

```

Output:

```java
  @NotNull
  public final KotlinAbstractMvpViewRxJava1 chainedCallSkip() {
    if (!predicate( M.CHAINEDCALLSKIP )) {
      // @com.olku.annotations.AutoProxy.Yield("skipped")
      // skipped call, ignore predicate result. afterCall will be used for return composing.
    }
    final KotlinAbstractMvpViewRxJava1 forAfterCall = null;
    return afterCall(M.CHAINEDCALLSKIP, forAfterCall);
  }
```
</details>

## 5.4. Customize Yield Return Types (Custom return type adapter)

Library provides three generators: 
- rxJava v1.xx: `RetRxGenerator`, `JustRxGenerator`
- rxJava v2.xx: `RetRx2Generator`, `JustRx3Generator`
- rxJava v3.xx: `RetRx2Generator`, `JustRx3Generator`

<details>
<summary>Code sample</summary>

```java
@AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.EMPTY)
abstract fun observableMethod(): Observable<Boolean>

@AutoProxy.Yield(adapter = RetRxGenerator::class, value = RetRx.ERROR)
abstract fun observableMethod(): Observable<Boolean>

@AutoProxy.Yield(adapter = RetRx2Generator::class, value = RetRx.NEVER)
abstract fun flowableMethod(): Flowable<Boolean>

@AutoProxy.Yield(adapter = JustRx2Generator::class, value = "false")
abstract fun flowableMethod(): Flowable<Boolean>

@AutoProxy.Yield(adapter = RetRx3Generator::class, value = RetRx.ERROR)
abstract fun maybeMethod(): Maybe<Boolean>

@AutoProxy.Yield(adapter = JustRx3Generator::class, value = "ignored")
abstract fun completableMethod(): Completable
```

</details>

## 5.5. Decouple MVC, MVP, MVVM patterns (inject mediator)

[![MVC pattern](https://i.imgur.com/CsFyRI9h.png)](https://imgur.com/CsFyRI9)

[![MVP pattern](https://i.imgur.com/SD6go98h.png)](https://imgur.com/SD6go98)

[![MVVM pattern](https://i.imgur.com/5bI3XTKh.png)](https://imgur.com/5bI3XTK)

All based on several refactoring steps:
- introduce IView interface and extract to it common actions/methods from View
- refactor Controler, Model, Presenter, ViewPresenter to use IView instead of View
- annotate IVew as AutoProxy enabled
- instead of instace of IView (VIEW is an implementation of it) start use instance of Proxy_IView.
- that will break relation 1..1 and will make possible implementation 0..1 (or N..1)

## 5.6. Compose FAKE class (experimental)

Create a class for Unit Tests that fake the real implementation. It's and alternative to Mocking approach.

```java
final FinalClass instance = new FinalClass();
final MimicFinalClass proxy =
        // return TRUE
        when(M.RETURNBOOLEAN, (s, o) -> true, 
          // call another implementation
          when(M.DUMMYCALL, MyClass::anotherAction, 
            // No Operation
            when(M.BI_CONSUMER_DATA_OPTIONS, MyClass::noOp,
              // convert final class instance to MimicFinalClass instance
              $MimicFinalClass.bind(instance) 
            )
          )
        );

proxy.returnBoolean(); // will return `true`
proxy.dummyCall(); // ~> forwarded to MyClass::anotherAction
proxy.bi_consumer("data", "options"); // ~> forwarded to MyClass::noOp
```

<details>
<summary>Show helper code</summary>

```java
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
```

```java
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
```

Code:
- [MimicFinalClassTest.java](sample/src/test/java/com/olku/autoproxy/sample/MimicFinalClassTest.java)

</details>

# 6. Troubles

http://www.vogella.com/tutorials/GitSubmodules/article.html

## 6.1. Reset submodule to remote repository version

```bash
git submodule foreach 'git reset --hard'
# including nested submodules
git submodule foreach --recursive 'git reset --hard'
```

## 6.2. Enable Tracing

Add those lines to `local.properties` file:

```bash
#
# Make Kotlin Annotation Processor verbose
#
kapt.verbose=true

```

## 6.3. How to Debug?

Pre-steps:

- `gradle/version-up.sh` for composing `version.properties` file with a future version
- `./gradlew install` to publish version in local maven repository

Debugging:

1. Go to terminal and execute `./gradlew --stop`
2. Enable/UnComment in gradle.properties line `#org.gradle.debug=true`
3. Switch IDE to run configuration `Debug Annotation Processor`
4. Run IDE configuration
5. Open terminal and execute `./gradlew clean assembleDebug`

[more details ...](https://stackoverflow.com/questions/8587096/how-do-you-debug-java-annotation-processors-using-intellij)

## 6.4. How to Change/Generate GPG signing key?

```bash
brew install gpg
gpg --gen-key

# pub   rsa2048 2020-04-21 [SC] [expires: 2022-04-21]
#       6B38C8BB4161F9AF99133B4B8DF78BA02F1868F9
# uid                      Oleksandr Kucherenko <ku.......x@gmail.com>
# sub   rsa2048 2020-04-21 [E] [expires: 2022-04-21]
gpg -a --export 6B38C8BB4161F9AF99133B4B8DF78BA02F1868F9 >gpg.public.key
gpg -a --export-secret-key 6B38C8BB4161F9AF99133B4B8DF78BA02F1868F9 >gpg.private.key
```

- open https://bintray.com/profile/edit
- Select `GPG Signing`
- First set the public key and press Update
- Than press Private Key `Click to Change` and upload private key
- Store Passpharse in `credentials.gradle`

```diff
- ext.gpg_password = '<secret>'
+ ext.gpg_password = 'my_new_and_secure_passphrase'
```

## 6.5. How to Publish?

Edit file `credentials.gradle` in the root of the project:

```diff
- ext.bintray_dryrun = true
+ ext.bintray_dryrun = false
```

Disable DRY_RUN mode, that will allow binaries upload to the bintray side. Than Run:

```bash
./gradlew bintrayUpload
```

# 7. Roadmap

- [x] Incremental Annotation Processing
- [x] Create constants class for method names
- [x] method name parameter annotated by custom annotation with @StringDef
- [x] static `create` method that allows proxy creation with use of lambda
- [x] `dispatchByName` method that calls specific inner instance method with provided parameters
- [x] customization auto-generation flags, configure demands to helper code
- [x] Proxy for final classes
- [x] Yield predicate with customizable return (lambda expression) (use SKIP approach)
- [x] Add Support for RxJava v2
- [x] Add Support for RxJava v3
- [x] customize Prefix of generated class "Proxy_" can be replaced by "Fake_" or "Stub_"
- [ ] Allow Mutable inner instance, replace instance in runtime for proxy
- [ ] Add Support for Kotlin language (generate code in Kotlin)

# 8. License

MIT &copy; Oleksander Kucherenko (ArtfulBits IT AB), 2017-2020
