# JNA Java bindings for Frame Buffet

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE.md)
[![Issue Tracking: Codeberg](https://img.shields.io/badge/issue%20tracking-codeberg-2684cf)](https://codeberg.org/dneuge/framebuffet-jna/issues)

This project provides bindings to work with [Frame Buffet](https://codeberg.org/dneuge/framebuffet)
using [Java Native Access (JNA)](https://github.com/java-native-access/jna).

Frame Buffet is a library implementing an externally configurable modular 2D scene graph for frame buffer composition.
See the [project page](https://github.com/dneuge/framebuffet) (on [Codeberg](https://codeberg.org/dneuge/framebuffet)
or [GitHub](https://github.com/dneuge/framebuffet)) for more information.

Official repositories for these bindings are hosted on [Codeberg](https://codeberg.org/dneuge/framebuffet-jna)
and [GitHub](https://github.com/dneuge/framebuffet-jna). Both locations are kept in sync and can be used to submit pull requests but issues are only tracked
on [Codeberg](https://codeberg.org/dneuge/framebuffet-jna/issues) to gather them in a single place. Please note that this project uses
the [same contribution guidelines as Frame Buffet](https://codeberg.org/dneuge/framebuffet/src/branch/main/CONTRIBUTING.md) itself and thus also has the same
strict "no AI" policy affecting all contributions incl. issue reports.

## Current state

Frame Buffet is in early development and these Java bindings may change or break with every change made to the library itself. There is no stable release yet;
bindings will probably start following Frame Buffet's version numbers at some point but are currently unrelated.

## How to use

As this library has not been deployed to any repository yet, you will need to build it yourself for now. It should simply compile by running
`mvn clean install`.

Frame Buffet itself is not included. It is not required for compiling the binaries but you will need it at runtime. Please refer to
the [project page](https://codeberg.org/dneuge/framebuffet) on details of how to build it. To build an all-in-one JAR file for your project using Frame Buffet
simply include the library into your project's class path within the directory expected by JNA, e.g. in `/linux-x86-64`.

## License

This project is released under [MIT license](LICENSE.md), same as the Frame Buffet framework. Please note that Frame Buffet modules may have different licenses;
see the [project page](https://codeberg.org/dneuge/framebuffet) for details.

In general, please check all dependencies for applicable licenses before use or redistribution.

### Note on the use of/for AI

Usage for AI training is subject to individual source licenses, there is no exception. This generally means that proper
attribution must be given and disclaimers may need to be retained when reproducing relevant portions of training data.
When incorporating source code, AI models generally become derived projects. As such, they remain subject to the
requirements set out by individual licenses associated with the input used during training. When in doubt, all files
shall be regarded as proprietary until clarified.

Unless you can comply with the licenses of this project you obviously are not permitted to use it for your AI training
set. Although it may not be required by those licenses, you are additionally asked to make your AI model publicly
available under an open license and for free, to play fair and contribute back to the open community you take from.

AI tools are not permitted to be used for contributions to this project. The main reason is that, as of time of writing,
no tool/model offers traceability nor can today's AI models understand and reason about what they are actually doing.
Apart from potential copyright/license violations the quality of AI output is doubtful and generally requires more
effort to be reviewed and cleaned/fixed than actually contributing original work. Contributors will be asked to confirm
and permanently record compliance with these guidelines.
