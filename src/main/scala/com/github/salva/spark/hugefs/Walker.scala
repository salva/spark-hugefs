package com.github.salva.spark.hugefs

import java.io.IOException

import com.github.salva.spark.hugefs.fs.impl.Native
import com.github.salva.spark.hugefs.fs.{Entry, FS}
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

class Walker(val spark:SparkSession, val fs:FS=Native) {
  def walk(base:String, restriction:Restriction=Good, ignoreErrors:Boolean=true): DataFrame = {
    import spark.implicits._
    val initDS = spark.createDataset(Seq(""))
    val cleanBase = fs.cleanBase(base)
    Walker.expand(fs, cleanBase, initDS, restriction, ignoreErrors).toDF("path").cache
  }
}

object Walker extends Serializable with WalkerHelper with LazyLogging {

  case class PathAndVerdict(path: String, good: Boolean, live: Boolean)

  def evaluateEntry(entry: Entry, restriction: Restriction, ignoreErrors: Boolean): PathAndVerdict = {
    try {
      val verdict = restriction.evaluate(entry)
      PathAndVerdict(entry.path, verdict.good, verdict.live)
    }
    catch {
      case e:IOException => {
        if (ignoreErrors) {
          logger.warn(s"Ignoring IO error for ${entry.path}: ${e.getMessage}")
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
          logger.warn(s"Ignoring IO error for ${entry.path}: ${e.getMessage}")
          Nil
        }
        else throw e
      }
    }
  }

  def expand(fs:FS, base:String, paths:Dataset[String], restriction:Restriction, ignoreErrors:Boolean):Dataset[String] = {
    import paths.sparkSession.implicits._
    val nextLevel = paths.flatMap(path => expandPath(fs, base, path, restriction, ignoreErrors))
    val good = nextLevel.filter(_.good).map(_.path)
    val live = nextLevel.filter(_.live).map(_.path)
    if (live.isEmpty) good
    else good.union(expand(fs, base, live.repartition(100), restriction, ignoreErrors))
  }

  def walk(spark:SparkSession, fullBase:String, restriction:Restriction=Good) = {
    val (fs, base) = breakFullBase(fullBase)
    val walker = new Walker(spark)
    walker.walk(base, restriction)
  }

}