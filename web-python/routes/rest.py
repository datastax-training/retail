import decimal
import datetime
from json import dumps
import uuid
from cassandra.util import OrderedMap
from helpers import cassandra_helper
from flask import Blueprint, request

rest_api = Blueprint('rest_api', __name__)

timeslice_query = None
simple_queries = {}


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
        return obj.strftime("Date(%Y,%m,%d,%H,%M,%S)")
    elif isinstance(obj, OrderedMap):
        return str(obj)
    raise TypeError

#
# Helper function to convert a column name
# to a beautiful label. Replace "_" with a
# blank, and capitalize each word.
#

def column_name_to_label (column_name):
    return column_name.replace('_', ' ').title()


#
# Simple type mapper to return the google type given a python type
#

def get_google_type(cassandra_value):
    cassandra_type = type(cassandra_value)
    if cassandra_type in [int, float, long, decimal.Decimal]:
        return 'number'
    elif cassandra_type == bool:
        return 'boolean'
    elif cassandra_type == datetime.datetime:
        return 'datetime'
    else:
        return 'string'

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
        statement = "SELECT timewindow, quantities FROM real_time_analytics" \
                    " WHERE series = ?" \
                    " AND timewindow >= ?" \
                    " AND   timewindow <= ?" \
                    " ORDER BY timewindow DESC LIMIT 60"

        timeslice_query = cassandra_helper.session.prepare(statement)

    results = cassandra_helper.session.execute(timeslice_query, [series, start_time, end_time])

    # Build a result table using the gviz_api.
    # We need to see what keys are in the map and treat them as colums

    description = [{'id':'timewindow','label':'Window','type':'datetime'}]
    if results:
        # extract the map of product quantities

        #Coalesce all of the product maps to create a set of names
        products_names = set()
        for row in results:
            products_names |= set(row['quantities'].keys())

        # Convert the map column to look like a series of regular columns to google
        # Create the schema [ ('timewindow', 'datetime'), ('some product', 'number'), ... ]
        description += [{'id': product,'label':column_name_to_label(product),'type': 'number'} for product in products_names]
        data = [ [row['timewindow']] + [row['quantities'].get(item_name) for item_name in products_names] for row in results]

        # sort the data by timewindow
        data.sort(key=lambda row: row[0])

    else:
        # create an empty (yet valid) one
        description += [{'id':'No Products', 'label':'No Products','type':'number'}]
        data = []

    thejson = dumps([description] + data, default=fix_json_format)
    return thejson

#
# This API returns data from the real_time_analytics table
# The table is organized as (series, timewindow, quantities map<text, int>)
# The quantities field allows each row to collect the quantities for several different
# products
#
# URL Format: /simplequery
# Parameters:
#    q         - The CQL query
#    order_col - column to sort by (after fetching) with optional desc
#

@rest_api.route('/simplequery')
def simplequery():

    global simple_queries
    statement = request.args.get('q')
    order_col = request.args.get('order_col')

    if not statement:
        # todo - log something.
        return ""

    if not statement in simple_queries:
        simple_queries[statement] = cassandra_helper.session.prepare(statement)

    results = cassandra_helper.session.execute(simple_queries[statement])

    # extract column names from the first row
    if results:
        first_row = results[0]
    else:
        first_row = {}

    # make a column header
    column_names = [column_name for column_name in first_row]
    description = [{'id':column_name, 'label':column_name_to_label(column_name), 'type': get_google_type(value)} for column_name, value in first_row.iteritems()]

    # Turn the whole thing into an array
    data = [row.values() for row in results]

    # sort it if an order column was specified
    if order_col:
        order_col_split = order_col.split(' ')
        reverse = len(order_col_split) > 1 and order_col_split[1].lower() == 'desc'
        posn = column_names.index(order_col_split[0])
        data.sort(key=lambda row: row[posn], reverse=reverse )

    # stick the description row up front, and dump it as json
    return dumps([description] + data, default=fix_json_format)