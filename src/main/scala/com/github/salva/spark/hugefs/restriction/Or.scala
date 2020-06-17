package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.impl.fs.Entry
import com.github.salva.spark.hugefs.{Restriction, Verdict}

class Or(val a:Restriction, val b:Restriction) extends Restriction {
  override def evaluateCheap(path:String):Verdict = {
    val verdictA = a.evaluateCheap(path)
    if (!verdictA.good || !verdictA.live) {
      val verdictB = b.evaluateCheap(path)
      Verdict(verdictA.good || verdictB.good, verdictA.live || verdictB.live)
    }
    else verdictA
  }

  override def evaluateHeavy(entry: Entry): Verdict = {
    val verdictA = a.evaluateHeavy(entry)
    if (!verdictA.good || !verdictA.live) {
      val verdictB = b.evaluateHeavy(entry)
      Verdict(verdictA.good || verdictB.good, verdictA.live || verdictB.live)
    }
    else verdictA
  }

}
