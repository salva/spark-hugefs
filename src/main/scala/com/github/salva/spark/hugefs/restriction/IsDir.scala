package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.{Restriction, Verdict}
import com.github.salva.spark.hugefs.fs.Entry

object IsDir extends Restriction {
  override def evaluateHeavy(entry: Entry): Verdict = Verdict(entry.isDir, true)
}
