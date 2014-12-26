package com.brokendata

import com.brokendata.Utils._
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD

object NewsGroup {

  def main(args: Array[String]) = {

    /*
    Stopwords will be boradcast to all spark RDD's across the cluster.

    val stopWords = sc.broadcast(loadStopWords("stopwords.txt")).value

    */
    val conf = new SparkConf().setAppName("SimpleTextClassificationPipeline")
    val sc = new SparkContext(conf)




  }

}
