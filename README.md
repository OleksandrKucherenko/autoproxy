# AutoProxy

[![Build Status](https://travis-ci.org/OleksandrKucherenko/autoproxy.svg?branch=master)](https://travis-ci.org/OleksandrKucherenko/autoproxy)
[ ![Download](https://api.bintray.com/packages/kucherenko-alex/android/com.olku%3Aautoproxy/images/download.svg) ](https://bintray.com/kucherenko-alex/android/com.olku%3Aautoproxy/_latestVersion)

Annotation Processing Library.

Generates proxy class on top of interface/abstract class, that allows to intercept calls.

Also known as a design pattern: proxy, delegate, interceptor.

[![Diagram](https://i.imgur.com/vvgUtw7h.png)](https://imgur.com/vvgUtw7)

UML PROXY PATTERN:

[![Proxy Pattern](https://i.imgur.com/EYDGCB1h.png)](https://imgur.com/EYDGCB1)

# Why should I use it?

## Use Cases

1. create proxy class that ignore all calls till UI is in a right lifecycle state; Library solves common Mvp View problem: call of view from presenter when
view is already detached from Activity. (delayed updated call)
2. inject side-effects on top of existing implementation. Example: log all the calls to proxy, control sequence and performance;
3. Lazy initialization, record all calls and playback them on instance that reach a specific state;
4. Mutability injection into AutoValue Builder (complex data migration from one AutoValue instance to another). AutoValue classes as DB with automatic Primary Key auto-increment;
5. Mocks vs Fakes. Generate a fake implementation by annotating primary interface/abstract class
6. Composing Adapter (Frontend) for another API 

Library gives a bigger freedom if you think for a second about it:

- auto-generated Proxy class is simple and does not have any performance impacts. No Reflection. All resolved during the compilation time. Survive ProGuard optimization/obfuscation. 
- used in library approach allows custom generators of code/results. Unknown types is super easy to support.
- Allows to decouple main application business logic from different side-effects and dependencies

# Concepts

## Predicate

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

## AfterCall

From time to time exists situations when we need to intercept and modify results of inner call.
In that case library provides `@AutoProxy.AfterCall` annotation.

Declaration:

```java
/** Abstract class. */
@AutoValue
public abstract class ParkingArea {

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

Change of internal proxy pattern:

```java
  public final ParkingArea build() {
    if (!predicate( "build" )) {
      throw new UnsupportedOperationException("cannot resolve return type.");
    }

    return afterCall("build", this.inner.build());
  }

```

# Usage

You can use it as a submodule or as compiled libs.

## Step #1 : configure dependency

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/kucherenko-alex/android"
    }
}

dependencies{
    /* AutoProxy generator */
    compileOnly 'com.olku:autoproxy-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-annotations:+'
    compileOnly 'com.olku:autoproxy-rx-generators:+'

    annotationProcessor 'com.olku:autoproxy-rx-generators:+'
    annotationProcessor 'com.olku:autoproxy-processor:+'

}
```

## Step #1.1 : OR attach as a submodule

attach repository as a submodule:

```bash
# initialize project for submodules usage
git submodule init

# add submodule
git submodule add https://github.com/OleksandrKucherenko/autoproxy.git modules/autoproxy

# update project recursively and pull all submodules
git submodule update --init --recursive
```

## Step #1.2: include submodule into project

`app/build.gradle`:

```groovy
    /* AutoProxy generator */
    compileOnly project(':modules:autoproxy:autoproxy-annotations')
    compileOnly project(':modules:autoproxy:autoproxy-rx-annotations')
    compileOnly project(':modules:autoproxy:autoproxy-rx-generators')

    /* For Java Projects */
    annotationProcessor project(':modules:autoproxy:autoproxy-rx-generators')
    annotationProcessor project(':modules:autoproxy:autoproxy-processor')

    /* OR for Kotlin Projects */
    kapt project(':modules:autoproxy-rx-generators')
    kapt project(':modules:autoproxy-processor')

```

`settings.gradle`:

```groovy
include ':modules:autoproxy:autoproxy-annotations'
include ':modules:autoproxy:autoproxy-generators'
include ':modules:autoproxy:autoproxy-rx-annotations'
include ':modules:autoproxy:autoproxy-rx-generators'
include ':modules:autoproxy:autoproxy-processor'
```

## Step #3: Declare proxy class specifics

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

## Step 4: Code Generation Results

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

  @StringDef({M.BOOLEANCALL, M.DISPATCHDEEPLINK_DEEPLINK, M.DUMMYCALL, M.DUMMYCALL_GENERIC, M.DUMMYCALL_MESSAGE_ARGS, M.NUMERICCALL, M.STARTHEARTHANIMATION})
  public @interface M {
    /**
     * {@link #booleanCall()}
     */
    String BOOLEANCALL = "booleanCall";

    /**
     * {@link #dispatchDeepLink(android.net.Uri)}
     */
    String DISPATCHDEEPLINK_DEEPLINK = "dispatchDeepLink_deepLink";

    /**
     * {@link #dummyCall()}
     */
    String DUMMYCALL = "dummyCall";

    /**
     * {@link #dummyCall(java.util.List<java.lang.String>)}
     */
    String DUMMYCALL_GENERIC = "dummyCall_generic";

    /**
     * {@link #dummyCall(java.lang.String, java.lang.Object[])}
     */
    String DUMMYCALL_MESSAGE_ARGS = "dummyCall_message_args";

    /**
     * {@link #numericCall()}
     */
    String NUMERICCALL = "numericCall";

    /**
     * {@link #startHearthAnimation()}
     */
    String STARTHEARTHANIMATION = "startHearthAnimation";
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
                public boolean predicate(@Methods @NonNull final String methodName, final Object... args) {
                    return ((BaseFragment) inner).isUpdatable();
                }
            };
        }

```

## Customization of Generated Code

By providing special flags you can customize output of AutoProxy generator:

```kotlin
@AutoProxy(flags = AutoProxy.Flags.ALL)
abstract class KotlinAbstractMvpView {
  /* ... */
}
```

Outputs:

```java
  public abstract <T> T afterCall(@M @NonNull final String methodName, final T result);

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

## Usage patterns

### Mimic final class interface

Can be used for decoupling the code implementation from system (or 3rd party) final class that
is impossible to mock or fake.

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
}
```

# Troubles

http://www.vogella.com/tutorials/GitSubmodules/article.html

## Reset submodule to remote repository version

```bash
git submodule foreach 'git reset --hard'
# including nested submodules
git submodule foreach --recursive 'git reset --hard'
```

## How to debug?

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

## How to Change/Generate GPG signing key?

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

## How to publish?

Edit file `credentials.gradle` in the root of the project:

```diff
- ext.bintray_dryrun = true
+ ext.bintray_dryrun = false
```

Disable DRY_RUN mode, that will allow binaries upload to the bintray side. Than Run:

```bash
./gradlew bintrayUpload
```

# Roadmap

- [x] Incremental Annotation Processing
- [x] Create constants class for method names
- [x] method name parameter annotated by custom annotation with @StringDef
- [x] static `create` method that allows proxy creation with use of lambda
- [x] `dispatchByName` method that calls specific inner instance method with provided parameters
- [x] customization auto-generation flags, configure demands to helper code
- [ ] customize Prefix of generated class "Proxy_" can be replaced by "Fake_" or "Stub_"
- [x] Proxy for final classes
- [ ] Allow Mutable inner instance, replace instance in runtime for proxy
- [ ] Yield predicate with customizable return (lambda expression)
- [ ] Add Support for RxJava v2
- [ ] Add Support for Kotlin language (generate code in Kotlin)
- [ ] Add Support for RxJava v3
