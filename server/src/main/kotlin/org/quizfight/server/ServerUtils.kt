package org.quizfight.server

import org.quizfight.common.SocketConnection

class ServerUtils{

    fun getIpAndPortFromConnection(conn: SocketConnection): List<String>{
        val sockConn = conn as SocketConnection
        val remoteIpPort = sockConn.socket.remoteSocketAddress.toString().
                replace("/", "").split(":")
        return remoteIpPort
    }

}