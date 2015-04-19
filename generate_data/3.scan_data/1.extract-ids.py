#!/usr/bin/env python

# Writes all product ids to /cache/product_ids.txt

from flask import Flask

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

app = Flask(__name__)
app.config.from_pyfile('../../web/application.cfg')
ip_addresses = app.config['DSE_CLUSTER'].split(',')

cluster = Cluster(ip_addresses)
session = cluster.connect()
session.row_factory = ordered_dict_factory

response = session.execute('SELECT product_id FROM retail.products')

with open('product_ids.txt', 'w') as f:
    for row in response:
        f.write('%s\n' % row['product_id'])
