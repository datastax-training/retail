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

    val lines = ssc.receiverStream(new JMSReceiver("HotProducts","tcp://localhost:61616"))

          // note that we use def here so it gets evaluated in the map
          def current_time = new DateTime()
          val series_name: String = "hotproducts"

          lines.map(line => line.split('|'))       // we have a list of arrays - not too useful
            .map(arr => (arr(0), arr(1).toInt))    // convert to list of tuples (product, amount)
            .reduceByKeyAndWindow( (total_sales,current_sale) => total_sales + current_sale , Seconds(10))  // same thing, but 1 row per product
            .map{ case (product, amount) => Map(product -> amount)}
            .reduce( (current_map, new_element) => current_map ++ new_element )  // Make a map of {product -> amount, ...}
            .map( qty_map => { (series_name, current_time, qty_map)}) // fill in the row keys
            .saveToCassandra("retail","real_time_analytics",SomeColumns("series","timewindow","quantities"))

    ssc.start()

    ssc.awaitTermination()

  }
}
