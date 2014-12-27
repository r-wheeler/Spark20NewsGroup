package com.brokendata

import com.brokendata.Utils._
import com.brokendata.NLPUtils._

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD

object NewsGroup {

  def main(args: Array[String]) = {

    /*
    Stopwords will be boradcast to all spark RDD's across the cluster.



    */
    val conf = new SparkConf().setAppName("SimpleTextClassificationPipeline")
    val sc = new SparkContext("local[2]", "List Combine")

    /**
     * Stopwords are broadcast to all RDD's across the cluster
     *
     */

     val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value



    /**
     * Training dataset is load via wholeTextFiles which returns an array of tuples
     * in the form of (pathToDocument, Text) without parsing the text like sc.textFiles
     */

    /*val training = sc.wholeTextFiles("data/training/*")
      .map(rawText => LabledDocument())
       */

      */
  }

}

