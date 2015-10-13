import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.BigDecimal.RoundingMode


object RollupRetailDataframe {

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

    // Create some handy UDF's

    val concat = udf((s1:String, s2:String) => s1 + s2)
    val round = udf((f1:java.math.BigDecimal, places:Int) => f1.setScale(places,RoundingMode.HALF_EVEN))

    // Create Dataframe to get sales by state

    val sales_by_state_df = receipts_by_store_date_df
      .join(stores_df, stores_df("store_id") === receipts_by_store_date_df("store_id"))
      .groupBy(stores_df("state"))
      .sum("receipt_total")
      .select(lit("dummy") alias "dummy", col("state"), concat( lit("US-"), col("state")) alias "region", round(col("SUM(receipt_total)"), lit(2)) alias ("receipts_total"))

    sales_by_state_df.write                         // Save the dataframe.
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> "retail",
                  "table" -> "sales_by_state"))
      .save()


    // Compute Sales by date

    val sales_by_date_df = receipts_by_store_date_df
     .groupBy("receipt_date")
      .sum("receipt_total")
      .select(lit("dummy") alias "dummy", col("receipt_date") as "sales_date", round(col("SUM(receipt_total)"),lit(2)) alias "receipts_total")

    sales_by_date_df.write                         // Save the dataframe.
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> "retail",
      "table" -> "sales_by_date"))
      .save()

  }
}

