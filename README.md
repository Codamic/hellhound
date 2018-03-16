<div align="center"><img src="https://github.com/Codamic/hellhound/raw/master/assets/hellhound-white.png" /></div>

# HellHound

**IMPORTANT NOTE**: This software is not ready to use yet. It's under heavy development
and reaches the alpha stage pretty soon. If you like to be part of this process please
leave [me](//github.com/lxsameer) a message.

---
[Guides](http://docs.hellhound.io/guides/) | [API Reference](http://docs.hellhound.io/api/) | [Examples](https://github.com/Codamic/hellhound_examples) | [How to contribute?](http://docs.hellhound.io/guides/#_contributing_to_hellhound)
----
[![Build Status](https://travis-ci.org/Codamic/hellhound.svg?branch=master)](https://travis-ci.org/Codamic/hellhound) [![Clojars Project](https://img.shields.io/clojars/v/codamic/hellhound.svg)](https://clojars.org/codamic/hellhound)

## What is HellHound
**HellHound** is a set of libraries to create simple and elegant programs based on streams. An **HellHound** application
basically is a [system](http://docs.hellhound.io/guides/#systems) of [components](http://docs.hellhound.io/guides/#components)
which work together in order to achieve a common goal. Components form one or more data pipelines through
[workflows](http://docs.hellhound.io/guides/#workflow). In general systems are a great way to manage the lifecycle and data flow
or your program and components are awesome for managing the state and dependencies of different pieces of your program.

HellHound provides different built-in components for different types of systems. For example Webserver component for creating
a fullstack web application, or a kafka component for a data processing application. For more information checkout the
[guides](http://docs.hellhound.io/guides/) and [Examples](https://github.com/Codamic/hellhound_examples).


## Road Map for next stable version
Our near future goals are:
* Support for configurable execution model for the entire system and components as well.
  In addition to manage the execution model of the system automatically by HellHound, We like to add a configurable
  execution model which allows users to change the behavior based on the use case. For example user might want
  to run a component on a blocking thread pool or control the number of threads in each thread pool under hood.

* Support for Kafka streams. We're working on adding the support for kafka streams in our stream abstractions so
  users can easily take advantage of good features of Apache Kafka in their systems. (Development Started)

* Support for IO spec for each component. The basic idea is to add the support for a pair of configurations to
  components that allow them to define specs for incoming and outgoing messages. It should be possible to enforce
  these spec on messages. Another purpose of having these specs is to create a diagram later that demonstrates how
  data transform in the system. This way by creating the diagram in any given time, as a developer we would have
  better understanding of our system and data flow. This feature would be awesome specially for new developers.

If you like to help us with any of these. Give us a shout.

## Branching guide
The `master` branch of this repo is for development purposes. In order to get the latest stable code please checkout the
`stable` branch.

## Where to get help
* [#hellhound](http://webchat.freenode.net/?channels=hellhound&uio=d4) IRC channel on [freenode](https://freenode.net/
)
* #hellhound channel on [clojurians](http://clojurians.net/)


## License

Copyright © 2016-2018 Sameer Rahmani <[@lxsameer](//twitter.com/lxsameer)>.

Distributed under the MIT License.
