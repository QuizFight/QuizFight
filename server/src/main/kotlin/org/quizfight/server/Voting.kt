package org.quizfight.server

class Voting(var game: Game){

    var votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    var timerSendingVotes = 12 * 1000L // seconds for clients to vote if wait or not is preferred
    var votingWaitingTime = 40 * 1000L
    var playerIsBack = false

    fun takeVote(vote: Boolean){
        serverLog("Vote empfrangen: $vote")
        var count = votes.get(vote)!!
        votes.put(vote, count + 1)
    }

    fun evaluateVoting() : Boolean{
        serverLog("Voting wird jetzt ausgewertet\n")
        serverLog("Es gab ${votes.get(true)!!} Stimmen dafür und ${votes.get(false)!!} dagegen\n")
        resetVotingLogic()
        return votes.get(true)!! > votes.get(false)!!
    }


    private fun resetVotingLogic(){
        votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
        playerIsBack = false
    }


}