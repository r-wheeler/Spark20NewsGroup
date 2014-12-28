package com.brokendata

/**
 * Created by ryan on 12/27/14.
 */
object notes {

  /*
  Notes:

  NLP Pre-Processing:
    Stanford NLP seems really slow, will investigate chalk




  TFIDF:
  The TFIDF vectorizor expects the data to be a RDD[Vector], when attempting to fit the idf model where the data was
  RDD[Int, Vector], calling idf.transform(RDD[INT, Vector]._2) would fail. digging around showed that this generally agreed as not good
  with calls for a PR to fix it. A work around in to create 2 copies of the data:
    - 1 data set consisting of LabledPoint(numericLabel, Vector)
    - another dataset that is just the vectors
    - then zip the 2 together and return a new labled point.


   */

}
