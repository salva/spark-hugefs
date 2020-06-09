package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.{Restriction, Verdict}

class Filename(val filenames:Seq[String], val caseInsensitive:Boolean) extends Restriction {
  val cmps = filenames.map(fn => if (caseInsensitive) fn.equalsIgnoreCase(_) else fn.equals(_))

  def this(filename:String, caseInsensitive:Boolean) = this(Seq(filename), caseInsensitive)

  override def evaluateCheap(path:String):Verdict = {
    val start = path.lastIndexOf("/") + 1
    val currentFilename = path.substring(start)

    Verdict(cmps.map(_(path)).reduce(_||_), true)
  }
}