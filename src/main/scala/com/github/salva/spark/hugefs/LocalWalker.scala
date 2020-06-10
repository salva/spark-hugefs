package com.github.salva.spark.hugefs

import java.io.IOException

import com.github.salva.spark.hugefs.fs.{Entry, FS}
import com.github.salva.spark.hugefs.fs.impl.Native

class LocalWalker(val fs:FS=Native) extends Serializable {

  case class EntryAndVerdict(entry:Entry, good:Boolean, live:Boolean)

  def walk(base:String, restriction:Restriction=Good, ignoreErrors:Boolean=true): Seq[String] = {
    walk(fs.pathToEntry(base, ""), restriction, ignoreErrors).map(_.path)
  }

  def evaluateEntry(entry:Entry, restriction:Restriction, ignoreErrors:Boolean):EntryAndVerdict = {
    try {
      val verdict = restriction.evaluate(entry)
      new EntryAndVerdict(entry, verdict.good, verdict.live)
    }
    catch {
      case e:IOException => if (ignoreErrors) EntryAndVerdict(entry, false, false) else throw e
    }
  }

  def walk(entry:Entry, restriction:Restriction, ignoreErrors:Boolean): Seq[Entry] = {
    try {
      val entries = entry.ls.map(evaluateEntry(_, restriction, ignoreErrors))
      val good = entries.filter(_.good).map(_.entry)
      val live = entries.filter(_.live).map(_.entry)
      good ++ live.flatMap(walk(_, restriction, ignoreErrors))
    }
    catch {
      case e: IOException => if (ignoreErrors) Nil else throw e
    }
  }
}
