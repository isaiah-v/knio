[![Knio Build](https://github.com/isaiah-v/Knio/actions/workflows/build.yml/badge.svg)](https://github.com/isaiah-v/Knio/actions/workflows/build.yml)
[![Test Coverage](https://www.knio.org/build/main/badges/Test-Coverage.svg)](https://www.knio.org/build/main/jacoco/)
[![Kdoc](https://img.shields.io/badge/Kdoc-green)](https://www.knio.org/build/main/kdoc/)
----  

# Knio

From files to sockets, a true non-blocking I/O library built with Kotlin Coroutines and NIO — offering an efficient,
readable, and user-friendly `java.io`-like API.

---  

## Features
- *Kotlin Coroutine Integration*: Enables concise, asynchronous, non-blocking code while maintaining the readability and maintainability of sequential programming.
- *NIO-Powered I/O*: Built on Java’s NIO framework for efficient, scalable, and truly non-blocking I/O operations.
- *Familiar API*: Provides a java.io-inspired API, allowing developers to modernize existing codebases effortlessly with minimal to no learning curve while transitioning to a fully non-blocking model.

---  

## Example: Buffered Reader
With the magic of `kotlin-coroutines`, operations like `readLine()` suspend instead of blocking, releasing the thread to
perform other tasks. And with `nio`, we aren't blocking a thread elsewhere. The underlying I/O operations are entirely
non-blocking. With no complicated callbacks or process streams, the code is easy to read and maintain.

```kotlin  
import org.ivcode.knio.io.bufferedReader  
import org.ivcode.knio.io.knioInputStream  
import org.ivcode.knio.lang.use  
import java.io.File  

suspend fun main() {  
    val filePath = "example.txt"  

    // Create a buffered reader for a file  
    File(filePath).knioInputStream().bufferedReader().use { reader ->  
        var line: String? = reader.readLine() // Suspends instead of blocking  

        while (line != null) {  
            println(line)            // Print each line from the file  
            line = reader.readLine() // Suspends instead of blocking  
        }  
    }  
}
```

---
## Snapshots

Snapshot builds are available from the `main` branch. To use the latest snapshot, configure your build script to use the GitHub Maven repository and add the Knio dependency.

`build.gradle.kts`
```kotlin
repositories {
    maven("https://maven.pkg.github.com/isaiah-v/Knio")
}

dependencies {
    implementation("org.ivcode:knio:0.1.0-SNAPSHOT")
}
```