# Book The Rook

This project aims at reproducing a minimalist **chess game collection manager** application, oriented towards creating something intuitive and light for the user.

The current main objectives to implement are:

* Handling creation (import), edition, reading (parsing) and deletion of games under [PGN](https://fr.wikipedia.org/wiki/Portable_Game_Notation) format
* Supporting intuitive tree structure to classify games according to user's criterias
* Allowing navigation into game moves through in-app chess visualizer

In some unknown future, deeper features such as *engine analysis*, *adding variations / comments* or *advanced filtering of games* could be added.

## How to run it

As of today, there is no release of the application, so the only way to test it is to run it from sources.

### Stack Requirements

* Java 17
* JavaFX

### Compile Instructions

Since (at least) one of the dependency is an [automatic module](https://stackoverflow.com/a/46742802/24615580), it may be required to add the following line to the VM arguments when trying to compile the project through IntelliJ:

```
--add-reads com.github.adrienave.booktherook=ALL-UNNAMED
```

After that - and making sure your IDE properly loaded the dependency through Gradle, the application can be started by running its main class: `java/com/github/adrienave/booktherook/BookTheRook.java`.
