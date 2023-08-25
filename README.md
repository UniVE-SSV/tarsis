# TARSIS Abstract domain for static string analysis 

![buildbadge](https://github.com/UniVE-SSV/tarsis/workflows/Java%20CI%20with%20Gradle/badge.svg)

Implementation of _Twinning automata and regular expressions for string static analysis_, by Luca Negrini, Vincenzo Arceri, Pietro Ferrara and Agostino Cortesi.

**This repo is obsolete and it is not currently maintained. However, the Tarsis abstract domain is implemented and mantained in LiSA (https://github.com/lisa-analyzer/lisa).**


Links:

* [Preprint](https://arxiv.org/abs/2006.02715)
* [Published version](https://link.springer.com/chapter/10.1007/978-3-030-67067-2_13)

## How to use the abstract domain 

Class [AutomatonString](src/main/java/it/unive/tarsis/AutomatonString.java) is the entry point for working with the domain.
Use its parameterless constructor to build automaton representing a generic unknown string, and use the one accepting a string parameter to build an automaton recognizing the given string.

## How to build the project ##
Tarsis comes as a gradle 6.0 project. For development with Eclipse, please install the [Gradle IDE Pack](https://marketplace.eclipse.org/content/gradle-ide-pack) plugin from the Eclipse marketplace, and make sure to import the project into the workspace as a Gradle project.
