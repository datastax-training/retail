import blist
import datetime
import gviz_api
import uuid

from decimal import Decimal
from flask import Blueprint, jsonify, request
from json import loads, dumps

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory
from cassandra.util import OrderedMap

rest_api = Blueprint('rest_api', __name__)

session = None
p = None


class PreparedStatements:
    """
    Helper class to cache prepared statements
    """

    def __init__(self, session):
        self.prepared_statements = {}
        self.session = session

    def get(self, query):
        if query in self.prepared_statements:
            return self.prepared_statements[query]

        prepared_statement = self.session.prepare(query)
        self.prepared_statements[query] = prepared_statement
        return prepared_statement


def decimal_default(obj):
    """
    Helper class for JSON decoding
    :param obj: JSON key/value
    :return: cleansed JSON key/value
    """
    if isinstance(obj, Decimal):
        return float(obj)
    elif isinstance(obj, uuid.UUID):
        return str(obj)
    elif isinstance(obj, datetime.datetime):
        return str(obj)
    elif isinstance(obj, OrderedMap):
        return str(obj)
    raise TypeError


def init_cassandra(ip_addresses):
    """
    Initialize Cassandra connections
    :param ip_addresses: ip addresses of Cassandra nodes
    :return:
    """
    global session, p

    cluster = Cluster(ip_addresses)
    session = cluster.connect()
    session.row_factory = ordered_dict_factory

    p = PreparedStatements(session)


@rest_api.route('/')
def base():
    f = {'status': 'OK'}
    return jsonify(**f)


@rest_api.route('/paging/<keyspace>/<table>/')
def paging(keyspace=None, table=None):
    """
    Convert REST calls to Cassandra queries
    :param keyspace: query keyspace
    :param table: query table
    :return:
    """

    # GET variables for Cassandra query
    result_size = request.args.get('result_size', 1000, type=int)
    paging_keys = request.args.get('paging_keys', None)
    paging_values = request.args.get('paging_values', None)

    # GET variables for Google Charts parameters
    gcharts_columns = request.args.get('gcharts_columns', None)

    # properly format gCharts columns field
    if gcharts_columns:
        gcharts_columns = gcharts_columns.split(',')

    # GET variables for Google Charts' DataTable parameters
    gcharts_datatable_order_by = request.args.get('gcharts_datatable_order_by',
                                                  None)

    if paging_keys and paging_values:
        # ensure the number of keys == number of values
        if len(paging_keys.split(',')) != len(paging_values.split(',')):
            return jsonify({'error': 'len(paging_keys) != len(paging_values)'})

        # seed query
        query = 'SELECT * FROM %s.%s WHERE ' % (keyspace, table)

        # create paging logic
        paging_query = []
        paging_keys = paging_keys.split(',')
        for key in paging_keys[:-1]:
            paging_query.append('token(%s) = token(?)' % key)
        paging_query.append('token(%s) > token(?)' % paging_keys[-1])
        query += ' AND '.join(paging_query)

        # create PagedResult
        try:
            paging_result = session.execute(p.get(query),
                                            paging_values.split(','))
        except Exception as e:
            return jsonify({'error': e.message})
    else:
        # else, process a simple query
        query = 'SELECT * FROM %s.%s' % (keyspace, table)
        try:
            paging_result = session.execute(p.get(query))
        except Exception as e:
            return jsonify({'error': e.message})

    # collect results from a Result or PagedResult
    results = []
    for result in paging_result:
        results.append(result)

        # stop collecting results once result_size if filled
        if len(results) == result_size:
            break

    # sanitize results for JSON
    results = loads(dumps(results, default=decimal_default))
    f = {'results': results}

    # create gcharts response
    if results:
        # initialize rest json for gcharts
        f['gcharts'] = {}

        # extract reference row
        result = results[0]

        # use requested columns or dynamically create column list
        if gcharts_columns:
            keys = gcharts_columns
        else:
            keys = result.keys()

        # build DataTable column description
        description = {}
        for key in keys:
            formatted_key = key.replace('_', ' ').title()
            if type(result[key]) in [int, float, long]:
                key_type = 'number'
            elif type(result[key]) == bool:
                key_type = 'boolean'
            elif type(result[key]) == datetime.date:
                key_type = 'date'
            else:
                key_type = 'string'

            description[key] = (key_type, formatted_key)

        # use gviz to create DataTable response
        data_table = gviz_api.DataTable(description)
        data_table.LoadData(results)
        f['gcharts'][1.1] = data_table.ToJSon(columns_order=keys,
                                              order_by=gcharts_datatable_order_by)

        # create Charts array
        charts_table = []

        # create headings
        charts_table.append(keys)

        for result in results:
            row = []
            for key in keys:
                row.append(result[key])

            charts_table.append(row)

        f['gcharts'][1] = charts_table

    return jsonify(f)
