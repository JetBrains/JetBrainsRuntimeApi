[![JetBrains incubator project](https://jb.gg/badges/official.svg)](https://github.com/JetBrains#jetbrains-on-github)

# JBR API

**_JBR API_** is an interface for the functionality specific to 
[JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime) aka JBR.
JBR API provides a standalone jar with classes and interfaces representing various APIs
allowing the client code to be compiled against any JDK, while enjoying unique
features provided by JBR at run time without worrying about compatibility and runtime errors.

## Quickstart

Any feature exposed via JBR API begins with a **_service_**, which is a basic
unit of JBR API. Each service has three related methods in the `JBR` class:
* `JBR.get<NAME>()` - returns the service instance if it's supported or `null`.
* `JBR.get<NAME>(Extensions...)` - returns the service instance with the set of optional extensions enabled (see [below](#extensions)).
* `JBR.is<NAME>Supported()` - a convenience method equivalent to `JBR.get<NAME>() != null`.

```java
if (JBR.isSomeServiceSupported()) {
    JBR.getSomeService().doSomething();
}
// or
SomeService service = JBR.getSomeService();
if (service != null) {
    service.doSomething();
}
```
> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/tip.svg">
>   <img alt="Tip" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/tip.svg">
> </picture><br>
>
> More details with a list of available services can be found in the
> [javadoc](https://jetbrains.github.io/JetBrainsRuntimeApi).

### Extensions

API methods marked with `@Extension` are *optional*, meaning that the service would still be
considered supported even if some of its extension methods are not.
Such extensions must be explicitly enabled when retrieving the service with `JBR.get<NAME>(Extensions...)`.
Extension methods may appear not only in services but in regular interfaces too.
In that case the set of enabled extensions is implicitly propagated to objects retrieved from that service.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/example.svg">
>   <img alt="Example" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/example.svg">
> </picture><br>
>
> ```java
> SomeService service;
> Foo foo;
> 
> service = JBR.getSomeService();
> foo = service.getFoo();
> foo.bar(); // UnsupportedOperationException: Foo.bar - extension BAR is disabled
> 
> service = JBR.getSomeService(Extensions.BAR);
> foo = service.getFoo();
> foo.bar(); // OK
> ```

## Versioning

JBR API releases follow [semantic versioning](https://semver.org).
API and implementation versions can be retrieved from the `JBR` class:
* `JBR.getApiVersion()` - the version of `jbr-api.jar` currently used.
* `JBR.getImplVersion()` - the version of JBR API implemented by the current runtime.

> <picture>
>   <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/light-theme/info.svg">
>   <img alt="Info" src="https://raw.githubusercontent.com/Mqxx/GitHub-Markdown/f167aefa480e8d37e9941a25f0b40981b74a47be/blockquotes/badge/dark-theme/info.svg">
> </picture><br>
>
>  _Versions should not be used for any purpose other than logging._
>
> Neither the API nor implementation versions are used in compatibility
> checks or when determining the service availability.
> 
> However, you can assume that when
> *impl.major == api.major && impl.minor >= api.minor*,
> all services currently present in that JBR API are **guaranteed** to be supported
> by that implementation.
> 

## References

* [JBR API documentation](https://jetbrains.github.io/JetBrainsRuntimeApi)
* [JBR API development guide](CONTRIBUTING.md)
* [JetBrainsRuntime (JBR)](https://github.com/JetBrains/JetBrainsRuntime)
