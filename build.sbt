name := "spark-hugefs"

//version := "0.1"

scalaVersion := "2.11.12"

sparkComponents += "sql"

//sparkVersion := "3.0.0-preview2"
sparkVersion := "2.4.5"

libraryDependencies += "com.databricks" %% "dbutils-api" % "0.0.4"

//githubOwner := "salva"
//githubRepository := "spark-hugefs"

organization := "com.github.salva"
licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
description := "Walk big and deep filesystem fast and efficiently from Spark"

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("salva", "spark-hugefs", "sfandino@yahoo.com"))

publishTo := sonatypePublishTo.value

