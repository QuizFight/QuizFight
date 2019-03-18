package org.quizfight.server

class Voting(var game: Game){

    var votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    var timerSendingVotes = 12 * 1000L // seconds for clients to vote if wait or not is preferred
    var votingWaitingTime = 40 * 1000L

    fun takeVote(vote: Boolean){
        serverLog("Vote empfangen: $vote")
        var count = votes.get(vote)!!
        votes.put(vote, count + 1)
    }

    fun evaluateVoting() : Boolean{
        serverLog("Voting wird jetzt ausgewertet\n")
        serverLog("Es gab ${votes.get(true)!!} Stimmen dafür und ${votes.get(false)!!} dagegen\n")
        var result = votes.get(true)!! > votes.get(false)!!
        resetVotingLogic()
        return result
    }


    private fun resetVotingLogic(){
        votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    }


}