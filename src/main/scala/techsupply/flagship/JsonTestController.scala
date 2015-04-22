package techsupply.flagship

import akka.actor.ActorSystem
import dispatch._
import org.apache.spark.SparkContext
import org.scalatra._
import techsupply.flagship.SparkTest._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

import com.datastax.spark.connector._

class JsonTestController(system: ActorSystem, DSE_HOST:String) extends ScalatraServlet
      with JacksonJsonSupport with FutureSupport
      with CassandraCapable
{

  //val DSE_HOST = system.settings.config.getString("DSE_HOST")


  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  //To serialize fractional numbers as BigDecimal instead of Double, use DefaultFormats.withBigDecimal:
  protected implicit val jsonFormats: Formats = DefaultFormats.withBigDecimal

  protected implicit def executor: ExecutionContext = system.dispatcher


  def callSparkJob() = {
    val sparkConf = createSparkConf(DSE_HOST)

    val sc = new SparkContext(sparkConf)
    val genericRDD = sc.cassandraTable[Product]("retail", "products")
    val products: Array[Product] = genericRDD.take(20)
    sc.stop()

    products
  }


  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/") {

    val products: List[Product] = callSparkJob().toList
    println(s"products size: ${products.size}")
    products

  }

}
