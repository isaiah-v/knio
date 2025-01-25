[![Knio Build](https://github.com/isaiah-v/Knio/actions/workflows/build.yml/badge.svg)](https://github.com/isaiah-v/Knio/actions/workflows/build.yml)
[![Test Coverage](https://isaiah-v.github.io/Knio/build/main/badges/Test-Coverage.svg)](https://isaiah-v.github.io/Knio/build/main/jacoco/)
[![Kdoc](https://img.shields.io/badge/Kdoc-green)](https://isaiah-v.github.io/Knio/build/main/kdoc/)
----  

# Knio

**Knio** enables true non-blocking I/O by combining Kotlin coroutines with Java NIO. This approach allows you to write asynchronous code that resembles synchronous code, using coroutines to suspend operations rather than blocking threads.

With **Knio**, you can take familiar Java I/O APIs and make them non-blocking, avoiding inefficiencies like thread blocking and context switching, while working seamlessly with Kotlin's coroutine model.

---  

## Features
- Seamless integration of Kotlin coroutines with Java NIO.
- Non-blocking I/O that feels like traditional synchronous code, improving performance by avoiding thread blocking and context switching.
- Simplified handling of non-blocking I/O operations without callbacks or streams, making your code more readable and maintainable.
- Familiar APIs, including file and network I/O, optimized for non-blocking and coroutine-friendly operations.

---  

## Example: Buffered Reader
Here's how to read lines from a file using NIO and coroutines in a non-blocking fashion:
```kotlin  
import org.ivcode.knio.io.bufferedReader  
import org.ivcode.knio.io.knioInputStream  
import org.ivcode.knio.lang.use  
import java.io.File  

suspend fun main() {  
    val filePath = "example.txt"  

    // Create a buffered reader for a file  
    File(filePath).knioInputStream().bufferedReader().use { reader ->  
        var line: String? = reader.readLine()  

        while (line != null) {  
            println(line) // Print each line from the file  
            line = reader.readLine() // Suspends instead of blocking  
        }  
    }  
}
```
In this example:
 - *Suspension*: The readLine() function suspends the coroutine, freeing the thread to perform other tasks while waiting for I/O to complete.
 - *No Callbacks*: This approach avoids callbacks, streams, or blocking threads, making your code more readable and maintainable.

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