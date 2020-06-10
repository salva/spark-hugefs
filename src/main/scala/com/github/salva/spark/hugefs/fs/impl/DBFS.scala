package com.github.salva.spark.hugefs.fs.impl

import com.databricks.backend.daemon.dbutils.FileInfo
import com.github.salva.spark.hugefs.fs.{Entry, FS}

class DBFS extends FS {

  lazy val fs = com.databricks.dbutils_v1.DBUtilsHolder.dbutils.fs

  case class DBFSEntry(absPath:String, path:String) extends Entry {
    def isDir = absPath.endsWith("/")
    def isFile = !isDir
    def isSymLink = false
    def ls = fs.ls(absPath).map(makeSon(_))

    def makeSon(fi:FileInfo):DBFSEntry = {
      DBFSEntry(fi.path, if (path == "") fi.name else path + "/" + fi.name)
    }
  }

  override def pathToEntry(base: String, path: String): Entry = {
    DBFSEntry(base + "/" + path, path)
  }
}
