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

//const val masterServerIP = "192.168.0.166"
//const val masterServerPort = 34567

object Client : CoroutineScope {
    override val coroutineContext = Dispatchers.Main
    public var connection: Connection? = null

    init {
       /* launch(Dispatchers.IO) {
            val socket = Socket(masterServerIP, masterServerPort)
            connection = SocketConnection(socket, emptyMap())
        }*/
    }

    fun setServer(ip: String, port: Int , handlers: Map<KClass<*>, (Connection, Message) -> Unit>){
        launch(Dispatchers.IO) {
            connection = SocketConnection(Socket(ip, port), handlers)
        }
    }

}