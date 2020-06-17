name := "spark-hugefs"

version := "0.13"

scalaVersion := "2.11.12"
sparkComponents += "sql"
sparkVersion := "2.4.5"

libraryDependencies += "com.databricks" %% "dbutils-api" % "0.0.4"
libraryDependencies += "com.github.salva" %% "scala-glob" % "0.0.3"

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

assemblyExcludedJars in assembly := {
  val exclusions = Seq("sourcecode_", "scala-library-",  "dbutils-", "cats-")
  (fullClasspath in assembly)
    .value
    .filter(dep => exclusions.exists(ex => dep.data.getName.startsWith(ex)))
}

publishMavenStyle := true
//sonatypeProjectHosting := Some(GitHubHosting("salva", "spark-hugefs", "sfandino@yahoo.com"))
