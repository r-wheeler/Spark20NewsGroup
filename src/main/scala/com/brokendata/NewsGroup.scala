package com.brokendata

import com.brokendata.Utils._
import com.brokendata.NLPUtils._
import com.brokendata.LabeledDocument
import org.apache.spark.mllib.classification.NaiveBayes

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.feature.{HashingTF, IDF, Normalizer}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD


object NewsGroup {

  def main(args: Array[String]) = {

    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("SimpleTextClassificationPipeline")
      .set("spark.executor.memory","2g")
    //val sc = new SparkContext("local[*]", "List Combine")
    val sc = new SparkContext(conf)

    //Stopwords are broadcast to all RDD's across the cluster
    val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value

    // tokenize, stem, etc
    val training = sc.wholeTextFiles("data/training/*").map(rawText => createLabeledDocument(rawText, stopWords))
    val test = sc.wholeTextFiles("file:////home/ryan/git/Spark20NewsGroup/data/training/*").map(rawText => createLabeledDocument(rawText, stopWords))

    //create feature Vectors
    val forumIndexMap = convertLabelToNumeric("data/training/") // map containing labels to numeric values for labeled Naive Bayes. "alt.atheism" -> 4
    val X_train = tfidfTransformer(training,forumIndexMap)
    val X_test = tfidfTransformer(training,forumIndexMap)

    //fix MultiNomial Naive Bayes
    val model = NaiveBayes.train(X_train,lambda = 1.0)

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

/*
Good example of tfidf + NaiveBayes
http://mail-archives.apache.org/mod_mbox/spark-user/201409.mbox/%3CCAJgQjQ_ZmO-P_2Ortv16vH-GpN9=GQGDzCWAuytH+=Ws8EqhiA@mail.gmail.com%3E
 */