# knio
A Kotlin Coroutine NIO library. Rebuilding the `java.io` API with asynchronous Kotlin Coroutines using NIO.

Kotlin Coroutines allows for asynchronous programming in a synchronous style without blocking threads, similar to
async/await in C# or JavaScript. This library attempts to reimplement the `java.io` API using NIO and Kotlin Coroutines.
Non-blocking I/O with a familiar API.


Reader Example:
```kotlin
// Read lines from a file using NIO and coroutines
suspend fun main() {

    val path = Path("C:\\Users\\isaiah\\Documents\\git\\isaiah-v\\middleman\\LICENSE")
    val inputStream = KFileInputStream(path)
    val reader = KInputStreamReader(inputStream)
    val bufferedReader = KBufferedReader(reader)

    bufferedReader.use {
        var line: String? = it.readLine()   // read the first line using nio [non-blocking]
        while (line != null) {
            println(line)                   // print the line
            line = it.readLine()            // read the next line using nio [non-blocking]
        }
    } // close resources [non-blocking]
}
```
