package org.quizfight.common.messages

import org.quizfight.common.question.Question
import java.io.Serializable
import java.util.*

interface Message : Serializable

data class GameData(val id: Int,
                    val name:String,
                    val maxPlayers: Int,
                    val players: List<String>) : Serializable

data class GameRequest(val name : String,
                       val maxPlayers : Int,
                       val questionCount: Int) : Serializable

data class ServerData(val ip: String,
                      val port: Int,
                      val games: List<GameData>) : Serializable

class MsgRegisterGameServer : Message
class MsgRequestOpenGames : Message
data class MsgGameList(val games : List<GameData>) : Message
data class MsgJoin(val gameId: Int) : Message
class MsgLeave : Message
data class MsgTransferToGameServer(val gameServer : ServerData) : Message
data class MsgGameInfo(val game : GameData) : Message
data class MsgCreateGame(val game : GameRequest) : Message
class MsgStartGame : Message
data class MsgQuestion(val question : Question) : Message
data class MsgScore(val score : Int) : Message
data class MsgRanking(val totalScore : SortedMap<String, Int>) : Message
data class MsgConnectionLost(val name : String) : Message
data class MsgVote(val waitForPlayer : Boolean, val name : String): Message
data class MsgWait(val name : String): Message
data class MsgConnectionResumed(val name : String) : Message
class MsgGameOver : Message