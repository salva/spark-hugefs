## spark-hugefs

Spark is commonly used with remote file systems that are relatively
slow, specially for operations related to walking the file structure.

This package provides methods that allow one to retrieve the file
structure fast and efficiently, taking advantage of Spark
parallelization infrastructure.

## Example

```scala
import com.github.salva.spark.hugefs.Walker

val w = Walker(spark.sparkContext)
val df = w.walk("/dbfs/mnt/fs1", Good.isFile.glob("**/images/*.jpg"))
println("%s JPEG images found in directory", df.count)
```

## Documentation

This is yet a work in progress. Documentation is comming!

## Latest version

The latest version of `spark-hugefs` is `0.1`, soon available from [Maven Central](http://repo1.maven.org/maven2/com/github/salva/spark/hugefs).

```scala
libraryDependencies += "com.github.salva.spark-hugefs" %% "spark-hugefs" % "0.1"
```

## Copying

Copyright 2020 Salvador Fandiño (sfandino@yahoo.com)


Licensed under the Apache License, Version 2.0 (the "License");
you may not use the files in this package except in compliance with
the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

