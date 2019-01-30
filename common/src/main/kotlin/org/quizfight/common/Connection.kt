package org.quizfight.common

import org.quizfight.common.messages.Message
import kotlin.reflect.KClass

class Connection(private val handler: Map<KClass<*>, (Message) -> Unit>) {


    fun send(msg: Message) {

    }
}
