package com.brokendata

import com.brokendata.Utils._
import com.brokendata.NLPUtils._
import com.brokendata.LabeledDocument
import org.apache.spark.mllib.classification.NaiveBayes

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.feature.{HashingTF, IDF, Normalizer}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD


object NewsGroup {

  def main(args: Array[String]) = {

    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("SimpleTextClassificationPipeline")
      .set("spark.executor.memory","2g")
      .set("spark.driver.memory", "3g")
    //val sc = new SparkContext("local[*]", "List Combine")
    val sc = new SparkContext(conf)

    //Stopwords are broadcast to all RDD's across the cluster
    val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value

    // tokenize, stem, etc
    val training = sc.wholeTextFiles("data/training/*").map(rawText => createLabeledDocument(rawText, stopWords))
    val test = sc.wholeTextFiles("data/training/*").map(rawText => createLabeledDocument(rawText, stopWords))

    //create feature Vectors
    val forumIndexMap = convertLabelToNumeric("data/training/") // map containing labels to numeric values for labeled Naive Bayes. "alt.atheism" -> 4
    val X_train = tfidfTransformer(training,forumIndexMap)
    val X_test = tfidfTransformer(training,forumIndexMap)

    //fix MultiNomial Naive Bayes
    val model = NaiveBayes.train(X_train,lambda = 1.0)

  }


}

/*
Good example of tfidf + NaiveBayes
http://mail-archives.apache.org/mod_mbox/spark-user/201409.mbox/%3CCAJgQjQ_ZmO-P_2Ortv16vH-GpN9=GQGDzCWAuytH+=Ws8EqhiA@mail.gmail.com%3E
 */