name := "spark-hugefs"

logLevel := Level.Debug

version := "0.5"

scalaVersion := "2.11.12"

sparkComponents += "sql"
sparkVersion := "2.4.5"

libraryDependencies += "com.databricks" %% "dbutils-api" % "0.0.4"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"


organization := "com.github.salva"
organizationHomepage := Some(url("https://github.com/salva"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/salva/spark-hugefs"),
    "https://github.com/salva/spark-hugefs.git"
  )
)

developers := List (
  Developer(
    id = "salva",
    name = "Salvador Fandiño",
    email = "sfandino@yahoo.com",
    url = url("https://github.com/salva")
  )
)

description := "Walk big and deep filesystem fast and efficiently from Spark"
licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/salva/spark-hugefs"))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true
//sonatypeProjectHosting := Some(GitHubHosting("salva", "spark-hugefs", "sfandino@yahoo.com"))
