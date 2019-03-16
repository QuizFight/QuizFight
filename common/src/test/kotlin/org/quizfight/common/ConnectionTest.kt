package org.quizfight.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
import java.util.concurrent.atomic.AtomicInteger

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

    @Test
    @DisplayName("Test changing handlers")
    fun withWHandlersTest() {
        val trigger = AtomicBoolean(false)
        val job = GlobalScope.launch(Dispatchers.IO) {
            val server = ServerSocket(34567)
            val client = SocketConnection(server.accept(), emptyMap())

            while (!trigger.get());
            client.send(StringMessage(""))
            client.send(StringMessage(""))
            client.close()
        }

        val oldHandlersRaised = AtomicInteger(0)
        val newHandlersRaised = AtomicInteger(0)

        SocketConnection(Socket("127.0.0.1", 34567), mapOf(
            StringMessage::class to { conn, _ ->
                oldHandlersRaised.incrementAndGet()
                conn.withHandlers(mapOf(
                    StringMessage::class to { _, _ -> newHandlersRaised.incrementAndGet(); conn.close() }
                ))
            }
        ))

        trigger.set(true)
        while(!job.isCompleted);
        Assertions.assertEquals(1, oldHandlersRaised.get(), "Old handlers did not get removed")
        Assertions.assertEquals(1, newHandlersRaised.get(), "New handlers were not raised")
    }
}