package com.brokendata

import com.brokendata.Utils._
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, TokensAnnotation, SentencesAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._



object NLPUtils {

  def tokenizeAndStem(text: String, stopWords: Set[String] ): Seq[String] = {
    val props = new Properties()
    props.put("annotators", "tokenize, ssplit, pos, lemma")

    val pipeline = new StanfordCoreNLP(props)
    val doc = new Annotation(text)

    pipeline.annotate(doc)

    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences;
         token <- sentence.get(classOf[TokensAnnotation])) {
      val lemma = token.get(classOf[LemmaAnnotation])
      if (lemma.length > 2 && !stopWords.contains(lemma)
        && isOnlyLetters(lemma)) {
        lemmas += lemma.toLowerCase
      }
    }

    lemmas
  }

  def loadStopWords(path: String): Set[String] =
    scala.io.Source.fromURL(getClass.getResource(path))
    .getLines().toSet

  def isOnlyLetters(str: String): Boolean = {
    // While loop for high performance
    var i = 0
    while (i < str.length) {
      if (!Character.isLetter(str.charAt(i))) {
        return false
      }
      i += 1
    }
    true
  }

  def createLabeledDocument(wholeTextFile: (String,String), stopWords:Set[String]): LabeledDocument = {
    /**
     * Parse the wholeTextFile and return a LabledDocument
     * wholeTextFile._1 is the path, this is parsed for the label and doc ID
     * wholeTextFile._2 is the text, this is tokenized and stemmed
     */

    val (label, id) = getLabelandId(wholeTextFile._1)
    val processedDoc = tokenizeAndStem(wholeTextFile._2, stopWords)
    LabeledDocument(id, processedDoc, label)

  }

}

case class LabeledDocument(id: String, body: Seq[String], label: String)