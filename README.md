[![Knio Build](https://github.com/isaiah-v/Knio/actions/workflows/build.yml/badge.svg)](https://github.com/isaiah-v/Knio/actions/workflows/build.yml)
[![Test Coverage](https://isaiah-v.github.io/Knio/build/main/badges/Test-Coverage.svg)](https://isaiah-v.github.io/Knio/build/main/jacoco/)
[![Kdoc](https://img.shields.io/badge/Kdoc-green)](https://isaiah-v.github.io/knio/build/main/kdoc/)
----

# Knio
**Knio** combines the power of Kotlin coroutines with Java NIO to deliver true non-blocking I/O. It allows you to write
asynchronous code that looks and debugs like synchronous code, leveraging coroutines to suspend operations rather than
blocking threads.

With Knio, you can take familiar Java I/O APIs and make them non-blocking, avoiding the inefficiencies of thread
blocking and context switching while working seamlessly with Kotlin's coroutine model.

---

## Features
- Seamless integration of Kotlin coroutines with Java NIO.
- Non-blocking I/O that feels like traditional synchronous code.
- Simplified handling of non-blocking I/O operations without callbacks or streams.
- Familiar APIs, including file and network I/O, that is true non-blocking and coroutine-friendly.

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

 - Suspension: The readLine() function suspends the coroutine, freeing the thread to perform other tasks while waiting for I/O to complete.
 - No Callbacks: This approach avoids callbacks, streams, or blocking threads, making your code more readable and maintainable.
