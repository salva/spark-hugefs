package com.github.salva.spark.hugefs.fs.impl

import com.github.salva.spark.hugefs.fs.{Entry, FS}

class FSTracer(val child:FS, dump:String=>Unit=println) extends FS {

  case class FSTracerEntry(child:Entry) extends Entry {
    def path = child.path
    def isDir = {
      val result = child.isDir
      dump(s"FSTracer: isDir(${child.path}) => $result")
      result
    }
    def isFile = child.isFile
    def isSymLink = child.isSymLink
    def ls:Seq[Entry] = {
      val result = child.ls
      val resultAsString = result.map(_.path).mkString(", ")
      dump(s"""FSTracer: ls(${child.path}) => $resultAsString""")
      result
    }
  }

  override def cleanBase(base: String): String = child.cleanBase(base)

  override def pathToEntry(base: String, path: String): Entry = FSTracerEntry(child.pathToEntry(base, path))
}
