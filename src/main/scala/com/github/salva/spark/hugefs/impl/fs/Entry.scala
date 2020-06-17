package com.github.salva.spark.hugefs.impl.fs

trait Entry {
  def path:String
  def isDir:Boolean
  def isFile:Boolean
  def isSymLink:Boolean
  def ls:Seq[Entry]
}
