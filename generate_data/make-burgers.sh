#!/bin/bash
#
# Script to populate the schema and start metagener
#
#cleanup tables
mkdir /home/student/logs
mkdir /mnt/disk1/spark_streaming/
echo cleaning up cassandra tables
cqlsh -f cleanup-burgers.cql

# populate zip codes
#
cd 1.seed_zipcode_data/

echo populating zip codes
python ./1.zipcodes-to-cassandra.py
sleep 5
cd -
cd 3.scan_data/ 
#Get list of products and zipcodes for restaurants
echo creating product and zipcode files for metagener
python ./1.extract-ids.py
sleep 5
python ./2.extract-zipcodes.py
sleep 5

# Start metagener to generate transactions
PATH=/home/student/jre1.8.0_45/bin:$PATH
echo starting metagener
./3.start-metagener.sh >/home/student/logs/metagener.log &
echo $! >/home/student/metagener.pid
sleep 5
python ./4.metagener-to-cassandra-stores-employees.py >/home/student/logs/employees.log &
echo $! >/home/student/employees.pid
sleep 5
nc -l 5005 &
sleep 5
python ./5.metagener-to-cassandra-scan-items.py >/home/student/logs/items.log &
echo $! >/home/student/items.pid

cd -
