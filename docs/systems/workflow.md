# Workflow
System's workflow a vector describing the dataflow of the system. Components have an input and an output stream. Each
stream is a`manifold.stream`. HellHound connects io of each component to another component based on the desciption given
by the `:workflow` of the system.

System's workflow is a vector of vectors. Each vector contains two mandatory element which are:
  * The name of the output component
  * The name of the input component
and an optional predicate function. This function connects the output stream of output component to input stream of
input component, and in case of existance of a predicate function, it only sends those messages which pass the predicate.

Predicate function should be a pure function obviousely.

Each component shoud have one *INPUT* and *OUTPUT* which should be stream of values. Input should be passed
to the component `start-fn` using `context` map and oputput should be return as the value of `:output` key
in the component map.
