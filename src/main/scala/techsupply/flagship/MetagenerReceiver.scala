package techsupply.flagship

import akka.actor.{Props, Actor, ActorRef}
import com.datastax.spark.connector.cql.CassandraConnector
import dispatch.url
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.receiver.{ActorHelper, Receiver}
import org.apache.spark.Logging

import org.json4s._
import org.json4s.jackson.JsonMethods._
import techsupply.flagship.MetagenerReceiverActor.FetchLatest

import scala.collection.immutable.{Queue, HashSet}
import scala.concurrent.duration.Duration
import scala.util.{Random, Failure, Try, Success}

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._


trait MetagenerCapable extends CassandraCapable {

  val metagenerTestUrl = "http://10.200.21.236:8080/bulksample/retail/retail.item_scans/10"

  def connectToMetagener(DSE_HOST:String): (DStream[MetagenerData], StreamingContext, CassandraConnector) = {
    val context = connect(DSE_HOST)
    //val metagenerStreamRDD = context.streamingContext.receiverStream[MetagenerData](new MetagenerReceiverActor(metagenerTestUrl, StorageLevel.MEMORY_ONLY_SER))
    val metagenerStreamRDD = context.streamingContext.actorStream[MetagenerData](MetagenerReceiverActor.props(metagenerTestUrl,
          StorageLevel.MEMORY_ONLY_SER), "MetagenerReceiverActor")

    (metagenerStreamRDD, context.streamingContext, context.connector)
  }
}


object MetagenerReceiverActor {

  def props(metagenerUrl: String, storageLevel: StorageLevel): Props =
    Props(new MetagenerReceiverActor(metagenerUrl, storageLevel))

  case object FetchLatest

}

class MetagenerReceiverActor(metagenerUrl: String, storageLevel: StorageLevel)
  extends Actor with ActorHelper with Logging {

  val metagenerTick = context.system.scheduler.schedule(Duration.Zero, 1000.millis, self, FetchLatest)

  def receive = {
    case FetchLatest => fetchMetagenerData()
  }

  def fetchMetagenerData() {
    try {
      import dispatch._

      logInfo(s"connectiong to metagener url $metagenerUrl")
      dispatch.Http(url(metagenerUrl) OK as.String) onComplete {
        case Success(jsonContent) => {
          println(s"jsonContent: $jsonContent)")
          parseJson(jsonContent)
        }
        case Failure(exception) => println(exception)
      }
    } catch {
      case e: Exception => logInfo("Error starting MetagenerReceiver stream", e)
    }
  }


  private def parseJson(jsonStr: String): Unit = {
    implicit lazy val formats = DefaultFormats

    try {
      val json = parse(jsonStr)
      println(s"json = ${json.toString.length}")
      val md = json.extract[MetagenerData]
      store(md)
      //println(s"md = $md")

    } catch {
      case e: MappingException => logInfo("Unable to map JSON message to MetagenerData object:" + e.msg)
      case e: Exception => logInfo("Unable to map JSON message to MetagenerData object")
    }
  }
}