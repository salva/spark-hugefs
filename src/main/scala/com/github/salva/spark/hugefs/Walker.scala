package com.github.salva.spark.hugefs

import java.io.IOException

import com.databricks.dbutils_v1.DbfsUtils
import com.github.salva.spark.hugefs.fs.{DBFS, Native}
import com.github.salva.spark.hugefs.impl.fs.Entry
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.slf4j.LoggerFactory

import scala.language.implicitConversions

class Walker(val spark:SparkSession, val fs:FS=Native, val partitions:Int=30) {
  def walk(base:String, restriction:Restriction=Good, ignoreErrors:Boolean=true): DataFrame = {
    import spark.implicits._
    val initDS = spark.createDataset(Seq(""))
    val cleanBase = fs.cleanBase(base)
    Walker.expand(fs, cleanBase, initDS, restriction, ignoreErrors, partitions).toDF("path").cache
  }
}

object Walker extends Serializable {

  lazy val logger = LoggerFactory.getLogger("com.github.salva.spark.hugefs")

  case class PathAndVerdict(path: String, good: Boolean, live: Boolean)

  def evaluateEntry(entry: Entry, restriction: Restriction, ignoreErrors: Boolean): PathAndVerdict = {
    try {
      val verdict = restriction.evaluate(entry)
      PathAndVerdict(entry.path, verdict.good, verdict.live)
    }
    catch {
      case e:IOException => {
        if (ignoreErrors) {
          logger.warn(s"Ignoring IO error: ${e.getMessage}")
          PathAndVerdict(entry.path, false, false)
        }
        else throw e
      }
    }
  }

  def expandPath(fs:FS, base:String, path: String, restriction:Restriction, ignoreErrors:Boolean):Seq[PathAndVerdict] = {
    val entry = fs.pathToEntry(base, path)
    try {
      entry.ls.map(evaluateEntry(_, restriction, ignoreErrors))
    }
    catch {
      case e:IOException => {
        if (ignoreErrors) {
          logger.warn(s"Ignoring IO error: ${e.getMessage}")
          Nil
        }
        else throw e
      }
    }
  }

  def expand(fs:FS, base:String, paths:Dataset[String], restriction:Restriction,
             ignoreErrors:Boolean, partitions:Int):Dataset[String] = {
    import paths.sparkSession.implicits._
    val nextLevel = paths.flatMap(path => expandPath(fs, base, path, restriction, ignoreErrors)).cache
    val good = nextLevel.filter(_.good).map(_.path)
    val live = nextLevel.filter(_.live).map(_.path)
    if (live.isEmpty) good
    else good.union(expand(fs, base, live.repartition(partitions), restriction, ignoreErrors, partitions))
  }

  implicit def fromDbfsUtilsToFS(fs:DbfsUtils):FS = new DBFS(fs)
}