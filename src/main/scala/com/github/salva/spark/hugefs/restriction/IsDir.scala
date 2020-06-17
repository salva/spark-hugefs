package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.impl.fs.Entry
import com.github.salva.spark.hugefs.{Restriction, Verdict}

object IsDir extends Restriction {
  override def evaluateHeavy(entry: Entry): Verdict = Verdict(entry.isDir, true)
}
