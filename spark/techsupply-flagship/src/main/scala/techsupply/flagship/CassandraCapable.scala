/*
 * Copyright 2014 Ryan Svihla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package techsupply.flagship

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Milliseconds, StreamingContext}


trait CassandraCapable {

  val keySpaceName =  "retail"
  val fullTableName =  "streaming_demo"

  def createSparkConf(): SparkConf = {
    new SparkConf()
      .setAppName("techsupply")
      .set("spark.cassandra.connection.host", "127.0.0.1")
      .setMaster("spark://127.0.0.1:7077")
      .setJars(Array("target/scala-2.10/techsupply-flagship-assembly-0.1.0-SNAPSHOT.jar"))
  }

  def connect(): CassandraContext = {
    var conf = createSparkConf()
    val connector = CassandraConnector(conf)
    val ssc = new StreamingContext(conf, Milliseconds(5000))
    new CassandraContext(connector, ssc)
  }
}
