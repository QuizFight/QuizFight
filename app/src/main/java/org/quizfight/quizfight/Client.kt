package org.quizfight.quizfight

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection
import org.quizfight.common.messages.*
import java.net.Socket
import kotlin.reflect.KClass


object Client {
    public var connection: Connection? = null
    private var serverIp: String = ""

    fun setServer(ip: String, port: Int , handlers: Map<KClass<*>, (Connection, Message) -> Unit>){
       connection = SocketConnection(Socket(ip, port), handlers)
        serverIp = ip
    }

    fun getServerIp(): String{
        return  serverIp
    }

}