package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.{Restriction, Verdict}
import com.github.salva.spark.hugefs.fs.Entry

class Not(val r:Restriction) extends Restriction {
  override def evaluateHeavy(entry: Entry): Verdict = {
    val verdict = r.evaluateHeavy(entry)
    Verdict(!verdict.good, true)
  }
}
