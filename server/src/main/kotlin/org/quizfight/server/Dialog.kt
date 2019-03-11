package org.quizfight.server

class Dialog{

    private val QUIT             = "q"
    private val MASTER_SERVER    = "m"
    private val GAME_SERVER      = "g"
    private val MENU_MSG         = "MasterServer (${MASTER_SERVER}), GameServer (${GAME_SERVER}), Quit (${QUIT})?"
    private val TYPE_MASTER_IP   = "Master Server IP-Address: "
    private val BAD_INPUT        = "Bad Input, try again"
    private val EXIT             = "Bye"

    private val MASTER_PORT      = 34567
    private val GAME_SERVER_PORT = 45678

    init{
        start()
    }

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

    private fun startGameServer() {
        println(TYPE_MASTER_IP)
        val masterIp = readLine()

        if (!masterIp.isNullOrEmpty()){
            GameServer(masterIp, GAME_SERVER_PORT, MASTER_PORT)
        }
    }

    private fun startMasterServer() {
        MasterServer(MASTER_PORT)
    }

    private fun selectAgain(): String? {
        println("${BAD_INPUT} \n${MENU_MSG}")
        return readLine()
    }
}