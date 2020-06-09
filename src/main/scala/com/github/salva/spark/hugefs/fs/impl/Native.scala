package com.github.salva.spark.hugefs.fs.impl

import java.io.IOException
import java.nio.file.{DirectoryIteratorException, FileSystemException, Files, Path, Paths}

import com.github.salva.spark.hugefs.fs.{Entry, FS}

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
        Files.newDirectoryStream(absPath)
          .iterator
          .asScala
          .map(makeChild(_))
          .toList // we convert to a list here in order
                  // to catch iterator exceptions
      }
      catch {
        case e:DirectoryIteratorException => throw new IOException(e)
        case e:FileSystemException => throw new IOException(e)
      }
    }
  }

  override def pathToEntry(base: String, path: String): Entry = new NativeEntry(Paths.get(base, path), Paths.get(path))
}