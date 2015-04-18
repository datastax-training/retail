## Download SNAP data

[Stanford Network Analysis Platform (SNAP)](http://snap.stanford.edu/)
is a general purpose network analysis and graph mining library.
In this project we download Electronics product reviews (1,241,778 reviews)
as well as the product brand info from the
[Amazon reviews dataset](http://snap.stanford.edu/data/web-Amazon.html).

## Parse SNAP data and populate Cassandra

The second script parses the SNAP data into digestible chunks, then uses a
full-pipeline of futures to send the writes into the `retail.products` table.
