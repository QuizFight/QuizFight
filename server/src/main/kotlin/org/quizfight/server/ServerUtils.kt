package org.quizfight.server

import org.quizfight.common.SocketConnection

    var serverLogger = true

    fun serverLog(message: String){
        if(serverLogger){
            println(message)
        }
    }

    fun getIpAndPortFromConnection(conn: SocketConnection): String{
        val sockConn = conn as SocketConnection
        val remoteIpPort = sockConn.socket.remoteSocketAddress.toString().
                replace("/", "")
        return remoteIpPort
    }

