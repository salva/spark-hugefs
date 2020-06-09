package com.github.salva.spark.hugefs

import com.github.salva.spark.hugefs.fs.Entry

final case class Verdict(good:Boolean, live:Boolean) {
  def ||(other:Verdict) = new Verdict(good || other.good, live || other.live)
  def &&(other:Verdict) = new Verdict(good && other.good, live && other.live)
}

trait Restriction extends Serializable {

  def evaluateCheap(path:String):Verdict = Verdict(true, true)
  def evaluateHeavy(entry:Entry):Verdict = evaluateCheap(entry.path)

  final def evaluate(entry:Entry):Verdict = {
    val cheapVerdict = evaluateCheap(entry.path)
    val live = cheapVerdict.live && entry.isDir
    if (cheapVerdict.good || live) {
      val heavyVerdict = evaluateHeavy(entry)
      Verdict(cheapVerdict.good && heavyVerdict.good, live && heavyVerdict.live)
    }
    else Verdict(false, false)
  }

  def &&(other:Restriction) = {
    if (this == com.github.salva.spark.hugefs.restriction.Good) other
    else new com.github.salva.spark.hugefs.restriction.And(this, other)
  }
  def ||(other:Restriction) = new com.github.salva.spark.hugefs.restriction.Or(this, other)
  def unary_! = new com.github.salva.spark.hugefs.restriction.Not(this)

  def isDir: Restriction =
    this && com.github.salva.spark.hugefs.restriction.IsDir
  def isFile: Restriction =
    this && com.github.salva.spark.hugefs.restriction.IsFile
  def endsWith(end:String, caseInsensitive:Boolean=false):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.EndsWith(end, caseInsensitive)
  def endsWithAny(ends:Seq[String], caseInsensitive:Boolean=false):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.EndsWith(ends, caseInsensitive)
  def maxDepth(maxDepth: Long):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.MaxDepth(maxDepth)
  def filename(filename:String, caseInsensitive:Boolean=false):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.Filename(filename, caseInsensitive)
  def filenameAny(filenames:Seq[String], caseInsensitive:Boolean=false):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.Filename(filenames, caseInsensitive)
  def glob(glob:String, caseInsensitive:Boolean=false):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.Glob(Seq(glob), caseInsensitive)
  def globAny(globs:Seq[String], caseInsensitive:Boolean=false):Restriction =
    this && new com.github.salva.spark.hugefs.restriction.Glob(globs, caseInsensitive)
  def dontFollowSymLinks:Restriction =
    this && com.github.salva.spark.hugefs.restriction.DontFollowSymLinks
  def isSymLink: Restriction =
    this && com.github.salva.spark.hugefs.restriction.IsSymLink
}

object Restriction {
  def any(rs:Seq[Restriction]): Restriction = rs.reduce(_ || _)
  def all(rs:Seq[Restriction]): Restriction = rs.reduce(_ && _)
}