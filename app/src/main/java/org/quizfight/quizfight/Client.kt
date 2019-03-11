package org.quizfight.quizfight

import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.net.Socket
import kotlin.reflect.KClass


object Client : CoroutineScope {
    override val coroutineContext = Dispatchers.Main
    public var connection: Connection? = null

    init { }

    fun setServer(ip: String, port: Int , handlers: Map<KClass<*>, (Connection, Message) -> Unit>){
       connection = SocketConnection(Socket(ip, port), handlers)
    }

}