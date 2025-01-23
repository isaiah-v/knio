![build](https://github.com/isaiah-v/knio/actions/workflows/build.yml/badge.svg)

----

# knio
A Kotlin Coroutine NIO Library. Rebuilding Java's I/O APIs with asynchronous Kotlin Coroutines powered by NIO.

I/O and asynchronous program
ing have come a long way. The original Java I/O APIs relied on blocking operations, using
threads to handle I/O tasks. This approach, while functional and easy to read, was inefficient due to thread overhead
and frequent context switching. The introduction of NIO and NIO2 brought significant improvements by enabling
non-blocking I/O, but the APIs were less intuitive and harder to work with. Kotlin Coroutines offer a modern solution by
allowing us to write asynchronous code in a more simple synchronous style using suspending functions. This approach
avoids blocking threads while maintaining code clarity.

It's time to bring the simplicity of the classic blocking Java I/O APIs back with the of benefits of non-blocking I/O
using Kotlin Coroutines.


### Buffered Reader Example
In this example we read from a file in a way that syntactically looks like blocking I/O, but is actually non-blocking.
```kotlin
// Read lines from a file using NIO and coroutines
suspend fun main() {

    // Create a buffered reader for a file, in a similar fashion to Java's BufferedReader
    File(file).knioInputStream().bufferedReader().use { reader ->
        // Suspend on the I/O operation
        // Though this looks like a blocking call, it's actually non-blocking. The thread is released to do other work
        // while waiting for the nio operation to complete. Not an offload, but a true non-blocking operation.
        var line: String? = reader.readLine()
        while (line != null) {
            print(line)                      // print the line
            line = reader.readLine()         // Another suspend on I/O [non-blocking]
        } // close resources [non-blocking]
    }
}
```

# Roadmap
### v0.1.0
The first release will focus on getting code complete for core I/O classes.

I/O Classes:
- [x] `FileInputStream`
- [ ] `FileOutputStream`
- [x] `SeverSocket`
- [x] `SSLServerSocket`
- [x] `Socket`
- [x] `SSLSocket`
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
