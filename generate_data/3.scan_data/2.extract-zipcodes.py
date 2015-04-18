#!/usr/bin/env python

from flask import Flask

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

app = Flask(__name__)
app.config.from_pyfile('/cornerstone/web/datastax/cornerstone-python/Cornerstone/application.cfg')
ip_addresses = app.config['DSE_CLUSTER'].split(',')

cluster = Cluster(ip_addresses)
session = cluster.connect()
session.row_factory = ordered_dict_factory

response = session.execute('SELECT zipcode FROM retail.zipcodes')

with open('/cache/zipcodes.txt', 'w') as f:
    for row in response:
        f.write('%s\n' % row['zipcode'])
