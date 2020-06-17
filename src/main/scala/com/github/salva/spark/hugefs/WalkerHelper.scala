package com.github.salva.spark.hugefs

import com.github.salva.spark.hugefs.fs.FS
import com.github.salva.spark.hugefs.fs.impl.{DBFS, Native}

trait WalkerHelper {
  def breakFullBase(fullBase:String):(FS, String) = {
    if (fullBase.startsWith("/")) (Native, fullBase)
    else if (fullBase.startsWith("dbfs:")) (new DBFS(), fullBase)
    else throw new IllegalArgumentException("Unrecognized file system in path")
  }
}
