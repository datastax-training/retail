package techsupply.flagship

import akka.actor.ActorSystem
import dispatch._
import org.apache.spark.SparkContext
import org.scalatra._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

// JSON-related libraries

import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra

import org.scalatra.json._
import com.datastax.spark.connector._
class FutureController(system: ActorSystem, DSE_HOST:String) extends ScalatraServlet with FutureSupport with CassandraCapable {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  //To serialize fractional numbers as BigDecimal instead of Double, use DefaultFormats.withBigDecimal:
  protected implicit val jsonFormats: Formats = DefaultFormats.withBigDecimal

  protected implicit def executor: ExecutionContext = system.dispatcher

 // val DSE_HOST = system.settings.config.getString("DSE_HOST")

  get("/") {

    val products: Array[Product] = callSparkJob()

    products foreach println
    println(s"products size: ${products.size}")
    products foreach println


    <html>
      <body>
        <h1>Hello, world!</h1>
        Say
        <a href="hello-scalate">hello to Scalate</a>
        .
        {products map (p => p.toString)}
      </body>
    </html>


  }

  get("/sd") {
    new AsyncResult {
      val is = HttpClient.retrievePage()
    }
  }

  def callSparkJob() = {
    val sparkConf = createSparkConf(DSE_HOST)

    val sc = new SparkContext(sparkConf)
    val genericRDD = sc.cassandraTable[Product]("retail", "products")
    val products: Array[Product] = genericRDD.take(20)
    sc.stop()

    products
  }

}

object HttpClient {

  def retrievePage()(implicit ctx: ExecutionContext): Future[String] = {
    val prom = Promise[String]()
    dispatch.Http(url("http://slashdot.org/") OK as.String) onComplete {
      case Success(content) => prom.complete(Try(content))
      case Failure(exception) => println(exception)
    }
    prom.future
  }
}
