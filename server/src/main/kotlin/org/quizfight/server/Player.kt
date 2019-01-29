package org.quizfight.server

/**
 * Player Class. Manages the Connection between a mobile device and the Server.
 * Includes player's score
 * @author Thomas Spanier
 */
class Player(name : String) {
    private val name: String
    private var score: Int

    init {
        this.name = name
        this.score = 0
    }


    fun getName():String{
        return name
    }

    fun getScore():Int{
        return score
    }

    fun setScore(score:Int){
        this.score = score
    }

    fun addToScore(value:Int){
        score += value
    }




    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (name != other.name) return false

        return true
    }

    override fun toString(): String {
        return "Player(name='$name', score=$score)"
    }


}