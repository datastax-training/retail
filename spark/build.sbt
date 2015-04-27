
name := "spark-demo"

version := "1.0"

libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "1.1.0" % "provided"

resolvers += Resolver.sonatypeRepo("public")

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblySettings


