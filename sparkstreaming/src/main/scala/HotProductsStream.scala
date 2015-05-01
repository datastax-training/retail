import java.util.UUID
import org.joda.time.DateTime
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.SomeColumns


case class Register(store_id: Int,
                 register_id: Int,
                 receipt_id: UUID,
                 scan_time: DateTime,
                 brand: String,
                 msrp: BigDecimal,
                 price: BigDecimal,
                 product: String,
                 product_id: String,
                 quantity: BigDecimal,
                 savings: BigDecimal,
                 scan_duration: Int)

case class Store(
                  store_id: Int,
                  address: String,
                  address_2: String,
                  address_3: String,
                  city: String,
                  state: String,
                  zip: Long,
                  size_in_sf: Int)

object HotProductsStream {

  def main(args: Array[String]) {

//    Create Spark Context


    val conf = new SparkConf(true)
         .setAppName("HotProductsStream")

    val ssc = new StreamingContext(conf, Seconds(10))

//    val lines = ssc.socketTextStream("localhost", 5002)
    val lines = ssc.receiverStream(new JMSReceiver("HotProducts","tcp://localhost:61616"))
          lines.map(line => line.split('|'))
            .map(arr => (arr(0), arr(1).toInt))    // create (product, amount)
            .reduceByKeyAndWindow( (acc,sale) => acc + sale , Seconds(10))
            .map{ case (k,v) => (k, new DateTime(),v)}
          .saveToCassandra("retail","hot_products",SomeColumns("product","timewindow","sales"))


//    lines.map(line => line.split('|') ).


    ssc.start()

    ssc.awaitTermination()

  }
}
