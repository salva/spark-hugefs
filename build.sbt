name := "spark-hugefs"

version := "0.1"

scalaVersion := "2.11.12"

sparkComponents += "sql"

//sparkVersion := "3.0.0-preview2"
sparkVersion := "2.4.5"

libraryDependencies += "com.databricks" %% "dbutils-api" % "0.0.4"

githubOwner := "salva"
githubRepository := "spark-hugefs"
