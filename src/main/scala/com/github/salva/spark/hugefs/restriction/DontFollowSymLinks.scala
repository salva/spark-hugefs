package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.{Restriction, Verdict}
import com.github.salva.spark.hugefs.fs.Entry

object DontFollowSymLinks extends Restriction {
  override def evaluateHeavy(entry: Entry): Verdict = Verdict(true, !entry.isSymLink)
}
