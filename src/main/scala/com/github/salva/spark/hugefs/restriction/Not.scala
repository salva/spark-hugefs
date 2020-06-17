package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.impl.fs.Entry
import com.github.salva.spark.hugefs.{Restriction, Verdict}

class Not(val r:Restriction) extends Restriction {
  override def evaluateHeavy(entry: Entry): Verdict = {
    val verdict = r.evaluateHeavy(entry)
    Verdict(!verdict.good, true)
  }
}
