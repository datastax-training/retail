package techsupply.flagship

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.datastax.spark.connector._

import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkContext._

import com.datastax.spark.connector._
import org.json4s.{MappingException, DefaultFormats}
import org.json4s.jackson.JsonMethods._

import com.datastax.spark.connector._

case class Receipts(cashier_first_name: Int, cashier_id: String, cashier_last_name: String)

case class Product(product_id: String, brand: String, price: BigDecimal, title: String)

object SparkTest extends TextSocketCapable with MetagenerCapable{

  def main(args: Array[String]): Unit = {
    //testMetagenerStream()

    //testJsonParsing()

    testSparkStreamSocket()
  }

  case class MG(minSampleId:Int, maxSampleId:Int, sampleValues:List[SV])
  case class SV(sampleId:Int, fieldValues:SDS)
  case class SDS(scan_duration_seconds:BigDecimal, scan_qty:String, product_id:String, item_discount:BigDecimal)
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
      val md = json.extract[MG]
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

  def testMetagenerStream() = {

    val (metagenerStreamRDD, sparkContext, connector) = connectToMetagener()

    val words = metagenerStreamRDD.map(mg => mg.toString).flatMap(_.split(" "))
    // Count each word in each batch
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)
    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    sparkContext.start()
    sparkContext.awaitTermination()
  }

  case class WordCount(word: String, count: Long)

  def testSparkStreamSocket() = {
    val (socketRdd, sparkContext, connector) = connectToSocket()

    val words = socketRdd.flatMap(_.split(" "))
    // Count each word in each batch
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)
    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

      /**

      CREATE TABLE wordCounts (
        word varchar PRIMARY KEY,
        count int
      );

    **/
    wordCounts.foreachRDD {
      wcRDD =>
        if (wcRDD.count > 0) {
          println(s"wcRDD.count() ${wcRDD.count}  = ${wcRDD.toDebugString}")
          wcRDD.saveToCassandra("retail", "wordcounts", SomeColumns("word", "count"))
        }
    }

    sparkContext.start()
    sparkContext.awaitTermination()
  }
}