package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Voting(var game: Game){

    var votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    var timerSendingVotes = 12  // seconds for clients to vote if wait or not is preferred
    var timerPlayerComeback = 40 // seconds to wait for a players comeback
    var playerIsBack = false


    fun takeVote(vote: Boolean){
        if(votes.size == 1){
            GlobalScope.launch {
                startTimer()
            }
        }

        var count = votes.get(vote)!!
        votes.put(vote, count + 1)
    }

    private fun startTimer(){
        var time = 0
        while(timerSendingVotes > time){
            Thread.sleep(1000)
            time++
        }
        evaluateVoting()
    }

    private fun evaluateVoting() {
        serverLog("Voting wird jetzt ausgewertet\n")
        if(votes.get(true)!! >= votes.get(false)!!){
            waitForPlayer()
        }
        game.broadcast(game.getNextQuestion())
    }

    private fun waitForPlayer() {
        while(timerPlayerComeback > 0){
            serverLog("Game $game.gameName wartet noch $timerPlayerComeback Sekunden auf den Spieler")

            Thread.sleep(1000)
            timerPlayerComeback--

            if(playerIsBack){
                resetVotingLogic()
                break
            }
        }
    }

    private fun resetVotingLogic(){
        votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
        playerIsBack = false
    }


}