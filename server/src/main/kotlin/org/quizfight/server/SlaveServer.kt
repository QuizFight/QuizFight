package org.quizfight.server

/**
 * Slave Server Class. Manages several Games.
 * @author Thomas Spanier
 */
open class SlaveServer(mip:String, ip:String) {

    private val mip:String
    private val ip :String

    private var gameIds: Int
    public var games: MutableList<Game>


    init {
        this.mip = mip
        this.ip = ip
        gameIds = 0
        games = arrayListOf()                   //Initialize games as an empty List

    }

    fun getMip():String{
        return mip
    }

    fun getIp():String{
        return ip
    }

    /*fun setMip(mip: String){
        this.mip = mip
    }

    fun setIp(ip:String){
        this.ip = ip
    }*/

    /**
     * Adds a new game to gamesList
     */
    fun addNewGame(gameName: String, maxPlayer: Int, questionCount: Int){
        games.add(Game(gameIds++, gameName, maxPlayer, questionCount))
    }

    /**
     * lists all open games running on this server
     */
    fun listOpenGames(): List<Game> {
        //val openGames: List<Game> = games.filter { it.getOpen()}
        return games.filter { it.getOpen() }
    }

    /**
     * starts Slave Server
     */
    open fun start(){
        //TODO: Implement
        println("Slave Server started")
    }




}