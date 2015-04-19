package techsupply.flagship

import org.apache.spark.SparkConf
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import com.datastax.spark.connector._
import org.apache.spark.SparkContext

case class Receipts(cashier_first_name: Int, cashier_id: String, cashier_last_name: String)

case class Product(product_id: String, brand: String, price:BigDecimal, title: String)

object SparkTest {

  def main(args: Array[String]): Unit = {
    callSparkJob()
  }

  def callSparkJob() = {
    val sparkConf = new SparkConf()
      .setAppName("techsupply")
      .set("spark.cassandra.connection.host", "10.0.0.26")
      .setMaster("spark://10.0.0.26:7077")
      .setJars(Array("target/scala-2.10/techsupply-flagship-assembly-0.1.0-SNAPSHOT.jar"))

    val sc = new SparkContext(sparkConf)

    val genericRDD = sc.cassandraTable[Product]("retail", "products")

    val products: Array[Product] = genericRDD.take(20)

    sc.stop

    products
  }
}