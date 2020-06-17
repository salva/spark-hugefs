package com.github.salva.spark.hugefs.fs

trait Entry {
  def path:String
  def isDir:Boolean
  def isFile:Boolean
  def isSymLink:Boolean
  def ls:Seq[Entry]
}

trait FS extends Serializable {
  def pathToEntry(base:String, path:String):Entry
  def cleanBase(base:String):String = base
}

