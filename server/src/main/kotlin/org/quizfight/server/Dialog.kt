package org.quizfight.server

import org.quizfight.common.GAME_SERVER_PORT
import org.quizfight.common.MASTER_PORT

/**
 * Class for user-interaction.
 * Selecting between master server or game server is possible.
 */
class Dialog{

    private val QUIT             = "q"
    private val MASTER_SERVER    = "m"
    private val GAME_SERVER      = "g"
    private val MENU_MSG         = "MasterServer (${MASTER_SERVER}), GameServer (${GAME_SERVER}), Quit (${QUIT})?"
    private val TYPE_MASTER_IP   = "Master Server IP-Address: "
    private val BAD_INPUT        = "Bad Input, try again"
    private val EXIT             = "Bye"

    /**
     * Starts the user-interaction after calling a Dialog-constructor
     */
    init{
        start()
    }

    /**
     * Starts the user-interaction. Menu is printed and possibilities to interact are shown.
     */
    private fun start(){
        println(MENU_MSG)
        var selection = readLine()

        while (!selection!!.equals(QUIT)){
            when(selection){
                MASTER_SERVER -> startMasterServer()
                GAME_SERVER   -> startGameServer()
                else          -> selection = selectAgain()
            }
        }
        
        println(EXIT)
    }

    /**
     * User has decided to start a game server
     */
    private fun startGameServer() {
        println(TYPE_MASTER_IP)
        val masterIp = readLine()

        if (!masterIp.isNullOrEmpty()){
            GameServer(masterIp, GAME_SERVER_PORT, MASTER_PORT)
        }
    }

    /**
     * User has decided to start a master server
     */
    private fun startMasterServer() {
        MasterServer(MASTER_PORT)
    }

    /**
     * This function is called if the user makes a bad input.
     * The menu is shown again.
     */
    private fun selectAgain(): String? {
        println("${BAD_INPUT} \n${MENU_MSG}")
        return readLine()
    }
}