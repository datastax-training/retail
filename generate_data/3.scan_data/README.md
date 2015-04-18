## Extract data from Cassandra

The first two scripts show how to cycle through large amounts of data and
manually write to a file. This file will be used by metagener to generate the
required data.

## Start metagener as a web service

The third script prepares and starts the metagener tools as a web service.
It filters out the entity ids which are needed to use generated data
with pre-generated data. It then starts the web service with a config
descriptor that preloads the data generator recipes from retail.metagener.
Data samples can be seen using:

    curl http://localhost:8080/sample/retail/retail.stores

## Read from a REST api into Cassandra

The last two scripts read data from the metagener REST API and store the data
into Cassandra.
