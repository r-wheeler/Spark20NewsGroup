package com.brokendata

import com.brokendata.Utils._
import com.brokendata.NLPUtils._
import org.apache.spark.mllib.classification.NaiveBayes

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

object NewsGroup {

  def main(args: Array[String]) = {

    /*
    Stopwords will be boradcast to all spark RDD's across the cluster.



    */
    val conf = new SparkConf().setAppName("SimpleTextClassificationPipeline")
    val sc = new SparkContext("local[2]", "List Combine")

    //Stopwords are broadcast to all RDD's across the cluster
     val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value


    /**
     * Training dataset is load via wholeTextFiles which returns an array of tuples
     * in the form of (pathToDocument, Text) without parsing the text like sc.textFiles
     */

    //val training = sc.wholeTextFiles("data/training/*").map(rawText => createLabledDocument(rawText, stopWords))
    val single = sc.wholeTextFiles("/home/ryan/git/Spark20NewsGroup/data/training/alt.atheism/53068").map(rawText => createLabledDocument(rawText, stopWords))

    val labelToNumeric = convertLabelToNumeric("data/training/") // map containing labels to numeric values for labeled Naive Bayes. "alt.atheism" -> 4

    val tf = new HashingTF(10000)
    // create a new labled point object consiting of (numericLable, Vector) tuples
    val freqs = single.map(x => (LabeledPoint(labelToNumeric(x.label), tf.transform(x.body)))).cache()
    val hashedData = freqs.map(x => x.features)
    val idfModel = new IDF().fit(hashedData)
    val idf = idfModel.transform(hashedData)
    val labledTFIDF = idf.zip(freqs).map(x => LabeledPoint(x._2.label, x._1))

    val model = NaiveBayes.train(labledTFIDF,lambda = 1.0)



  }

}

/*
Good example of tfidf + NaiveBayes
http://mail-archives.apache.org/mod_mbox/spark-user/201409.mbox/%3CCAJgQjQ_ZmO-P_2Ortv16vH-GpN9=GQGDzCWAuytH+=Ws8EqhiA@mail.gmail.com%3E
 */