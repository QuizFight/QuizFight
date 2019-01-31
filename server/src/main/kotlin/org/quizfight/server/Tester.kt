package org.quizfight.server

import org.quizfight.common.messages.GameData

/**
 * First test class
 */
fun main(args: Array<String>){
    var sls = GameServer()
    sls.addNewGame("Game0", 5, 5)
    sls.addNewGame("Game1", 2, 15)
    sls.games[0].open = false
    val list: List<GameData> = sls.listOpenGames()
    //println(sls.listOpenGames())
    for(game in list){
        println(game.toString())
    }



}