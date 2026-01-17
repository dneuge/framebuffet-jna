# JNA Java bindings for Frame Buffet

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE.md)

This project provides bindings to work with Frame Buffet using [Java Native Access (JNA)](https://github.com/java-native-access/jna).

[Frame Buffet](https://github.com/dneuge/framebuffet) is a basic framework implementing an externally configurable modular 2D scene graph for frame buffer
compositing. See the [project page](https://github.com/dneuge/framebuffet) for more information.

## Current state

Frame Buffet is in early development and these Java bindings may change or break with every change made to the library itself. There is no stable release yet;
bindings will probably start following Frame Buffet's version numbers at some point but are currently unrelated.

## How to use

As this library has not been deployed to any repository yet, you will need to build it yourself for now. It should simply compile by running
`mvn clean install`.

Frame Buffet itself is not included. It is not required for compiling the binaries but you will need it at runtime. Please refer to
the [project page](https://github.com/dneuge/framebuffet) on details of how to build it. To build an all-in-one JAR file for your project using Frame Buffet
simply include the library into your project's class path within the directory expected by JNA, e.g. in `/linux-x86-64`.

## License

This project is released under [MIT license](LICENSE.md), same as the Frame Buffet framework. Please note that Frame Buffet modules may have different licenses;
see the [project page](https://github.com/dneuge/framebuffet) for details.

In general, please check all dependencies for applicable licenses before use or redistribution.
