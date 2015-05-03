import decimal
import datetime
from json import dumps
import uuid
from cassandra.util import OrderedMap

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
# Helper function to have json.dump format dates correctly
#

def fix_json_format(obj):
    """Default JSON serializer."""

    if isinstance(obj, decimal.Decimal):
        return float(obj)
    elif isinstance(obj, uuid.UUID):
        return str(obj)
    elif isinstance(obj, datetime.datetime):
        return str(obj)
    elif isinstance(obj, OrderedMap):
        return str(obj)
    raise TypeError

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

        # Convert the map column to look like a series of regular columns to google
        # Create the schema [ ('timewindow', 'datetime'), ('some product', 'number'), ... ]
        description = ['timewindow'] + products_map.keys()
        data = [ [row['timewindow']] + [row['quantities'].get(item_name) for item_name in products_map] for row in results]

        # sort the data by timewindow
        data.sort(key=lambda row: row[0])

    else:
        # create an empty (yet valid) one
        description = ['timewindow', 'No Products']
        data = []

    return dumps([description] + data, default=fix_json_format)

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

    # extract column names from the first row
    first_row = results[0]

    # make a column header
    description = [column for column in first_row]

    # Turn the whole thing into an array
    data = [row.values() for row in results]

    # sort it if an order column was specified
    if order_col:
        posn = description.index(order_col)
        data.sort(key=lambda row: row[posn] )

    # stick the description row up front, and dump it as json
    return dumps([description] + data, default=fix_json_format)