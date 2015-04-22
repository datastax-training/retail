package techsupply.flagship

import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import org.apache.spark.{Logging, SparkContext}

import org.apache.spark.streaming.StreamingContext._

import org.json4s.{MappingException, DefaultFormats}
import org.json4s.jackson.JsonMethods._

import com.datastax.spark.connector._
import techsupply.flagship.WebActor.NextMG

case class Receipts(cashier_first_name: Int, cashier_id: String, cashier_last_name: String)

case class Product(product_id: String, brand: String, price: BigDecimal, title: String)


class WebActor extends Actor {
  def receive = {
    case NextMG(mg) => {
      println ("#########-----------------------########")
      println(s"NextMG: $mg")
      println ("#########-----------------------########")
      for (x <- 1 to 5) println
    }
  }
}

object WebActor {
  case class NextMG(mg:MetagenerData)

  def props(): Props =
    Props(new WebActor())

}


object SparkTest extends TextSocketCapable with MetagenerCapable with Logging {

  def main(args: Array[String]): Unit = {
    //testMetagenerStream()
    //testJsonParsing()

    val system = ActorSystem("SPARKTESTSYSTEM")
    val webActorRef = system.actorOf(WebActor.props())
    val numData = system.settings.config.getString("NUM_DATA")
    println(s"numData = $numData")

    testMetagenerStream(webActorRef)
  }

  def testJsonParsing() = {

    val jsonStr = """
    {
      "minSampleId": 16561348,
      "maxSampleId": 16561352,
          "sampleValues": [
                { "sampleId": 16561348,
                  "fieldValues": {
                    "scan_duration_seconds": 0.48807000692069563,
                    "scan_qty": "1",
                    "product_id": "B0000DCTC7",
                    "item_discount": 0.8816160100375953
                  }
                },
                {"sampleId": 16561348,
                    "fieldValues": {
                      "scan_duration_seconds": 0.48807000692069563,
                      "scan_qty": "1",
                      "product_id": "B0000DCTC7",
                      "item_discount": 0.8816160100375953
                    }
                }
          ]
    }
                  """

    println(jsonStr)
    implicit lazy val formats = DefaultFormats

    try {
      val json = parse(jsonStr)
      println(s"json = $json")
      val md = json.extract[MetagenerData]
      println(s"md = $md")

    } catch {
      case e: MappingException => println("Unable to map JSON message to MetagenerData object:" + e.msg)
      case e: Exception => println("Unable to map JSON message to MetagenerData object")
    }

  }

  def callSparkJob() = {
    val sparkConf = createSparkConf()

    val sc = new SparkContext(sparkConf)
    val genericRDD = sc.cassandraTable[Product]("retail", "products")
    val products: Array[Product] = genericRDD.take(20)
    sc.stop()

    products
  }

  def testMetagenerStream(webActorRef:ActorRef) = {
    println(s"webActorRef  = $webActorRef  = ${webActorRef.path}}")
    setStreamingLogLevels()

    val (metagenerStreamRDD, sparkContext, connector) = connectToMetagener()

    metagenerStreamRDD.foreachRDD {
      metaRDD => {
        if (metaRDD.count() > 0) {
         /* metaRDD.foreachPartition { metaPartition =>
              logInfo(s"metaPartition: $metaPartition")
          }*/
          val collect: Array[MetagenerData] = metaRDD.collect()
          val md:MetagenerData = collect(0)
          println(s"#############  foreach rdd ${md.toString}")
          webActorRef ! NextMG(collect(0))
        }
      }
    }
    sparkContext.start()
    sparkContext.awaitTermination()
  }


  import org.apache.log4j.{Level, Logger}

  def setStreamingLogLevels() {
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      // We first log something to initialize Spark's default logging, then we override the
      // logging level.
      logInfo("Setting log level to [INFO] for WordCount Metagener example." +
        " To override add a custom log4j.properties to the classpath.")
      Logger.getRootLogger.setLevel(Level.INFO)
    }
  }

  case class WordCount(word: String, count: Long)

  def testSparkStreamSocket() = {

    setStreamingLogLevels()

    val (socketRdd, sparkContext, connector) = connectToSocket()

    val words = socketRdd.flatMap(_.split(" "))
    // Count each word in each batch
    val pairs = words.filter(word => word.length > 0).map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)
    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    logInfo("RAPIDSTART:### ->")

      /**

      CREATE TABLE wordCounts (
        word varchar PRIMARY KEY,
        count int
      );

    **/
    wordCounts.foreachRDD {
      wcRDD =>
        if (wcRDD.count > 0) {
          logInfo(s"wcRDD.count() ${wcRDD.count}  = ${wcRDD.toDebugString}")
          wcRDD.foreach{
            case wcr => logInfo(s"STUFFHERE:### -> nxt: $wcr")
          }
          wcRDD.saveToCassandra("retail", "wordcounts", SomeColumns("word", "count"))
        }
    }

    sparkContext.start()
    sparkContext.awaitTermination()
  }
}