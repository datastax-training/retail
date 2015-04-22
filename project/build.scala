import sbtassembly.Plugin.{MergeStrategy, PathList, AssemblyKeys}
import sbt._
import sbt.Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

import AssemblyKeys._

import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin._

object TechSupplyBuild extends Build {
  val Organization = "techsupply"
  val Name = "techsupply-flagship"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.4"
  val ScalatraVersion = "2.3.0"
  val AkkaVersion = "2.2.3"

  val Spark = "1.1.0"
  val SparkCassandra = "1.1.0"

  lazy val project = Project (
    "techsupply-flagship",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ sbtassembly.Plugin.assemblySettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      resolvers += "Akka Repo" at "http://repo.akka.io/repository",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % AkkaVersion % "provided",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",

        "org.apache.spark" % "spark-core_2.10" % Spark % "provided",
        "org.apache.spark" % "spark-streaming_2.10" % Spark ,
        "org.apache.spark" % "spark-streaming-kafka_2.10" % Spark,
        ("com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandra withSources() withJavadoc()).
          exclude("com.esotericsoftware.minlog", "minlog").
          exclude("commons-beanutils","commons-beanutils").
          exclude("org.apache.spark","spark-core"),
        ("com.datastax.spark" %% "spark-cassandra-connector-java" % SparkCassandra withSources() withJavadoc()).
          exclude("org.apache.spark","spark-core"),
        "net.jpountz.lz4" % "lz4" % "1.2.0",
        "org.scalatra" %% "scalatra-json" % "2.2.2",
        "org.json4s"   %% "json4s-jackson" % "3.2.6",

        "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      },
      mergeStrategy in assembly := {
        case PathList("META-INF", "ECLIPSEF.RSA", xs @ _*)         => MergeStrategy.discard
        case PathList("META-INF", "mailcap", xs @ _*)         => MergeStrategy.discard
        case PathList("org", "apache","commons","collections", xs @ _*) => MergeStrategy.first
        case PathList("org", "apache","commons","logging", xs @ _*) => MergeStrategy.first
        case PathList("org", "slf4j","impl", xs @ _*) => MergeStrategy.first
        case PathList("com", "esotericsoftware","minlog", xs @ _*) => MergeStrategy.first
        case PathList("org", "apache","commons","beanutils", xs @ _*) => MergeStrategy.first
        case PathList(ps @ _*) if ps.last == "Driver.properties" => MergeStrategy.discard
        case PathList(ps @ _*) if ps.last == "plugin.properties" => MergeStrategy.discard
        case PathList(ps @ _*) if ps.last == "pom.xml" => MergeStrategy.discard
        case PathList(ps @ _*) if ps.last == "pom.properties" => MergeStrategy.discard
        case x =>
          val oldStrategy = (mergeStrategy in assembly).value
          oldStrategy(x)
      },
      mainClass in assembly := Some("techsupply.flagship.SparkTest"),
      port in container.Configuration := 7071
    )
  )
}
