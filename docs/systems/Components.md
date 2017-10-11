# HellHound Components
A **Component** is a tiny framework for managing the lifecycle, dependencies
and dataflow of software components which have runtime state.

A component is similar in spirit to the definition of an object in Object-Oriented Programming.
This does not alter the primacy of pure functions and immutable data structures in Clojure as
a language. Most functions are just functions, and most data are just data. Components are intended
to help manage stateful resources within a functional paradigm.

## Advantages of the Component Model

Large applications often consist of many stateful processes which must be started and stopped in
a particular order. The component model makes those relationships explicit and declarative, instead
of implicit in imperative code.

Components provide some basic guidance for structuring a **HellHound** application, with boundaries
between different parts of a system. Components offer some encapsulation, in the sense of grouping
together related entities. Each component receives references only to the things it needs, avoiding
unnecessary shared state. Instead of reaching through multiple levels of nested maps, a component
can have everything it needs at most one map lookup away.

Instead of having mutable state (atoms, refs, etc.) scattered throughout different namespaces, all
the stateful parts of an application can be gathered together. In some cases, using components may
eliminate the need for mutable references altogether, for example to store the "current" connection
to a resource such as a database. At the same time, having all state reachable via a single
[**system**](./README.md#overview) map makes it easy to reach in and inspect any part of the application
from the REPL.

The component dependency model makes it easy to swap in **stub** or **mock** implementations of a component
for testing purposes, without relying on time-dependent constructs, such as with-redefs or binding, which are
often subject to race conditions in multi-threaded code.

Having a coherent way to set up and tear down all the state associated with an application enables rapid
development cycles without restarting the JVM. It can also make unit tests faster and more independent,
since the cost of creating and starting a system is low enough that every test can create a new instance
of the system.

## Disadvantages of the Component Model

For small applications, declaring the dependency relationships among components may actually be more work than
manually starting all the components in the correct order or even not using component model at all. Everything
comes at a price.

The [**system**](./README.md#overview) map produced by **HellHound** is a complex map and it is typically too
large to inspect visually. But there are enough helper functions in `[hellhound.system](#)` namespace to help
you with it.

You must explicitly specify all the dependency relationships among components: the code cannot discover these
relationships automatically.

Finally, **HellHound** system forbids cyclic dependencies among components. I believe that cyclic dependencies
usually indicate architectural flaws and can be eliminated by restructuring the application. In the rare case
where a cyclic dependency cannot be avoided, you can use mutable references to manage it, but this is outside
the scope of components.

## Implementaion
Components are the main parts of HellHound systems. Basically each components is an implementation of `IComponent`
protocol. The protocol which defines a component functionality. By default HellHound implements `IComponent`
protocols for hashmaps only. So we can define components in form of maps.

In order to define a component a map should should contains following keys (All the keys should be namespaced
keyword under `hellhound.component`):

### `name`:
The name of the component. It should be a namespaced keyword.
This key is **mandatory**.

### `start-fn`:
A function which takes the component map as the only argument
and return the component with the necessary keys attached to it. This
function is responsible for **starting** the component.
This key is **mandatory**.

### `stop-fn`:
A function which takes the component map as the only argument
and return the component with the necessary keys attached to it. This
function is responsible for **stoping** the component.
This key is **mandatory**.

###`depends-on`:
This key specifies all the components which are the
 dependencies of the current component. A collection  of components
name.
This key is optional.

### `input-stream-fn`:
A function which returns a `manifold` as the input
of the component. You rarely need to use this key for a component.
This key optional.

### `output-stream-fn`:
A function which returns a `manifold` as the output
of the component. You rarely need to use this key for a component.
This key optional.

So as an example:

```clojure
  (def sample-component
    {:hellhound.component/name :sample.core/component-A
     :hellhound.component/start-fn (fn [component] component)
     :hellhound.component/stop-fn (fn [component] component)
     :hellhound.component/depends-on [:sample.core/component-B]})
```

In this example `start-fn` and `stop-fn` don't do anything.
