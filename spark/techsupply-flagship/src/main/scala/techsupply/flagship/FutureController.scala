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

class FutureController(system: ActorSystem) extends ScalatraServlet with FutureSupport {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  //To serialize fractional numbers as BigDecimal instead of Double, use DefaultFormats.withBigDecimal:
  protected implicit val jsonFormats: Formats = DefaultFormats.withBigDecimal

  protected implicit def executor: ExecutionContext = system.dispatcher

  get("/") {

    val products: Array[Product] = SparkTest.callSparkJob()

    products foreach println
    println(s"products size: ${products.size}")
    products foreach println


    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
        {products map (p => p.toString) }
      </body>
    </html>


  }

  get("/sd") {
    new AsyncResult {
      val is = HttpClient.retrievePage()
    }
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
