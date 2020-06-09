package com.github.salva.spark.hugefs.restriction

import com.github.salva.spark.hugefs.{Restriction, Verdict}

class MaxDepth(val maxDepth: Long) extends(Restriction) {
  def charInStringCount(str:String, c:Char): Long = str.chars.filter(_ == c).count

  def pathDepth(path:String):Long = {
    if (path == "") 0 else 1 + charInStringCount(path, '/')
  }

  override def evaluateCheap(path:String): Verdict = {
    val depth = pathDepth(path)
    Verdict(depth <= maxDepth, depth < maxDepth)
  }
}
