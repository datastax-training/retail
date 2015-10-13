from pyspark import SparkContext
from pyspark import SparkConf
from pyspark.sql import SQLContext
from pyspark.sql import functions as F
from pyspark.sql.functions import udf
import pyspark.sql.types as T

# Create SQL Context

conf = SparkConf().setAppName("RollupRetail")
sc = SparkContext(conf=conf)

sqlContext = SQLContext(sc)

# Create Dataframes on Cassandra tables

stores_df = sqlContext.read\
    .format("org.apache.spark.sql.cassandra")\
    .options(keyspace="retail", table="stores")\
    .load()

receipts_by_store_date_df = sqlContext.read\
    .format("org.apache.spark.sql.cassandra")\
    .options(keyspace="retail", table="receipts_by_store_date")\
    .load()


# Create UDFs - These may not perform well, but are run only on the final results

concat = udf(lambda s1, s2: s1 + s2, T.StringType())


# 1. join receipts_by_store_date to store
# 2. group by state
# 3. sum by receipt_total
# 4. do a select to add the dummy column, rename columns, compute the region and round the totals

sales_by_state_df = receipts_by_store_date_df\
    .join(stores_df, stores_df.store_id == receipts_by_store_date_df.store_id )\
    .groupBy("state")\
    .sum("receipt_total")\
    .select(F.lit("dummy").alias("dummy"), "state", concat(F.lit("US-"),"state").alias("region"), F.col("SUM(receipt_total)").cast("Decimal(10,2)").alias("receipts_total"))


# Save it

sales_by_state_df\
    .write\
    .format("org.apache.spark.sql.cassandra") \
    .options(keyspace = "retail", table = "sales_by_state") \
    .mode('overwrite') \
    .save()

# Compute Sales by date

sales_by_date_df = receipts_by_store_date_df\
    .groupBy("receipt_date")\
    .sum("receipt_total")\
    .select(F.lit("dummy").alias("dummy"), F.col("receipt_date").alias("sales_date"), F.col("SUM(receipt_total)").cast("Decimal(10,2)").alias("receipts_total"))

# Save the dataframe
sales_by_date_df.write\
    .format("org.apache.spark.sql.cassandra")\
    .options(keyspace = "retail", table = "sales_by_date") \
    .mode('overwrite') \
    .save()



