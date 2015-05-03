from collections import OrderedDict
import datetime
import decimal
import gviz_api


from flask import Blueprint, request
from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

rest_api = Blueprint('rest_api', __name__)

session = None
timeslice_query = None
simple_queries = {}


def init_cassandra(ip_addresses):
    """
    Initialize Cassandra connections
    :param ip_addresses: ip addresses of Cassandra nodes
    :return:
    """
    global session

    cluster = Cluster(ip_addresses)
    session = cluster.connect()
    session.row_factory = ordered_dict_factory

def init_solr(url_base):
    global solr_url_base

    solr_url_base = url_base

#
# Simple type mapper to return the google type given a python type
#

def get_google_type(cassandra_type):
    if type(cassandra_type) in [int, float, long, decimal.Decimal]:
        key_type = 'number'
    elif type(cassandra_type) == bool:
        key_type = 'boolean'
    elif type(cassandra_type) == datetime.date:
        key_type = 'date'
    else:
        key_type = 'string'
    return key_type

#
# This API returns data from the real_time_analytics table
# The table is organized as (series, timewindow, quantities map<text, int>)
# The quantities field allows each row to collect the quantities for several different
# products
#
# URL Format: /realtime/<series>
#    series - the series name
# Parameters:
#    minutes - The number of minutes in a time slice (default 5)
#


@rest_api.route('/realtime/<series>')
def timeslice(series=None):

    global timeslice_query

    minutes = int(request.args.get('minutes', 5))
    end_time = datetime.datetime.utcnow()
    start_time = end_time - datetime.timedelta(minutes=minutes)

    if not timeslice_query:
        statement = "SELECT timewindow, quantities FROM retail.real_time_analytics" \
                    " WHERE series = ?" \
                    " AND timewindow >= ?" \
                    " AND   timewindow <= ?" \
                    " ORDER BY timewindow DESC LIMIT 60"

        timeslice_query = session.prepare(statement)

    results = session.execute(timeslice_query, [series, start_time, end_time])

    # Build a result table using the gviz_api.
    # We need to see what keys are in the map and treat them as colums
    if results:
        # extract the map of product quantities
        products_map = results[0]['quantities']

        # We chose to represent data as a list of tuples
        # Convert the map column to look like a series of regular columns to google
        # Create the schema [ ('timewindow', 'datetime'), ('some product', 'number'), ... ]
        description = [('timewindow', 'datetime')] + map(lambda product: (product, 'number'), products_map.keys())

        data_table = [ [row['timewindow']] + [row['quantities'].get(item_name) for item_name in products_map] for row in results]
        google_table = gviz_api.DataTable(description)
        google_table.LoadData(data_table)
    else:
        # create an empty (yet valid) one
        description = [('timewindow', 'datetime'), ('No Products', 'number')]
        google_table = gviz_api.DataTable(description)

    return google_table.ToJSon(order_by="timewindow")


#
# This API returns data from the real_time_analytics table
# The table is organized as (series, timewindow, quantities map<text, int>)
# The quantities field allows each row to collect the quantities for several different
# products
#
# URL Format: /simplequery
# Parameters:
#    q         - The CQL query
#    parms     - comma separated list of bind values
#    order_col - column to sort by (after fetching)
#

@rest_api.route('/simplequery')
def simplequery():

    global simple_queries
    statement = request.args.get('q')
    parms_str = request.args.get('parms')
    order_col = request.args.get('order_col')

    if not statement:
        # todo - log something.
        return ""

    if not statement in simple_queries:
        simple_queries[statement] = session.prepare(statement)

    results = session.execute(simple_queries[statement])
    description = OrderedDict()

    # extract types
    first_row = results[0]

    # We chose to represent data as a Dict because that's how we're getting back rows
    # for each column in the first row, add it to the description
    i=0
    for column, value in first_row.iteritems():
        description["%02d" % i ] = (get_google_type(value), column)
        i += 1

    google_table = gviz_api.DataTable(description)
    google_table.LoadData(results)

    return google_table.ToJSon(order_by=order_col)
