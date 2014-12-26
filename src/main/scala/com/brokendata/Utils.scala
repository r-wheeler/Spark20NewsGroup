package com.brokendata

import java.io.File


object Utils {
  //-------------------------------------------------------------------------------------------------------------
  //File Utils
  //-------------------------------------------------------------------------------------------------------------

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
           else Stream.empty)

  def getFileAndParent(f: File): Stream[(String,String)] =
    getFileTree(f) map(x => (x.getName, x.getParentFile.getName))







}
