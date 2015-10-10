import AssemblyKeys._

name := "spark-streaming-retail"

version := "1.1"

val sparkVersion = "1.4.1"
val sparkCassandraVersion = "1.4.0"

libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % sparkCassandraVersion % "provided"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided"

libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion % "provided"

// We get some duplicates here, so don't include some of the libraries
libraryDependencies += "org.apache.activemq" % "activemq-core" %
  "5.7.0" exclude("org.springframework","spring-aop") exclude("org.springframework","spring-beans") exclude("org.springframework","spring-context") exclude ("org.apache.geronimo.specs","geronimo-jms_1.1_spec")

libraryDependencies += "javax.jms" % "jms-api" % "1.1-rev-1"

resolvers += Resolver.sonatypeRepo("public")

resolvers += Resolver.url("java",url("https://repository.jboss.org/nexus/content/groups/public"))

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblySettings


