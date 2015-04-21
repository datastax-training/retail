package techsupply.flagship

import com.datastax.spark.connector.cql.CassandraConnector
import dispatch.url
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.Logging

import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Try, Success}

import scala.concurrent.ExecutionContext.Implicits.global

case class MetagenerData(minSampleId:Int, maxSampleId:Int, sampleValues:List[SV])
case class SV(sampleId:Int, fieldValues:SDS)
case class SDS(scan_duration_seconds:BigDecimal, scan_qty:String, product_id:String, item_discount:BigDecimal)

trait MetagenerCapable extends CassandraCapable {

  val metagenerTestUrl = "http://localhost:8080/bulksample/retail/retail.item_scans/1000"

  def connectToMetagener(): ( DStream[MetagenerData], StreamingContext, CassandraConnector) = {
    val context = connect()
    //val rdd: DStream[String] = context.streamingContext.socketTextStream(hostName,port)

    val metagenerStreamRDD = context.streamingContext.receiverStream[MetagenerData](new MetagenerReceiver(metagenerTestUrl, StorageLevel.MEMORY_ONLY_SER))

    (metagenerStreamRDD, context.streamingContext, context.connector)
  }
}

class MetagenerReceiver(metagenerUrl: String, storageLevel: StorageLevel)
    extends Receiver[MetagenerData](storageLevel) with Logging
{

  def onStart() {
    try{
      logInfo("Connecting to MetagenerReceiver: " + url)
     // val newWebSocket = WebSocket().open(url).onTextMessage({ msg: String => parseJson(msg) })
     // setWebSocket(newWebSocket)
     // logInfo("Connected to: WebSocket" + url)

      import dispatch._
      while (true) {
        println(s"connectiong to metagener url $metagenerUrl")
        dispatch.Http(url(metagenerUrl) OK as.String) onComplete {
          case Success(jsonContent) => {
            //prom.complete(Try(content))
            println(s"jsonContent: $jsonContent)")
            parseJson(jsonContent)
          }
          case Failure(exception) => println(exception)
        }
      }
    } catch {
      case e: Exception => restart("Error starting MetagenerReceiver stream", e)
    }
  }

  def onStop() {
    //MetagenerReceiver(null)
    logInfo("WebSocket receiver stopped")
  }



  private def parseJson(jsonStr: String): Unit =
  {
    implicit lazy val formats = DefaultFormats

    try {
      val json = parse(jsonStr)
      println(s"json = ${json.toString.length}")
      val md = json.extract[MetagenerData]
      store(md)
      //println(s"md = $md")

    } catch {
      case e: MappingException => println("Unable to map JSON message to MetagenerData object:" + e.msg)
      case e: Exception => println("Unable to map JSON message to MetagenerData object")
    }
  }
}