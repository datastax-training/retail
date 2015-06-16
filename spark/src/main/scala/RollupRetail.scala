import com.datastax.spark.connector._
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
import java.util.UUID
import org.joda.time.DateTime

import scala.math.BigDecimal.RoundingMode

case class Store(
                  store_id: Int,
                  address: String,
                  address_2: String,
                  address_3: String,
                  city: String,
                  state: String,
                  zip: Long,
                  size_in_sf: Int)

object RollupRetail {

  def main(args: Array[String]) {

//    Create Spark Context
    val conf = new SparkConf(true).setAppName("RollupRetail")

// We set master on the command line for flexibility
    val sc = new SparkContext(conf)

    // Create some general RDDs

    val stores = sc.cassandraTable("retail","stores").select("store_id","address",
      "address_2","address_3","city","state","zip","size_in_sf"
    ).as(Store)

    val receipts = sc.cassandraTable("retail","receipts_by_store_date")

    val store_state = stores.map(s => (s.store_id, s.state))

    // Compute Sales by State

    val total_receipts_by_store =
      receipts.map(r => (r.getInt("store_id"), r.getDecimal("receipt_total").setScale(2,RoundingMode.HALF_EVEN) )  )
        .reduceByKey(_+_)   // Add up by store

    total_receipts_by_store.join(store_state)                                 //  (store, (total, state))
      .map{case (store,(receipts_total, state)) => (state, receipts_total)}   // (state, total)
      .reduceByKey(_+_)                                                       // (state, total) summed by state
      .map{ case(state, receipts_total) => ("dummy", state, "US-" + state, receipts_total)} // (state, US-state, total)
      .saveToCassandra("retail","sales_by_state",SomeColumns("dummy","state","region","receipts_total"))

    // Compute Sales by date

    val receipts_by_date = receipts.map(r => (r.getDate("receipt_date"), r.getDecimal("receipt_total")))

    receipts_by_date.reduceByKey(_+_)
      .map{ case (receipt_date,receipt_total) => ("dummy",receipt_date,receipt_total)}
      .saveToCassandra("retail","sales_by_date",SomeColumns("dummy","sales_date","receipts_total"))
  }
}
