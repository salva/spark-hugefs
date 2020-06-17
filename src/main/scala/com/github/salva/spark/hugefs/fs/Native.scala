package com.github.salva.spark.hugefs.fs

import java.io.IOException
import java.nio.file._

import com.github.salva.spark.hugefs.FS
import com.github.salva.spark.hugefs.impl.fs.Entry

import scala.collection.JavaConverters._

object Native extends FS {

  class NativeEntry(val absPath: Path, val relPath: Path) extends Entry {

    def path: String = relPath.toString

    lazy val isDir = Files.isDirectory(absPath)
    lazy val isFile = Files.isRegularFile(absPath)
    lazy val isSymLink = Files.isSymbolicLink(absPath)

    def makeChild(childAbsPath: Path) = new NativeEntry(childAbsPath, relPath.resolve(childAbsPath.getFileName))

    override def ls: Seq[Entry] = {
      try {
        val stream = Files.newDirectoryStream(absPath)
        try {
          stream
            .iterator
            .asScala
            .map(makeChild(_))
            .toList // we convert to a list here in order
          // to catch iterator exceptions
        }
        finally {
          stream.close()
        }
      }
      catch {
        case e:DirectoryIteratorException => throw new IOException(e)
        case e:FileSystemException => throw new IOException(e)
      }
    }
  }

  override def pathToEntry(base: String, path: String): Entry = new NativeEntry(Paths.get(base, path), Paths.get(path))
}
