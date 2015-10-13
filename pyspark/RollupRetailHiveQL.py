from pyspark import SparkContext
from pyspark import SparkConf
from pyspark.sql import HiveContext

# Create SQL Context

conf = SparkConf().setAppName("RollupRetail")
sc = SparkContext(conf=conf)

hc = HiveContext(sc)

# register temporary tables so we use the correct driver

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


# 1. join receipts_by_store_date to store
# 2. group by state
# 3. sum by receipt_total
# 4. do a select to add the dummy column, rename columns, compute the region and round the totals

sales_by_state_sql = """select
        'dummy' AS dummy,
         s.state AS state,
         CONCAT('US-', s.state) AS region,
         ROUND(SUM(r.receipt_total),2) AS receipts_total
       FROM hc_receipts_by_store_date r
       JOIN hc_stores s ON r.store_id = s.store_id
       GROUP BY s.state"""

sales_by_state_df = hc.sql(sales_by_state_sql)

# Save it

sales_by_state_df\
    .write\
    .format("org.apache.spark.sql.cassandra") \
    .options(keyspace = "retail", table = "sales_by_state") \
    .mode('overwrite') \
    .save()

# Compute Sales by date

sales_by_date_df = hc.sql("""select
        'dummy' AS dummy,
         receipt_date as sales_date,
         ROUND(SUM(receipt_total),2) as receipts_total
       FROM hc_receipts_by_store_date
       GROUP BY receipt_date""")

sales_by_date_df.write\
    .format("org.apache.spark.sql.cassandra")\
    .options(keyspace = "retail", table = "sales_by_date")\
    .mode('overwrite')\
    .save()



