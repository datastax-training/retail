package retail.controller

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}


object RollupRetailDataFrame {

  def main(args: Array[String]) {

//    Create Spark Context
    val conf = new SparkConf(true).setAppName("RollupRetailHiveQL")

// We set master on the command line for flexibility

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)


    // Nice handy dandy function.  It picks up the current value of SQLContext at execution
    // so it's breaks encapsulation

    def cassandra_df (ks:String, table:String) =  sqlContext.read.format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace"-> ks, "table" -> table))
      .load()



    val receipts_by_store_date_df = cassandra_df("retail","receipts_by_store_date")
    val stores_df = cassandra_df("retail","stores")

    // Create Dataframe to get sales by state

    val sales_by_state_df = receipts_by_store_date_df
      .join(stores_df, stores_df("store_id") === receipts_by_store_date_df("store_id"))
      .groupBy(stores_df("state"))
      .sum("receipt_total")
      .select("state", "US-")

    val sales_by_state_sql = """select
        'dummy' AS dummy,
         s.state AS state,
         CONCAT('US-', s.state) AS region,
         ROUND(SUM(r.receipt_total),2) AS receipts_total
       FROM hc_receipts_by_store_date r
       JOIN hc_stores s ON r.store_id = s.store_id
       GROUP BY s.state"""

    val sales_by_state_df = hc.sql(sales_by_state_sql)   // Create a dataframe from statement

    sales_by_state_df.write                         // Save the dataframe.
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> "retail",
                  "table" -> "sales_by_state"))
      .save()


    // Compute Sales by date

    val sales_by_date_df = hc.sql("""select
        'dummy' AS dummy,
         receipt_date as sales_date,
         SUM(receipt_total) as receipts_total
       FROM hc_receipts_by_store_date
       GROUP BY receipt_date""")   // Create a dataframe from statement

    sales_by_date_df.write                         // Save the dataframe.
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> "retail",
      "table" -> "sales_by_date"))
      .save()

  }
}

