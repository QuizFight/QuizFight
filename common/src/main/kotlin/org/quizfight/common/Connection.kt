package org.quizfight.common

import org.quizfight.common.messages.Message
import kotlin.reflect.KClass

class Connection(private val handler: Map<KClass<*>, (Message) -> Unit>) {


    fun send(msg: Message) {

    }
}

data class MsgGetLobbies(val test: String) : Message
data class MsgSendLobbies(val test: List<String>) : Message

class MasterServer {
    val connection = Connection(mapOf(
            MsgGetLobbies::class to { msg -> getLobbies(msg as MsgGetLobbies) }
    ))

    private fun getLobbies(msgGetLobbies: MsgGetLobbies) {
        // collect lobbies
        connection.send(MsgSendLobbies(emptyList()))
    }
}