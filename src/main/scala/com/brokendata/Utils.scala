package com.brokendata

import java.io.File
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import scala.collection.JavaConversions._
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap


/*
 * Created by ryan on 12/23/14.
 */
object Utils {
  //-------------------------------------------------------------------------------------------------------------
  //File Utils
  //-------------------------------------------------------------------------------------------------------------

  def getFileTree(f: File): Stream[File] =
  /** Create a lazy stream for accessing all files in a directory possibly containing multiple sub directories
    *
    * @param f dicrectory to start recursive search
    */


    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
           else Stream.empty)

  def getFileAndParent(f: File): Stream[(String,String)] = {
    getFileTree(f) map(x => (x.getName, x.getParentFile.getName))
  }

  //-------------------------------------------------------------------------------------------------------------
  //NLP Utils
  //-------------------------------------------------------------------------------------------------------------

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

  def loadStopWords(path: String) = scala.io.Source.fromFile(path).getLines().toSet

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






}
