# knio
A Kotlin Coroutine NIO library. Rebuilding the java APIs with asynchronous Kotlin Coroutines using NIO.

Kotlin Coroutines allows for asynchronous programming in a synchronous style without blocking threads, similar to
async/await in C# or JavaScript. This library attempts to reimplement some of the java I/O API (`java.io`, `java.net`)
using NIO and Kotlin Coroutines. Non-blocking I/O with a familiar APIs.


Reader Example:
```kotlin
// Read lines from a file using NIO and coroutines
suspend fun main() {

    val path = Path("/path/to/file.txt")
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

# Roadmap
### v0.1.0
The first release will focus on getting code complete for core I/O classes.

I/O Classes:
- [x] `FileInputStream`
- [ ] `FileOutputStream`
- [ ] `SeverSocket`
- [ ] `SSLServerSocket`
- [x] `Socket`
- [ ] `SSLSocket`
- [ ] `DatagramSocket`

Supporting Classes:
- [x] `InputStream`
- [x] `OutputStream`
- [x] `Reader`
- [ ] `Writer`
- [ ] `BufferedInputStream`
- [ ] `BufferedOutputStream`
- [x] `BufferedReader`
- [ ] `BufferedWriter`
- [x] `InputStreamReader`
- [ ] `OutputStreamWriter`
