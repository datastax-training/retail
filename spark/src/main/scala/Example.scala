import com.datastax.spark.connector._
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
import java.util.UUID
import org.joda.time.DateTime

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

object Example {

  def main(args: Array[String]) {

    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")

    val sc = new SparkContext("spark://127.0.0.1:7077", "test", conf)

    val stores = sc.cassandraTable("retail","stores").select("store_id","address",
      "address_2","address_3","city","state","zip","size_in_sf"
    ).as(Store)

    val receipts = sc.cassandraTable("retail","receipts_by_store_date")

    val store_state = stores.map(s => (s.store_id, s.state))
    val total_receipts_by_store = receipts.map(r => (r.getInt("store_id"), r.getDecimal("receipt_total") )  ).reduceByKey(_+_)

    total_receipts_by_store.join(store_state).map(r => (r._2._2, r._2._1)).reduceByKey(_+_).saveToCassandra("retail","sales_by_state")

    val receipts_by_date = receipts.map(r => (r.getDate("receipt_date"), r.getDecimal("receipt_total")))

    receipts_by_date.reduceByKey(_+_).saveToCassandra("retail","sales_by_date")


  }
}
