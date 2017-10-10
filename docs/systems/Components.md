# HellHound Components
Components are the main parts of HellHound systems. Basically
each components should be an implementation of `IComponent`. The
protocol which defines a component functionality. By default HellHound
implements `IComponent` protocols for hashmaps only. So we can define
components in form of maps.

In order to define a component a map should should contains following
keys (All the keys should be namespaced keyword under `hellhound.component`):

* `name`: The name of the component. It should be a namespaced keyword.
This key is **mandatory**.

* `start-fn`: A function which takes the component map as the only argument
and return the component with the necessary keys attached to it. This
function is responsible for **starting** the component.
This key is **mandatory**.

* `stop-fn`: A function which takes the component map as the only argument
and return the component with the necessary keys attached to it. This
function is responsible for **stoping** the component.
This key is **mandatory**.

* `depends-on`: This key specifies all the components which are the
 dependencies of the current component. A collection  of components
name.
This key is optional.

* `input-stream-fn`: A function which returns a `manifold` as the input
of the component. You rarely need to use this key for a component.
This key optional.

* `output-stream-fn`: A function which returns a `manifold` as the output
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
