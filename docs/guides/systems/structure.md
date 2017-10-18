# Structure
---
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
