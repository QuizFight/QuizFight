package org.quizfight.quizfight

class Person(name:String){

    lateinit var prenom : String

    init{
        prenom = name
    }

    fun im(){
        println(prenom)
    }
}
fun main(args: Array<String>) {

    val p = Person("sadou")

    p.im()
}
