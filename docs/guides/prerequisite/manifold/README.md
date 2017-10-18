# Manifold
---
This library provides basic building blocks for asynchronous programming, and can be used as a translation layer
between libraries which use similar but incompatible abstractions. Manifold provides two core abstractions:
**deferreds**, which represent a single asynchronous value, and **streams**, which represent an ordered sequence
of asynchronous values.

HellHound uses manifolds almost everywhere so it's a good idea learn about this fantastic library which brought to us
by [@ztellman](https://github.com/ztellman) and the awesome [contributors](https://github.com/ztellman/manifold/graphs/contributors)
of this library.

Long story short, **Manifold** library provides an awesome asynchronous values by **deferreds** and a super useful abstraction
for **streams**.

> **NOTE**: If you're interested in Manifold you might want to know about the [rationale](http://aleph.io/manifold/rationale.html) behind it.
