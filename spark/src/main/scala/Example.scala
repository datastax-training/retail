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

object Example {

  def main(args: Array[String]) {

    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")

    val sc = new SparkContext("spark://127.0.0.1:7077", "test", conf)

    val sales = sc.cassandraTable("retail","registers").as(Register)

    sales.map( sale => (sale.product, (1, sale.price )) )
      .reduceByKey( (x,y) => (x._1 + y._1, x._2 + y._2)  )
      .map( x=> (x._1, x._2._1, x._2._2) )
      .saveToCassandra("retail","sales_by_product",
        SomeColumns("product","quantity","amount"))
    }
}
