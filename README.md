<div align="center"><img src="https://github.com/Codamic/hellhound/raw/master/assets/hellhound-white.png" /></div>

# HellHound [![Build Status](https://travis-ci.org/Codamic/hellhound.svg?branch=master)](https://travis-ci.org/Codamic/hellhound)

**IMPORTANT NOTE**: This software is not ready to use yet. It's under heavy development
and reaches the alpha stage pretty soon. If you like to be part of this process please
leave us a message.

## Goals

**HellHound** is a fullstack Clojure/ClojureScript framework for creating async distributed web applications suitbale for stream processing.
Basically **HellHound** is a set of smaller tools to create such systems.

The goal is to create a tool which allows developers to focus on their business logic while it takes care of different aspect of a distributed
software in an opinionated way.

### Server Side
Here is a breif description about the server side application, its requirements and design.

#### Requirements
The server side application should be:

* Stateless
* Scalable
* Fault tolarance
* Async
* Extendable

#### Design
Each **HellHound** application should have a `system` which is a map of `components` with possible
`workflows` and a `supervisor`. Basically `supervisor` is in charge of starting and stopping the system
and dealing with any problem with the `system`. For example it should be able to automatically restart
a `component` which is in a bad state.

Each `component` has a start and stop function with some input and an output. A `workflows` simply
describe data workflow in the `component` pipeline.

##### Component IO
TBA

##### WebServer Component
TBA

### Client Side
Here is a breif description about the client side application, its requirements and design.

#### Requirements
The client side should able to:

* Process a stream of incoming data
* Set data as a stream of event to the server
* Subscribe to a query result

#### Design
TBA

## Installation

Add **HellHound** to your dependencies as follow:

```clojure
[codamic/hellhound "1.0.0-SNAPSHOT"]
```
## Where to get help
* [#hellhound](http://webchat.freenode.net/?channels=hellhound&uio=d4) IRC channel on [freenode](https://freenode.net/
)
* #hellhound channel on [clojurians](http://clojurians.net/)

## Getting Started
TBA
## How to contibute
TBA
## License

Copyright © 2016-2017 Sameer Rahmani <@lxsameer>.

Distributed under the MIT License.
