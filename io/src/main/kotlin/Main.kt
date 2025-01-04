package org.ivcode

import org.ivcode.knio.KFileInputStream
import org.ivcode.knio.KInputStreamReader
import org.ivcode.knio.use
import org.ivcode.org.ivcode.knio.KBufferedReader
import java.nio.file.Path


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() {

    val path = Path.of("io/src/test/resources/test.txt")
    val inputStream = KFileInputStream(path)
    val reader = KInputStreamReader(inputStream)
    val bufferedReader = KBufferedReader(reader)

    bufferedReader.use {
        var line: String? = it.readLine()   // read the first line using nio (suspends instead of blocking)
        while (line != null) {
            println(line)                   // print the line
            line = it.readLine()            // read the next line using nio (suspends instead of blocking)
        }
    } // close the bufferedReader and underlying reader and input stream
}