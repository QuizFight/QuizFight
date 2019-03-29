package org.quizfight.server

import org.quizfight.common.Connection
import org.quizfight.common.SocketConnection

/** Flag for allowing server prints */
var serverLogger = true

/**
 * Prints a message, triggered by any server procedure.
 * If the flag serverLogger is true, the message will printed.
 * If not, nothing happens.
 * @param message is the message to print.
 */
fun serverLog(message: String){
    if(serverLogger){
        println(message)
    }
}

/**
 * Receives a connection and extracts ip and port from it.
 * This is used for getting unique information about a player
 * or a game server.
 * @param conn is the connection
 * @return is the ip + port from a connection with this pattern "111.111.111.111:12345"
 */
fun getIpAndPortFromConnection(conn: Connection): String{
    val sockConn = conn as SocketConnection
    val remoteIpPort = sockConn.socket.remoteSocketAddress.toString().
            replace("/", "")
    return remoteIpPort
}

