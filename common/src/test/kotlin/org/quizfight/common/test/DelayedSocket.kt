package org.quizfight.common.test

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import kotlin.random.Random

class DelayedSocket(private val socket: Socket) : Socket() {
    private val outStream = socket.getOutputStream()
    private val inStream  = socket.getInputStream()

    private val outWrapper = object : OutputStream() {
        override fun write(b: Int) {
            Thread.sleep(Random.nextLong(1, 5))
            outStream.write(b)
        }
    }
    private val inWrapper = object : InputStream() {
        override fun read(): Int {
            Thread.sleep(Random.nextLong(1, 5))
            return inStream.read()
        }
    }

    override fun getOutputStream() = outWrapper
    override fun getInputStream() = inWrapper
    override fun close() = socket.close()
}