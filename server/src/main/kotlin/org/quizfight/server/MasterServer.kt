package org.quizfight.server

/**
 * The Master Server who manages the Slave Servers.
 * First Bootstrap for all connections.
 * Inherits from SlaveServer
 * @author Thomas Spanier
 */
class MasterServer (ip: String):SlaveServer(ip, ip) {
    var servers: MutableList<String>


    init {
        servers = arrayListOf(ip)

    }

    /**
     * Adds a new Slave Server to the List
     */
    fun addServerToList(slaveIp: String){
        servers.add(slaveIp)
    }

    /**
     * Removes a Slave Server from the List
     */
    fun removeServerFromList(slaveIp: String){
        servers.remove(slaveIp)
    }

    /**
     * returns a List with all open Games from all Slave Servers
     */
    fun listAllOpenGames(): List<Game>{
        var allGames: List<Game> = arrayListOf()
        for(slaveServer in servers){
            //TODO: connect to SlaveServer and call listOpenGames
        }
        return allGames
    }

    /**
     * Starts the Master Server
     */
    override fun start(){
        //TODO: implement
        println("Master Server started")
    }
}