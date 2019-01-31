package org.quizfight.common


//import java.io.ObjectInputStream
//import java.io.ObjectOutputStream
import org.quizfight.common.messages.Message
import java.net.ServerSocket
import java.net.Socket
import kotlin.reflect.KClass

//import org.quizfight.server.Game

class Connection(
        val socket: Socket,
        private val handler: Map<KClass<*>, (Connection, Message) -> Unit>,
        private val connectTo: String? = null
) {
    //private val inStream: ObjectInputStream
    //private val outStream: ObjectOutputStream

    init {
        /* TODO: Type check handler map
            for (key in handler.keys)
            if (Message::class in key.supertypes)
        */

        if (connectTo != null) {
            val socket = Socket(connectTo, 34567)
        } else {
            val socket = ServerSocket(34567)
        }
    }

    fun send(msg: Message) {

    }
}

