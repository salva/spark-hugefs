package com.github.salva.spark.hugefs.fs

import com.databricks.backend.daemon.dbutils.FileInfo
import com.databricks.dbutils_v1.DbfsUtils
import com.github.salva.spark.hugefs.FS
import com.github.salva.spark.hugefs.impl.fs.Entry

import scala.language.implicitConversions

class DBFS(val fs:DbfsUtils) extends FS {
  case class DBFSEntry(absPath:String, path:String) extends Entry {
    def isDir = absPath.endsWith("/")
    def isFile = !isDir
    def isSymLink = false
    def ls:Seq[Entry] = fs.ls(absPath).map(makeSon(_))

    def makeSon(fi:FileInfo):DBFSEntry = {
      val sonName = if (fi.name.endsWith("/")) fi.name.substring(0, fi.name.length - 1) else fi.name
      DBFSEntry(fi.path, if (path == "") sonName else path + "/" + sonName)
    }
  }

  override def pathToEntry(base: String, path: String): Entry = {
    DBFSEntry(base + "/" + path, path)
  }

  override def cleanBase(base: String): String = {
    val uriBreaker = """(?:dbfs:)?(.*?)/*""".r
    base match {
      case uriBreaker(filePath) => "dbfs:" + filePath
      case _ => throw new IllegalArgumentException("Bad path")
    }
  }
}
