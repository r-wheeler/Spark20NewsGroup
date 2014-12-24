package com.brokendata

import java.io.File

/**
 * Created by ryan on 12/23/14.
 */
object Utils {

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
           else Stream.empty)

  def getFileAndParent(f: File): Stream[(String,String)] = {
    getFileTree(f) map(x => (x.getName, x.getParentFile.getName))
  }


}
