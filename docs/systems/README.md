# Systems

## Overview
At heart, **HellHound** created around the idea of systems. The basic idea is to describe the execution model and
dataflow of a program using data and let **HellHound** handles the rest.

A **HellHound** system is basically a `hashmap` describing different aspects of a system.
For example any should should have `:components` key with a vector as its value. All the `components`
of the system should be defined under this key. Each component is a `hashmap` describing the behaviours of
that component.

* `:hellhound.component/name`
The name of the component.
* `:hellhound.component/start-fn`
A *function* which is responsible to start the component. This function should accept two parameters.
The first one simply is the *component map itself* and the second one is *context map* which contains
some data to be used inside the component. For example *input* stream.

The *start-fn* basically *should return* the component map and attach any state necessary to it. Also
if the component has any *output* stream is should associate the ~:output~ with the *output* stream value.
* `:hellhound.component/stop-fn`
This function is responsible for stopping the component and it gets only one argument which is the *component map*
again and should return the stopped component map as the return value.
* `:hellhound.component/depends-on`
An array of component names which the current component is depends on.

### Workflow
Each component shoud have one *INPUT* and *OUTPUT* which should be stream of values. Input should be passed
to the component `start-fn` using `context` map and oputput should be return as the value of `:output` key
in the component map.
