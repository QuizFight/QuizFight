package org.quizfight.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.quizfight.common.messages.Message
import org.quizfight.common.test.DelayedSocket
import java.net.ServerSocket
import java.net.Socket
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

data class StringMessage(val string: String) : Message

class ConnectionTest {
    @Test
    @DisplayName("Test simple message transfer")
    fun transferTest() {
        val testValue = "1234567890abcdefghijklmnopqrstuvwxyzäöüß"
        val valueReceived = AtomicBoolean(false)

        // Make sure server is available before Client tries to connect
        val serverSocket = ServerSocket(0)
        val job = GlobalScope.launch {
            val outgoing = SocketConnection(DelayedSocket(serverSocket.accept()), emptyMap())
            outgoing.send(StringMessage(testValue))
            outgoing.close()
            serverSocket.close()
        }

        val clientSocket = DelayedSocket(Socket("localhost", serverSocket.localPort))
        SocketConnection(clientSocket, mapOf(
            StringMessage::class to { conn, msg ->
                Assertions.assertTrue(msg is StringMessage)
                Assertions.assertEquals(testValue, (msg as StringMessage).string)
                valueReceived.set(true)
                conn.close()
            }
        ))

        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5)) {
            while (!valueReceived.get() || !job.isCompleted);
        }
    }
}