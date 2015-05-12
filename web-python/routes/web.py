from collections import OrderedDict
from flask import Blueprint, request, render_template
from helpers import cassandra_helper
import json

# from rest import session
from collections import namedtuple

web_api = Blueprint('web_api', __name__)

def init():
    global get_receipt_by_id_stmt
    global get_product_by_id_stmt
    global get_product_by_brand_cc
    global get_product_by_category_cc
    global get_receipt_by_cc

    get_product_by_brand_cc = cassandra_helper.session.prepare("SELECT * from products_by_supplier WHERE supplier_id = ? limit 300")
    get_product_by_category_cc = cassandra_helper.session.prepare("SELECT * from products_by_category_name WHERE category_name = ? limit 300")
    get_product_by_id_stmt = cassandra_helper.session.prepare("SELECT * from products_by_id WHERE product_id = ?")
    get_receipt_by_id_stmt = cassandra_helper.session.prepare("SELECT * from receipts WHERE receipt_id = ?")
    get_receipt_by_cc = cassandra_helper.session.prepare("SELECT * from receipts_by_credit_card WHERE credit_card_number = ?")

@web_api.route('/')
def index():
    return render_template('index.jinja2')

@web_api.route('/product_search')
def search_for_products():

    global get_product_by_brand_cc
    global get_product_by_category_cc

    results = None

    brand_id = request.args.get('brand_id')
    category_name = request.args.get('category_name')

    if brand_id:
        results = cassandra_helper.session.execute(get_product_by_brand_cc,[long(brand_id)])
    elif category_name:
        results = cassandra_helper.session.execute(get_product_by_category_cc,[category_name])

    return render_template('product_list.jinja2', products = results)


@web_api.route('/product')
def find_product_by_id():

    global get_product_by_id_stmt
    product = None
    features = None

    product_id = request.args.get('product_id')

    if product_id is not None:
        results = cassandra_helper.session.execute(get_product_by_id_stmt,[product_id])

        if results:
            product = results[0]
            features = product["features"]

    return render_template('product_detail.jinja2', product = product, features=features)

@web_api.route('/receipt')
def find_receipt_by_id():

    global get_receipt_by_id_stmt

    results = None

    receipt_id = request.args.get('receipt_id')

    if receipt_id is not None:
        results = cassandra_helper.session.execute(get_receipt_by_id_stmt,[long(receipt_id)])

    return render_template('receipt_detail.jinja2', scans = results)

@web_api.route('/credit_card')
def find_receipt_by_credit_card():

    global get_receipt_by_cc
    results = None

    cc_no = request.args.get('cc_no')

    if cc_no:
        results = cassandra_helper.session.execute(get_receipt_by_cc, [long(cc_no)])

    return render_template('credit_card_search.jinja2', receipts = results)

@web_api.route('/search')
def search():
    # this will search the city field in the zipcodes solr core
    # the import parameter is 's'

    search_term = request.args.get('s')

    if not search_term:
        return render_template('search_list.jinja2',
                               products = None)

    filter_by = request.args.get('filter_by')

    # parameters to solr are rows=300  wt (writer type)=json, and q=city:<keyword> sort=zipcode asc
    # note: escape quote any quotes that are part of the query / filter query
    solr_query = '"q":"title:%s"' % search_term.replace('"','\\"').encode('utf-8')

    if filter_by:
        solr_query += ',"fq":"%s"' % filter_by.replace('"','\\"').encode('utf-8')

    query = "SELECT * FROM products_by_id WHERE solr_query = '{%s}' LIMIT 300" % solr_query

    # get the response
    results = cassandra_helper.session.execute(query)

    facet_query = 'SELECT * FROM products_by_id WHERE solr_query = ' \
                  '\'{%s,"facet":{"field":["supplier_name","category_name"]}}\' ' % solr_query

    facet_results = cassandra_helper.session.execute(facet_query)
    facet_string = facet_results[0].get("facet_fields")

    # convert the facet string to an ordered dict because solr sorts them desceding by count, and we like it!
    facet_map = json.JSONDecoder(object_pairs_hook=OrderedDict).decode(facet_string)

    return render_template('search_list.jinja2',
                           search_term = search_term,
                           categories = filter_facets(facet_map['category_name']),
                           suppliers = filter_facets(facet_map['supplier_name']),
                           products = results,
                           filter_by = filter_by)

#
# The facets come in a list [ 'value1', 10, 'value2' 5, ...] with numbers in descending order
# We convert it to a list of [('value1',10), ('value2',5) ... ]
#
def filter_facets(raw_facets):
    # keep only the facets that have > 0 items

    FacetValue = namedtuple('FacetValue', ['name', 'amount'])
    return [FacetValue(name,amount) for name,amount in raw_facets.iteritems() if amount > 0]
