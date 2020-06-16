package com.github.salva.spark.hugefs.restriction

import com.github.salva.scala.glob.{Match, MatchResult, NoMatch}
import com.github.salva.spark.hugefs.fs.Entry
import com.github.salva.spark.hugefs.{Restriction, Verdict}

class Glob(val globs:Seq[String], caseInsensitive:Boolean) extends Restriction {
  if (globs.isEmpty) throw new IllegalArgumentException("At least one glob pattern is required")
  val compiledGlobs = globs.map(new com.github.salva.scala.glob.Glob(_))

  def this(glob:String, caseInsensitive:Boolean) = this(Seq(glob), caseInsensitive)

  def anyMatches(path:String):MatchResult = compiledGlobs.map(_.matches(path)).reduce(_||_)
  def anyMatchesPartially(path:String):MatchResult = compiledGlobs.map(_.matchesPartially(path)).reduce(_||_)

  override def evaluateCheap(path:String):Verdict =
    Verdict(anyMatches(path) != NoMatch, anyMatchesPartially(path) != NoMatch)

  override def evaluateHeavy(entry: Entry): Verdict = {
    val good = anyMatches(entry.path) match {
      case NoMatch => false
      case Match(false) => true
      case Match(true) => entry.isDir
    }
    Verdict(good, anyMatchesPartially(entry.path) != NoMatch)
  }
}
