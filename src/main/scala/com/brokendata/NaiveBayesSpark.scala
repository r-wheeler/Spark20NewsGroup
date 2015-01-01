package com.brokendata

import com.brokendata.Utils._
import com.brokendata.NLPUtils._
import com.brokendata.LabeledDocument
import org.apache.spark.mllib.classification.NaiveBayes

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.feature.{HashingTF, IDF, Normalizer}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD


object NaiveBayesSpark {

  def main(args: Array[String]) = {

    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("NaiveBayes")
      .set("spark.driver.memory", "3g")
    val sc = new SparkContext(conf)

    //Stopwords are broadcast to all RDD's across the cluster
    val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value
    // map containing labels to numeric values for labeled Naive Bayes. "alt.atheism" -> 4
    val labelToNumeric = createLabelMap("data/training/")

    // tokenize, stem,
    val training = sc.wholeTextFiles("data/training/*")
      .map(rawText => createLabeledDocument(rawText, labelToNumeric, stopWords))
    val test = sc.wholeTextFiles("data/test/*")
      .map(rawText => createLabeledDocument(rawText,labelToNumeric, stopWords))

    //create features
    val X_train = tfidfTransformer(training)
    val X_test = tfidfTransformer(training)

    //fix MultiNomial Naive Bayes
    val model = NaiveBayes.train(X_train,lambda = 1.0)
    val predictionAndLabel = X_test.map(x => (model.predict(x.features), x.label))
    val accuracy = 1.0 *  predictionAndLabel.filter(x => x._1 == x._2).count() / X_test.count()

    println("*************Accuracy Report:***********************")
    println(accuracy)

  }


}

/*
Good example of tfidf + NaiveBayes
http://mail-archives.apache.org/mod_mbox/spark-user/201409.mbox/%3CCAJgQjQ_ZmO-P_2Ortv16vH-GpN9=GQGDzCWAuytH+=Ws8EqhiA@mail.gmail.com%3E
 */