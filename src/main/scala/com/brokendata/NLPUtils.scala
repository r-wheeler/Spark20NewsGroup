package com.brokendata

import com.brokendata.Utils._
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, TokensAnnotation, SentencesAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.apache.spark.mllib.feature.{Normalizer, IDF, HashingTF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

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

  def tfidfTransformer(data: RDD[LabeledDocument],
                       lableMap: Map[String,Int],
                       norm: Boolean = false): RDD[LabeledPoint] = {
    /**
     * Implements TFIDF via Sparks built in methods. Because idfModel requires and RDD[Vector] we are not able to pass directly in
     * a RDD[LabeledPoint]. A work around is to save the LabeledPoint.features to a var (hashedData), transform the data, then  zip
     * the labeled dataset and the transformed IDFs and project them to a new LabeledPoint

      Data: RDD of type LabledDocument
      LabelMap: a hashmap containing text labels to numeric labels ("alt.atheism" -> 4)
     */
    val tf = new HashingTF()
    val freqs = data.map(x => (LabeledPoint(lableMap(x.label), tf.transform(x.body)))).cache()
    val hashedData = freqs.map(_.features)
    val idfModel = new IDF().fit(hashedData)
    val idf = idfModel.transform(hashedData)
    val LabeledVectors = if (norm == true) {
      val l2 = new Normalizer()
      idf.zip(freqs).map(x => LabeledPoint(x._2.label, l2.transform(x._1)))
    } else {
      idf.zip(freqs).map(x => LabeledPoint(x._2.label, x._1))
    }
    LabeledVectors
  }

}

case class LabeledDocument(id: String, body: Seq[String], label: String)