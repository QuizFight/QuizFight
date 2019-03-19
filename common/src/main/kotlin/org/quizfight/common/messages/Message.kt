package org.quizfight.common.messages

import org.quizfight.common.question.Question
import java.io.Serializable

interface Message : Serializable

data class GameData(val id: String,
                    val name:String,
                    val maxPlayers: Int,
                    val players: List<String>,
                    val questionCount: Int,
                    val gameCreator: String) : Serializable

data class GameRequest(val name : String,
                       val maxPlayers : Int,
                       val questionCount: Int) : Serializable

data class ServerData(val ip: String,
                      val port: Int,
                      var games: List<GameData>) : Serializable

data class MsgRegisterGameServer(val port : Int) : Message
class MsgRequestOpenGames : Message
data class MsgGameList(val games : List<GameData>) : Message
data class MsgJoin(val gameId: String, val nickname: String) : Message
data class MsgPlayerCount(val playerCount: Int) : Message
class MsgLeave : Message
data class MsgTransferToGameServer(val gameServer : ServerData) : Message
data class MsgGameInfo(val game : GameData) : Message
data class MsgCreateGame(val game : GameRequest, val nickname : String) : Message
class MsgStartGame : Message
data class MsgQuestion(val question : Question<*>) : Message
data class MsgScore(val score : Int) : Message
data class MsgRanking(val totalScore : Map<String, Int>) : Message
data class MsgConnectionLost(val name : String) : Message
class MsgCheckConnection() : Message
data class MsgVote(val waitForPlayer : Boolean, val name : String): Message
data class MsgRejoin(val gameServerID: String, val nickname: String) : Message
class MsgWait : Message
data class MsgConnectionResumed(val name : String) : Message
class MsgGameOver : Message