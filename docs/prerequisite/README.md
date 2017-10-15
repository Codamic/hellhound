# Prerequisite
Before learning about **HellHound** and how to use it. You need to know about several concepts and libraries
which are heavily used in **HellHound**. If you already know about the following topics, you can safely skip
this chapter and jump to [Getting Started](../getting_started/README.md) guide.

## Stream Processing
TBA
## Commander Pattern
TBA
## Manifold
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

### Deferred
> **NOTE**: Most of the contents of this section extract from the original docs in [here](http://aleph.io/manifold/deferreds.html)

A deferred in Manifold is similar to a Clojure promise:

```clojure
> (require '[manifold.deferred :as d])
nil

> (def d (d/deferred))
#'d

> (d/success! d :foo)
true

> @d
:foo
```

However, similar to Clojure's futures, deferreds in Manifold can also represent errors. Crucially, they also allow for
callbacks to be registered, rather than simply blocking on dereferencing.

```clojure

> (def d (d/deferred))
#'d

> (d/error! d (Exception. "boom"))
true

> @d
Exception: boom
```

```clojure
> (def d (d/deferred))
#'d

> (d/on-realized d
    (fn [x] (println "success!" x))
    (fn [x] (println "error!" x)))
<< ... >>

> (d/success! d :foo)
success! :foo
true
```
#### composing with deferreds

Callbacks are a useful building block, but they're a painful way to create asynchronous workflows.  In practice, no one should ever use `on-realized`.

Instead, they should use `manifold.deferred/chain`, which chains together callbacks, left to right:

```clj
> (def d (d/deferred))
#'d

> (d/chain d inc inc inc #(println "x + 3 =" %))
<< ... >>

> (d/success! d 0)
x + 3 = 3
true
```

`chain` returns a deferred representing the return value of the right-most callback.  If any of the functions returns a deferred or a value that can be coerced into a deferred, the chain will be paused until the deferred yields a value.

Values that can be coerced into a deferred include Clojure futures, Java futures, and Clojure promises.

```clj
> (def d (d/deferred))
#'d

> (d/chain d
    #(future (inc %))
    #(println "the future returned" %))
<< ... >>

> (d/success! d 0)
the future returned 1
true
```

If any stage in `chain` throws an exception or returns a deferred that yields an error, all subsequent stages are skipped, and the deferred returned by `chain` yields that same error.  To handle these cases, you can use `manifold.deferred/catch`:

```clj
> (def d (d/deferred))
#p

> (-> d
    (d/chain dec #(/ 1 %))
    (d/catch Exception #(println "whoops, that didn't work:" %)))
<< ... >>

> (d/success! d 1)
whoops, that didn't work: #error {:cause Divide by zero :via [{:type java.lang.ArithmeticException ...
true
```

Using the `->` threading operator, `chain` and `catch` can be easily and arbitrarily composed.

To combine multiple deferrable values into a single deferred that yields all their results, we can use `manifold.deferred/zip`:

```clj
> @(d/zip (future 1) (future 2) (future 3))
(1 2 3)
```

Finally, we can use `manifold.deferred/timeout!` to register a timeout on the deferred which will yield either a specified timeout value or a `TimeoutException` if the deferred is not realized within `n` milliseconds.

```clj
> @(d/timeout!
     (d/future (Thread/sleep 1000) :foo)
     100
     :bar)
:bar
```

Note that if a timeout is placed on a deferred returned by `chain`, the timeout elapsing will prevent any further stages from being executed.

#### `future` vs `manifold.deferred/future`

Clojure's futures can be treated as deferreds, as can Clojure's promises.  However, since both of these abstractions use a blocking dereference, in order for Manifold to treat it as an asynchronous deferred value it must allocate a thread.

Wherever possible, use `manifold.deferred/deferred` instead of `promise`, and `manifold.deferred/future` instead of `future`.  They will behave identically to their Clojure counterparts (`deliver` can be used on a Manifold deferred, for instance), but allow for callbacks to be registered, so no additional threads are required.

#### let-flow

Let's say that we have two services which provide us numbers, and want to get their sum.  By using `zip` and `chain` together, this is relatively straightforward:

```clj
(defn deferred-sum []
  (let [a (call-service-a)
        b (call-service-b)]
    (chain (zip a b)
      (fn [[a b]]
        (+ a b)))))
```

However, this isn't a very direct expression of what we're doing.  For more complex relationships between deferred values, our code will become even more difficult to understand.  In these cases, it's often best to use `let-flow`.

```clj
(defn deferred-sum []
  (let-flow [a (call-service-a)
             b (call-service-b)]
    (+ a b)))
```

In `let-flow`, we can treat deferred values as if they're realized.  This is only true of values declared within or closed over by `let-flow`, however.  So we can do this:

```clj
(let [a (future 1)]
  (let-flow [b (future (+ a 1))
             c (+ b 1)]
    (+ c 1)))
```

but not this:

```clj
(let-flow [a (future 1)
           b (let [c (future 1)]
                (+ a c))]
  (+ b 1))
```

In this example, `c` is declared within a normal `let` binding, and as such we can't treat it as if it were realized.

It can be helpful to think of `let-flow` as similar to Prismatic's [Graph](https://github.com/prismatic/plumbing#graph-the-functional-swiss-army-knife) library, except that the dependencies between values are inferred from the code, rather than explicitly specified.  Comparisons to core.async's goroutines are less accurate, since `let-flow` allows for concurrent execution of independent paths within the bindings, whereas operations within a goroutine are inherently sequential.

#### `manifold.deferred/loop`

Manifold also provides a `loop` macro, which allows for asynchronous loops to be defined.  Consider `manifold.stream/consume`, which allows a function to be invoked with each new message from a stream.  We can implement similar behavior like so:

```clj
(require
  '[manifold.deferred :as d]
  '[manifold.stream :as s])

(defn my-consume [f stream]
  (d/loop []
    (d/chain (s/take! stream ::drained)

      ;; if we got a message, run it through `f`
      (fn [msg]
        (if (identical? ::drained msg)
          ::drained
          (f msg)))

      ;; wait for the result from `f` to be realized, and
      ;; recur, unless the stream is already drained
      (fn [result]
        (when-not (identical? ::drained result)
          (d/recur))))))
 ```

Here we define a loop which takes messages one at a time from `stream`, and passes them into `f`.  If `f` returns an unrealized value, the loop will pause until it's realized.  To recur, we make sure the value returned from the final stage is `(manifold.deferred/recur & args)`, which will cause the loop to begin again from the top.

While Manifold doesn't provide anything as general purpose as core.async's `go` macro, the combination of `loop` and `let-flow` can allow for the specification of highly intricate asynchronous workflows.

#### custom execution models

Both deferreds and streams allow for custom execution models to be specified.  To learn more, [go here](//aleph.io/docs/execution.md).


### Stream

> **NOTE**: Most of the contents of this section extract from the original docs in [here](http://aleph.io/manifold/streams.html)

A Manifold stream can be created using `manifold.stream/stream`:

```clj
> (require '[manifold.stream :as s])
nil
> (def s (s/stream))
#'s
```

A stream can be thought of as two separate halves: a **sink**  which consumes messages, and a **source**  which produces them.
We can `put!` messages into the sink, and `take!` them from the source:

```clj
> (s/put! s 1)
<< ... >>
> (s/take! s)
<< 1 >>
```

Notice that both `put!` and `take!` return [deferred values](//aleph.io/manifold/deferred.md).  The deferred returned by `put!` will yield
`true` if the message was accepted by the stream, and `false` otherwise; the deferred returned by `take!` will yield the message.

Sinks can be **closed** by calling `close!`, which means they will no longer accept messages.

```clj
> (s/close! s)
nil
> @(s/put! s 1)
false
```

We can check if a sink is closed by calling `closed?`, and register a no-arg callback using `on-closed` to be notified when
the sink is closed.

Sources that will never produce any more messages (often because the corresponding sink is closed) are said to be **drained**.
We may check whether a source is drained via `drained?` and `on-drained`.

By default, calling `take!` on a drained source will yield a message of `nil`.  However, if `nil` is a valid message, we may
want to specify some other return value to denote that the source is drained:

```clj
> @(s/take! s ::drained)
::drained
```

We may also want to put a time limit on how long we're willing to wait on our put or take to complete.  For this, we can use
`try-put!` and `try-take!`:

```clj
> (def s (s/stream))
#'s
> @(s/try-put! s :foo 1000 ::timeout)
::timeout
```

Here we try to put a message into the stream, but since there are no consumers, it will fail after waiting for 1000ms.  Here
we've specified `::timeout` as our special timeout value, otherwise it would simply return `false`.

```clj
> @(s/try-take! s ::drained 1000 ::timeout)
::timeout
```

Again, we specify the timeout and special timeout value.  When using `try-take!`, we must specify return values for both the
drained and timeout outcomes.

#### stream operators

The simplest thing we can do a stream is consume every message that comes into it:

```clj
> (s/consume #(prn 'message! %) s)
nil
> @(s/put! s 1)
message! 1
true
```

However, we can also create derivative streams using operators analogous to Clojure's sequence operators, a full list of
which [can be found here](http://ideolalia.com/manifold/):

```clj
> (->> [1 2 3]
    s/->source
    (s/map inc)
    s/stream->seq)
(2 3 4)
```

Here, we've mapped `inc` over a stream, transforming from a sequence to a stream and then back to a sequence for the sake of
a concise example.  Note that calling `manifold.stream/map` on a sequence will automatically call `->source`, so we can actually omit that, leaving just:

```clj
> (->> [1 2 3]
    (s/map inc)
    s/stream->seq)
(2 3 4)
```

Since streams are not immutable, in order to treat it as a sequence we must do an explicit transformation via `stream->seq`:

```clj
> (->> [1 2 3]
    s/->source
    s/stream->seq
    (map inc))
(2 3 4)
```

Note that we can create multiple derived streams from the same source:

```clj
> (def s (s/stream))
#'s
> (def a (s/map inc s))
#'a
> (def b (s/map dec s))
#'b
> @(s/put! s 0)
true
> @(s/take! a)
1
> @(s/take! b)
-1
```

Here, we create a source stream `s`, and map `inc` and `dec` over it.  When we put our message into `s` it immediately is accepted, since
`a` and `b` are downstream.  All messages put into `s` will be propagated into *both* `a` and `b`.

If `s` is closed, both `a` and `b` will be closed, as will any other downstream sources we've created.  Likewise, if everything downstream
of `s` is closed, `s` will also be closed.  This is almost always desirable, as failing to do this will simply cause `s` to exert
backpressure on everything upstream of it.  However, If we wish to avoid this behavior, we can create a `(permanent-stream)`, which cannot be closed.

For any Clojure operation that doesn't have an equivalent in `manifold.stream`, we can use `manifold.stream/transform` with a transducer:

```clj
> (->> [1 2 3]
    (s/transform (map inc))
    s/stream->seq)
(2 3 4)
```

There's also `(periodically period f)`, which behaves like `(repeatedly f)`, but will emit the result of `(f)` every `period` milliseconds.


#### connecting streams

Having created an event source through composition of operators, we will often want to feed all messages into a sink.  This can be
accomplished via `connect`:

```clj
> (def a (s/stream))
#'a
> (def b (s/stream))
#'b
> (s/connect a b)
true
> @(s/put! a 1)
true
> @(s/take! b)
1
```

Again, we see that our message is immediately accepted into `a`, and can be read from `b`.  We may also pass an options map into
`connect`, with any of the following keys:

| field | description |
|-------|-------------|
| `downstream?` | whether the source closing will close the sink, defaults to `true` |
| `upstream?` | whether the sink closing will close the source, *even if there are other sinks downstream of the source*, defaults to `false` |
| `timeout` | the maximum time that will be spent waiting to convey a message into the sink before the connection is severed, defaults to `nil` |
| `description` | a description of the connection between the source and sink, useful for introspection purposes |

Upon connecting two streams, we can inspect any of the streams using `description`, and follow the flow of data using `downstream`:

```clj
> (def a (s/stream))
#'a
> (def b (s/stream))
#'b
> (s/connect a b {:description "a connection"})
nil
> (s/description a)
{:pending-puts 0, :drained? false, :buffer-size 0, :permanent? false, ...}
> (s/downstream a)
(["a connection" << stream: ... >>])
```

We can recursively apply `downstream` to traverse the entire topology of our streams.  This can be a powerful way to reason about
the structure of our running processes, but sometimes we want to change the message from the source before it's placed into the sink.
For this, we can use `connect-via`:

```clj
> (def a (s/stream))
#'a
> (def b (s/stream))
#'b
> (s/connect-via a #(s/put! b (inc %)) b)
nil
```

Note that `connect-via` takes an argument between the source and sink, which is a single-argument callback.  This callback will be
invoked with messages from the source, under the assumption that they will be propagated to the sink.  This is the underlying
mechanism for `map`, `filter`, and other stream operators; it allow us to create complex operations that are visible via `downstream`:

```clj
> (def a (s/stream))
#'a
> (s/map inc a)
<< source: ... >>
> (s/downstream a)
([{:op "map"} << sink: {:type "callback"} >>])
```

Each element returned by `downstream` is a 2-tuple, the first element describing the connection, and the second element describing
the stream it's feeding into.

The value returned by the callback for `connect-via` provides backpressure - if a deferred value is returned, further messages will
not be passed in until the deferred value is realized.

#### buffers and backpressure

We saw above that if we attempt to put a message into a stream, it won't succeed until the value is taken out.  This is because the
default stream has no buffer; it simply conveys messages from producers to consumers.  If we want to create a stream with a buffer,
we can simply call `(stream buffer-size)`.  We can also call `(buffer size stream)` to create a buffer downstream of an existing stream.

We may also call `(buffer metric limit stream)`, if we don't want to measure our buffer's size in messages.  If, for instance, each
message is a collection, we could use `count` as our metric, and set `limit` to whatever we want the maximum aggregate count to be.

To limit the rate of messages from a stream, we can use `(throttle max-rate stream)`.

#### event buses and publish/subscribe models

Manifold provides a simple publish/subscribe mechanism in the `manifold.bus` namespace.  To create an event bus, we can use
`(event-bus)`.  To publish to a particular topic on that bus, we use `(publish! bus topic msg)`.  To get a stream representing
all messages on a topic, we can call `(subscribe bus topic)`.

Calls to `publish!` will return a deferred that won't be realized until all streams have accepted the message.  By default,
all streams returned by `subscribe` are unbuffered, but we can change this by providing a `stream-generator` to `event-bus`,
such as `(event-bus #(stream 1e3))`.  A short example of how `event-bus` can be used in concert with the buffering and flow control
mechanisms [can be found here](https://youtu.be/1bNOO3xxMc0?t=1887).
