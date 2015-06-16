
name := "spark-retail"

version := "1.0"

val sparkVersion = "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % sparkVersion % "provided"

libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % sparkVersion % "provided"

resolvers += Resolver.sonatypeRepo("public")

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblySettings


