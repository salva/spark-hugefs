# spark-hugefs

Spark is commonly used with remote file systems that are relatively
slow, specially for operations related to walking the file structure.

This package provides methods that allow one to retrieve the file
structure fast and efficiently, taking advantage of Spark
parallelization infrastructure.

## Example

```scala
import com.github.salva.spark.hugefs._

val df = Walker.walk("/dbfs/mnt/fs1", Good.isFile.glob("**/images/*.jpg"), spark)
println("%s JPEG images found in directory", df.count)
```

## Documentation

This package uses three main abstractions:

* Walkers: The classes that can walk the file system and retrieve its structure
using different strategies.

Currently, two walker classes are available:

  * Walker: walks the file system using the Spark engine, distributing the
  process over all the available nodes.
  
  * LocalWalker: walks the file system using only the local thread.

* Restrictions: A family of classes that allow one to prune the search space
limiting the file system walk to those directories or files that comply with
the declared restrictions.

For instance, it is possible to pick files by its type (regular file,
directory, symbolic link), extension, filename, globing patterns, etc.   

* File systems: The classes that implement the low level access to the
filesystem. Currently, backends are available for the Databricks file
system (DBFS), and the Java native one.

Probably only users interested in adding support for additional file systems
should be concerned with these classes. Needless to say that contributions
in this area are very welcome!

## Latest version

The latest version of `spark-hugefs` is `0.1`, soon to be available
from [Maven Central](http://repo1.maven.org/maven2/com/github/salva/spark/hugefs).

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

