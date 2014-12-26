package com.brokendata

import java.io.File


object Utils {
  //-------------------------------------------------------------------------------------------------------------
  //File Utils
  //-------------------------------------------------------------------------------------------------------------

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
           else Stream.empty)

  def getFileAndParent(path: String): Stream[(String,String)] = {
    val f = new File(path)
    getFileTree(f) map (x => (x.getName, x.getParentFile.getName))
  }

  def getLabelandId(path: String):(String, String) = {
    val spath = path.split("/")
    val label = spath.init.last
    val id = spath.last
    (label, id)

  }






}
