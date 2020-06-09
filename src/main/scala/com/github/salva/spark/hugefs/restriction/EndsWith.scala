package com.github.salva.spark.hugefs.restriction

import java.util.regex.Pattern

import com.github.salva.spark.hugefs.{Restriction, Verdict}

class EndsWith(val ends:Seq[String], caseInsensitive:Boolean) extends Restriction {
  val cmps = ends.map { end =>
    if (caseInsensitive)
      (str:String) => Pattern.compile(Pattern.quote(end) + "$", Pattern.CASE_INSENSITIVE).matcher(str).find
    else
      (str:String) => str.endsWith(end)
  }

  def this(end:String, caseInsensitive:Boolean) = this(Seq(end), caseInsensitive)

  override def evaluateCheap(path: String): Verdict = Verdict(cmps.map(_(path)).reduce(_||_), true)
}
