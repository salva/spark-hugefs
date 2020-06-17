package com.github.salva.spark.hugefs

import com.github.salva.spark.hugefs.impl.fs.Entry


trait FS extends Serializable {
  def pathToEntry(base:String, path:String):Entry
  def cleanBase(base:String):String = base
}

