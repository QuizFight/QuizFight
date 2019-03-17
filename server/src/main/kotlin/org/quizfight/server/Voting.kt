package org.quizfight.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Voting(var game: Game){

    var votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    var timerSendingVotes = 12L  // seconds for clients to vote if wait or not is preferred
    var votingWaitingTime = 40 * 1000L
    var playerIsBack = false
    var isOpen = false

    fun startVoting(){
        isOpen = true
        GlobalScope.launch {
            startTimer()
        }
    }

    fun takeVote(vote: Boolean){
        var count = votes.get(vote)!!
        votes.put(vote, count + 1)
    }

    private fun startTimer(){
        Thread.sleep(timerSendingVotes)
        isOpen = false
    }

    fun evaluateVoting() : Boolean{
        serverLog("Voting wird jetzt ausgewertet\n")
        resetVotingLogic()
        return votes.get(true)!! >= votes.get(false)!!
    }


    private fun resetVotingLogic(){
        votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
        playerIsBack = false
    }


}