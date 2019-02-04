package org.quizfight.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.quizfight.common.messages.Message
import java.net.ServerSocket
import java.net.Socket

data class StringMessage(val string: String) : Message

class ConnectionTest {
    @Test
    @DisplayName("Test simple message transfer")
    fun transferTest() {
        val testValue = "1234567890abcdefghijklmnopqrstuvwxyzäöüß"
        var valueReceived = false

        GlobalScope.launch {
            val serverSocket = ServerSocket(34567)
            val outgoing = SocketConnection(serverSocket.accept(), emptyMap())
            outgoing.send(StringMessage(testValue))
            outgoing.close()
            serverSocket.close()
        }

        val clientSocket = Socket("127.0.0.1", 34567)
        val incoming = SocketConnection(clientSocket, mapOf(
            StringMessage::class to { conn, msg ->
                Assertions.assertTrue(msg is StringMessage)
                Assertions.assertEquals(testValue, (msg as StringMessage).string)
                valueReceived = true
            }
        ))

        while (!valueReceived);
        incoming.close()
    }
}