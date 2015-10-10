import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}


object RollupRetailHiveQL {

  def main(args: Array[String]) {

//    Create Spark Context
    val conf = new SparkConf(true).setAppName("RollupRetailHiveQL")

// We set master on the command line for flexibility

    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)

    // WORKAROUND as SQLContext in DSE 4.8 uses the wrong input format by default
    // So we register the tables separately.

    hc.sql("""CREATE TEMPORARY TABLE hc_stores
        USING org.apache.spark.sql.cassandra
        OPTIONS (
          table "stores",
          keyspace "retail",
          pushdown "true"
        )""")

    hc.sql("""CREATE TEMPORARY TABLE hc_receipts_by_store_date
        USING org.apache.spark.sql.cassandra
        OPTIONS (
          table "receipts_by_store_date",
          keyspace "retail",
          pushdown "true"
        )""")

    // Create SQL to get sales by state

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

