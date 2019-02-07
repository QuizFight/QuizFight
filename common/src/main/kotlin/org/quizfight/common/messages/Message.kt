package org.quizfight.common.messages

import org.quizfight.common.question.Question
import java.io.Serializable

interface Message : Serializable


data class GameData(val id: Int, val gameName:String, val maxPlayer: Int, val questions: MutableList<Question>) //TODO: Add ip for GameServer


data class MsgGetAllOpenGames   (val openGames: List<GameData>) : Message
//data class MsgGetOpenGames      (val openGames: List<GameData>) : Message
data class MsgGetOpenGames      (val tmp: Boolean) : Message
data class MsgJoinGame          (val id: Int, val playerName: String) : Message
data class MsgGameJoined        (val gameData: GameData) : Message
data class MsgCreateGame        (val gameData: GameData) :Message
data class MsgSendGameCreated   (val gameData: GameData) :Message
data class MsgSendQuestion      (val question: Question) : Message
data class MsgSendAnswer        (val id: Int, val score: Int): Message
data class MsgStartGame         (val id: Int) : Message
data class MsgSendOpenGames     (val openGames: List<GameData>) : Message