package org.quizfight.parser

import org.quizfight.common.question.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * XML-Parser for transforming xml-files and its nodes to Kotlin Question Objects
 * @author Julian Einspenner
 */
class XmlParser() {

    /* Types of questions for reading the type from XML */
    val CHOICE_QUESTION = "CHOICE_QUESTION";
    val GUESS_QUESTION  = "GUESS_QUESTION";

    val DIR_OF_XML = "/xml"

    /**
     * Reads every xmlFile and converts the data to a kotlin-object, representating the questions
     */
    fun convertXmlToQuestions(): List<Question<*>>{
        val paths: List<String> = getPaths()
        val documents = getDocumentsFromPaths(paths)

        return createQuestionObjectList(documents)
    }

    /**
     * Finds out the paths of every single xml-file including questions
     * @return is a list with the paths to every xml-file
     */
    fun getPaths() : List<String> {
        var paths = mutableListOf<String>()


        var res = XmlParser::class.java.getResource(DIR_OF_XML)
        var file = File(res.toURI())

        file.walk().forEach {
            if(it.isFile && it.path.endsWith(".xml", true)){
                paths.add(it.path)
            }
        }
        return paths
    }

    /**
     * This function gets a path-array. The array includes paths to every xml-questions-file
     * The function parses the content of these files to a Document-object
     * @param path is the array with paths
     */
    fun getDocumentsFromPaths(paths: List<String>): List<Document>{
        var documents = mutableListOf<Document>()

        for(path in paths){
            documents.add(readXmlFile(path))
        }
        return documents
    }

    /**
     * Reads a xml-document and transforms its content to Document-object
     * @return The Document-object
     */
    fun readXmlFile(path: String): Document {
        val xmlFile = File(path)

        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val xml = InputSource(StringReader(xmlFile.readText()))
        val doc: Document = builder.parse(xml)

        return doc
    }

    /**
     * Uses a When-Else statement to distinguish the types of questions
     * The attribute type of the xml-node 'questions' is read for getting the
     * correct case.
     */
    fun createQuestionObjectList(documents: List<Document>): List<Question<*>>{
        var resultList = mutableListOf<Question<*>>()

        for(document in documents){
            val type = document.documentElement.getAttribute("type")
            var temporaryList = mutableListOf<Question<*>>()

            var question: Question<*>
            when(type){
                CHOICE_QUESTION -> temporaryList = getChoiceQuestions(document) as MutableList<Question<*>>
                GUESS_QUESTION  -> temporaryList = getGuessQuestions(document)  as MutableList<Question<*>>
                else             -> System.err.println("Found bad XML -> Skipped it")
            }

            resultList = (resultList + temporaryList).toMutableList()
        }
        return resultList
    }

    /**
     * Parses the questions of a XML-File with questions from type FourAnswers
     * @param document is the xml-representation
     * @return is the question list parsed from xml
     */
    fun getChoiceQuestions(document: Document): MutableList<ChoiceQuestion> {
        var questionList = mutableListOf<ChoiceQuestion>()

        val questionNodes = document.getElementsByTagName("question")

        for(i in 0..questionNodes.length - 1){
            val questionElement: Element = questionNodes.item(i) as Element

            val text          = questionElement.getElementsByTagName("text").item(0).textContent
            val correctAnswer = questionElement.getElementsByTagName("correct").item(0).textContent
            val category      = Category.valueOf(questionElement.parentNode.nodeName)

            val badAnswers   = questionElement.getElementsByTagName("bad")
            val badAnswer_1   = badAnswers.item(0).textContent
            val badAnswer_2   = badAnswers.item(1).textContent
            val badAnswer_3   = badAnswers.item(2).textContent

            val answers = listOf(correctAnswer, badAnswer_1, badAnswer_2, badAnswer_3)

            questionList.add(ChoiceQuestion(text, category, answers, correctAnswer))
        }
        return questionList
    }

    /**
     * Parses the questions of a XML-File with questions from type RangedAnswers
     * @param document is the xml-representation
     * @return is the question list parsed from xml
     */
    fun getGuessQuestions(document: Document): MutableList<GuessQuestion>{
        var questionList = mutableListOf<GuessQuestion>()

        val questionNodes = document.getElementsByTagName("question")

        for(i in 0..questionNodes.length - 1){
            val questionElement: Element = questionNodes.item(i) as Element

            val text          = questionElement.getElementsByTagName("text").item(0).textContent
            val correctAnswer = questionElement.getElementsByTagName("correct").item(0).textContent.toInt()
            val category      = Category.valueOf(questionElement.parentNode.nodeName)

            val begin   = questionElement.getElementsByTagName("begin").item(0).textContent.toInt()
            val end     = questionElement.getElementsByTagName("end").item(0).textContent.toInt()

            questionList.add(GuessQuestion(text, category, begin, end, correctAnswer))
        }
        return questionList
    }
}
