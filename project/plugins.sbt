resolvers += "Spark Packages repo" at "https://dl.bintray.com/spark-packages/maven/"

addSbtPlugin("org.spark-packages" %% "sbt-spark-package" % "0.2.6")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.3.1")
