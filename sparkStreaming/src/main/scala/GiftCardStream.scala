import java.util.UUID
import org.joda.time.DateTime

import org.apache.spark._
import org.apache.spark.streaming._
//import org.apache.spark.streaming.StreamingContext._
import com.datastax.spark.connector.streaming._


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

object GiftCardStream {

  def main(args: Array[String]) {

//    Create Spark Context


    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")

    val ssc = new StreamingContext(conf, Seconds(1))

    val lines = ssc.socketTextStream("localhost", 5002)

    lines.saveToCassandra("retail","sales_by_date")


  }
}
