package org.quizfight.server

/**
 * First test class
 */
fun main(args: Array<String>){
    var sls = SlaveServer("192.168.0.1", "192.168.0.2")
    sls.addNewGame("Game0", 5, 5)
    sls.addNewGame("Game1", 2, 15)
    sls.games[0].setOpen(false)
    val list: List<Game> = sls.listOpenGames()
    //println(sls.listOpenGames())
    for(game in list){
        println(game.toString())
    }

}