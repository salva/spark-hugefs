package com.github.salva.spark.hugefs

import java.io.IOException

import com.github.salva.spark.hugefs.fs.impl.Native
import com.github.salva.spark.hugefs.fs.{Entry, FS}
import com.github.salva.spark.hugefs.restriction.Good
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

class Walker(val sparkSession:SparkSession, val fs:FS=Native) {
  def walk(base:String, restriction:Restriction=Good, ignoreErrors:Boolean=true): DataFrame = {
    import sparkSession.implicits._
    val initDS = sparkSession.createDataset(Seq(""))

    Walker.expand(fs, base, initDS, restriction, ignoreErrors).toDF("path")
  }
}

object Walker extends Serializable {

  case class PathAndVerdict(path: String, good: Boolean, live: Boolean)

  def evaluateEntry(entry: Entry, restriction: Restriction, ignoreErrors: Boolean): PathAndVerdict = {
    try {
      val verdict = restriction.evaluate(entry)
      new PathAndVerdict(entry.path, verdict.good, verdict.live)
    }
    catch {
      case e:IOException => {
        if (ignoreErrors) new PathAndVerdict(entry.path, false, false)
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
      case e:IOException => if (ignoreErrors) Nil else throw e
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
}