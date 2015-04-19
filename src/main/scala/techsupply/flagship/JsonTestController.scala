package techsupply.flagship

import akka.actor.ActorSystem
import dispatch._
import org.scalatra._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class JsonTestController(system: ActorSystem) extends ScalatraServlet with JacksonJsonSupport with FutureSupport {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  //To serialize fractional numbers as BigDecimal instead of Double, use DefaultFormats.withBigDecimal:
  protected implicit val jsonFormats: Formats = DefaultFormats.withBigDecimal

  protected implicit def executor: ExecutionContext = system.dispatcher

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/") {

    val products: List[Product] = SparkTest.callSparkJob().toList
    println(s"products size: ${products.size}")
    products

  }

}
