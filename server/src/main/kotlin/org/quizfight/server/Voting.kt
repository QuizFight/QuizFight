package org.quizfight.server

/**
 * This class is used for the voting procedure.
 * If a players connection is lost while a game is running, a voting which
 * decides if the other players want to wait for the lost player is started.
 * @param game is the Game which needs to vote.
 */
class Voting(var game: Game){

    /** Saves the WAIT or DONT WAIT decicions from clients */
    var votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))

    /** Time a client has to vote */
    var timerSendingVotes = 12 * 1000L // seconds for clients to vote if wait or not is preferred

    /** Time a player has to rejoin a game */
    var votingWaitingTime = 40

    /** If a player send a vote, here the value is collectoes to the map 'votes' */
    fun takeVote(vote: Boolean){
        serverLog("Vote empfangen: $vote")
        var count = votes.get(vote)!!
        votes.put(vote, count + 1)
    }

    /** After timer is over, the voting will evaluated here */
    fun evaluateVoting() : Boolean{
        serverLog("Voting wird jetzt ausgewertet\n")
        serverLog("Es gab ${votes.get(true)!!} Stimmen dafür und ${votes.get(false)!!} dagegen\n")
        var result = votes.get(true)!! > votes.get(false)!!
        resetVotingLogic()
        return result
    }

    /** The map with the collected votes has to be resetted here */
    private fun resetVotingLogic(){
        votes = mutableMapOf<Boolean, Int>((false to 0), (true to 0))
    }


}