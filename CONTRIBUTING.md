# Developing JBR API

1. [How does it work](#how-does-it-work)
2. [Development setup](#development-setup)
3. [Adding new API](#adding-new-api)
4. [Writing tests](#writing-tests)
5. [Contributing your changes](#contributing-your-changes)


## How does it work

Core functionality of JBR API consists of dynamic linkage of an **_interface_**
with its **_target_** implementation at run time. It does so by generating proxy
classes implementing the given interfaces and delegating all calls into the actual
implementation.

```
╭───────────────────────╮    ╭───────────────╮        
│         CLIENT        │    │      JBR      │              
│╭─────────────────────╮│    │╭─────────────╮│               
││jetbrains.runtime.api││ => ││  java.base  ││                
││ [I] Foo             ││    ││ [C] Bar     ││  
│╰─────────────────────╯│    │╰─────────────╯│  
╰───────────────────────╯    ╰───────────────╯
```
```java
// module jetbrains.runtime.api
public interface Foo {
    void hello();
}
// module java.base
class Bar {
    void hello() {
        System.out.println("Hello JBR API!");
    }
}
// generated at runtime
public class FooProxy implements Foo {
    private final Bar bar;
    @Override
    public void hello() {
        bar.hello();
    }
}
```

JBR API build produces a multi-release jar compatible with Java 8 and newer.
Code in JBR API must conform to Java 8 with the following exceptions:

1. There is a `module-info.java` defining the `jetbrains.runtime.api` module,
   it is included into Java 9+ builds.
2. `@Deprecated` annotation allows `forRemoval` member despite being added in Java 9.

## Development setup

1. First off you need to have JetBrains Runtime (JBR) [cloned](https://github.com/JetBrains/JetBrainsRuntime)
   into `JetBrainsRuntime` and [built](https://github.com/JetBrains/JetBrainsRuntime#configuring-the-build-environment).
   Development of JBR itself is not covered here.

2. `cd JetBrainsRuntime` and `make jbr-api` - this will initialize a nested repo
   under `JetBrainsRuntime/jbr-api` and build JBR API into `JetBrainsRuntime/jbr-api/out/`.

   > <picture>
   >   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
   >   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
   > </picture><br>
   >
   > If you have previously built JBR API, `make jbr-api`
   > may issue a warning about an outdated branch. It's advised to keep
   > your JBR API branch up-to-date with `origin/main`.

   The build result will be
   `JetBrainsRuntime/jbr-api/out/jbr-api-SNAPSHOT.jar`
   It will also be installed into your local Maven repository as
   `com.jetbrains:jbr-api:SNAPSHOT`. The easiest way to try the
   new JBR API is to add it as Maven artifact to your project.
   JBR API will then get updated automatically every time you build it with `make jbr-api`.

   > <picture>
   >   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
   >   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
   > </picture><br>
   >
   > JBR API can be built standalone, if needed.
   > In this case JDK 18 or newer is required:
   > ```shell
   > bash tools/build.sh full /path/to/jdk
   > ```
   > There are build types other than `full`,
   > see `build.sh` for more info.

3. Create a new feature branch. You may need to update remote to your
   fork if you don't have write access to the JBR API repository.
   It's easy to do via IDEA: *Git -> Manage Remotes...*


## Adding new API

Having **_interface_** and **_target_** you just need to bind them
together using `@Provided` and `@Provides` annotations:
- `@Provided` is for **_interface_**, meaning that its implementation is **_provided_** by JBR API.
- `@Provides` is for **_target_**, meaning that it **_provides_** an implementation to JBR API.

In JBR these annotations are nested members of `com.jetbrains.exported.JBRApi`,
accepting the name of the corresponding JBR API type it is bound to.

The following table summarizes possible annotation combinations: 

| JBR API                     | JBR                                       | Note                                                                                          |
|-----------------------------|-------------------------------------------|-----------------------------------------------------------------------------------------------|
| `@Service`<br/>`@Provided`  | `@JBRApi.Service`<br/>`@JBRApi.Provides`  | **_Service_** is a JBR API entry point. Used by a client, implemented by JBR.                 |
| `@Provided`                 | `@JBRApi.Provides`                        | Regular JBR API type. Used by a client, implemented by JBR.                                   |
| `@Provides`                 | `@JBRApi.Provided`                        | Usually used for callbacks. Used by JBR, **_target_** is on JBR API side.                     |
| `@Provided`<br/>`@Provides` | `@JBRApi.Provided`<br/>`@JBRApi.Provides` | This is rarely used when there can be multiple implementations on both JBR and JBR API sides. |

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
>   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
> </picture><br>
>
> JBR API doesn't care about visibility modifiers:
> the implementation can be private and in a package not exported by its module,
> but still be discoverable by JBR API.


### TLDR

1. If you want to add a method to an existing type, use [extension methods](#extension-methods).
2. If your new functionality doesn't fit into any existing service, [create your own](#services).
3. As a service doesn't have any state, it can often be implemented with [static methods](#static-implementation-methods).
4. If you pass/return JBR API types to/from methods, consider the rules of [type conversion](#type-conversion);


### Services

JBR API **_services_** are marked with `@Service` for both **_interface_** and **_target_**.
Services are instantiated by JBR via the static factory method `create()`, or the no-arg constructor.
Factory methods and no-arg constructors can throw `JBRApi.ServiceNotAvailableException`
to indicate that the service is unavailable for some reason. In this case the corresponding JBR API
method `JBR.get<NAME>...` will return `null` to the client.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> ```java
> // JetBrainsRuntime/jbr-api/src/com/jetbrains/MyService.java
> package com.jetbrains;
> 
> @Service
> @Provided
> public interface MyService {
>     void print(String string);
> }
> ```
> ```java
> // JetBrainsRuntime/src/java.desktop/share/classes/javax/swing/JOptionPane.java
> // ...
> @JBRApi.Service
> @JBRApi.Provides("MyService")
> private static class MyServiceImpl {
>     // This factory method is preferred over the constructor
>     private static MyServiceImpl create() {
>         return new MyServiceImpl();
>     }
>     private MyServiceImpl() {
>         throw new JBRApi.ServiceNotAvailableException();
>     }
>     void print(String string) {
>         showMessageDialog(null, string);
>     }
> }
> // ...
> ```

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
>   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
> </picture><br>
>
> `@JBRApi.Provided` and `@JBRApi.Provides` accept a fully qualified class name but `com.jetbrains`
> can be omitted, so both `MyService` and `com.jetbrains.MyService` are fine in the example above.


### Static implementation methods

A static method can be marked as an implementation for specific **_interface_** method using
`@JBRApi.Provides("Interface#method")`.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> ```java
> // JetBrainsRuntime/jbr-api/src/com/jetbrains/MyService.java
> package com.jetbrains;
> 
> @Service
> @Provided
> public interface MyService {
>     void printForMyService(String string);
> }
> ```
> ```java
> // JetBrainsRuntime/src/java.desktop/share/classes/javax/swing/JOptionPane.java
> // ...
> @JBRApi.Provides("MyService#printForMyService")
> private static void printForMyService(String string) {
>     showMessageDialog(null, string);
> }
> // ...
> ```

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
>   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
> </picture><br>
>
> `@JBRApi.Provides` also allows omitting the method name when used on a static method, if it matches
> the name of the static method itself. In the example above all combinations are fine:
> - `MyService`
> - `MyService#printForMyService`
> - `com.jetbrains.MyService`
> - `com.jetbrains.MyService#printForMyService`

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
>   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
> </picture><br>
>
> **_Service_** may not have its **_target_** class at all, with all methods
> implemented statically.


### Extension methods

Extension methods are used to add functionality to existing objects while still being compatible
with older runtimes that do not support said functionality.
Extension names are added to the `Extensions` enumeration, then extension methods are marked with
`@Extension`.
Extensions must be explicitly named when retrieving the service with `JBR.get<NAME>(Extensions...)`
on the client side to be able to use their corresponding methods.
This is the recommended way to add functionality to existing types.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> ```java
> // JetBrainsRuntime/jbr-api/src/com/jetbrains/Extensions.java
> // ...
> public enum Extensions {
>     // ...
>     SOME_FEATURE
> }
> ```
> ```java
> // JetBrainsRuntime/jbr-api/src/com/jetbrains/MyService.java
> package com.jetbrains;
> 
> @Service
> @Provided
> public interface MyService {
>     void foo();
>     void bar();
>     @Extension(Extensions.SOME_FEATURE)
>     void newMethod();
> }
> ```
> ```java
> MyService myService = JBR.getMyService(Extensions.SOME_FEATURE);
> if (myService != null) myService.newMethod();
> ```


### Fallback implementation

Service may have a client-side fallback implementation.
It is used when current runtime doesn't support given service.
`JBR.is<NAME>Supported()` will still return `true`, though.
Fallback implementation class is specified by adding
`@Fallback(<IMPL_CLASS>)` annotation to the service.
Implementation class must be inherited from the service
and have a no-arg constructor.
99% of the time you wouldn't want your fallback class to be public.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> ```java
> // JetBrainsRuntime/jbr-api/src/com/jetbrains/MyService.java
> package com.jetbrains;
> 
> @Service
> @Provided
> @Fallback(MyService_Fallback.class)
> public interface MyService {
>     // ...
> }
> final class MyService_Fallback implements MyService {
>     MyService_Fallback() {}
>     // ...
> }
> ```


### Type conversion

JBR API automatically converts mapped types when passing objects back and forth between the client and JBR.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> ```java
> // JetBrainsRuntime/jbr-api/src/com/jetbrains/MyService.java
> package com.jetbrains;
> 
> @Service
> @Provided
> public interface MyService {
>     Printer createPrinter(Formatter formatter);
> 
>     @Provided
>     interface Printer {
>         void print(String string);
>     }
> 
>     @Provides
>     interface Formatter {
>         String format(String string);
>     }
> }
> ```
> ```java
> // JetBrainsRuntime/src/java.desktop/share/classes/javax/swing/JOptionPane.java
> // ...
> @JBRApi.Service
> @JBRApi.Provides("MyService")
> private static class MyServiceImpl {
>     PrinterImpl createPrinter(FormatterCallback formatter) {
>         return new PrinterImpl(formatter);
>     }
> 
>     @JBRApi.Provides("MyService.Printer")
>     private static class PrinterImpl {
>         private final FormatterCallback formatter;
>         PrinterImpl(FormatterCallback formatter) {
>             this.formatter = formatter;
>         }
>         void print(String string) {
>             showMessageDialog(null, formatter.format(string));
>         }
>     }
> 
>     @JBRApi.Provided("MyService.Formatter")
>     interface FormatterCallback {
>         String format(String string);
>     }
> }
> // ...
> ```
> Note how `Printer createPrinter(Formatter formatter)` on the JBR API side translates into
> `PrinterImpl createPrinter(FormatterCallback formatter)` on the JBR side.

When the JBR API backend determines the service availability it also considers
all mapped types reachable from that service. Therefore, a failure
to find an implementation for a type used (even indirectly) by a
service will render that service unsupported.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> If we rename `PrinterImpl#print` to `PrinterImpl#print2` in
> the example above, the JBR API backend will fail to bind `Printer` and `PrinterImpl`
> together due to a missing implementation for `Printer#print`.
> This will cause whole `MyService` to become unavailable with
> `JBR.getMyService()` returning `null`.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
>   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
> </picture><br>
>
> You can troubleshoot mapping and other issues by using the
> `-Djetbrains.runtime.api.verbose=true` system property when running your tests.

When building JBR API via `make jbr-api` or `build.sh`, it will report
a digest of API changes with their compatibility status.
The *MAJOR* status means that you've broken compatibility and need to
revise your API changes.


## Writing tests

Tests for JBR API functionality are kept in `jbr-api/tests`.
It's a single set of JTreg tests, which is run against
each JBR supporting the corresponding JBR API version.
Each service usually has its own test like `MyServiceTest`.
JBR API tests are [block box tests](https://en.wikipedia.org/wiki/Black-box_testing)
verifying the observed behavior via public API.
These usually include examples of the service's intended usage.
If you need to test the new functionality in the
[white box manner](https://en.wikipedia.org/wiki/White-box_testing),
consider writing tests in JBR instead.


## Contributing your changes

When your new API is ready, you have built both JBR and JBR API, tested them
together and made sure you didn't break compatibility or anything else,
it's time to contribute your changes.
Commiting your changes from IDEA is convenient as it automatically creates two
commits into JBR API and JBR with the same commit message.

All JBR API changes *must* go through GitHub Pull Requests. It's convenient to
review both JBR API and JBR changes simultaneously, so it would be nice to provide
links to one another.

After the bot had checked your changes, and they got approved by at least one
reviewer they will be merged into the `main` branch and will be assigned a
new version.
Together with the new version each change also gets:
1. A new tag in the form `v1.2.3`.
2. A new GitHub release.
3. An updated [javadoc](https://jetbrains.github.io/JetBrainsRuntimeApi).

That's it, thanks for contributing!
Just a few last words to make your life easier and JBR API better:
1. JBR API is intended to be a high-level API -
   provide only what's needed for the user, don't make Lego.
2. Plan API ahead - once it's released, it still *sometimes*
   can be extended, but never amended.
3. Don't break compatibility - major changes are always long planned,
   with everything possible done to mitigate the impact of incompatible
   changes. If that's not your case - you are doing it wrong.
4. When in doubt, contact me, I will try to help - **@nikita.gubarkov**.
   
