from flask import Blueprint, jsonify, request, render_template
import urllib
import urllib2
import json

# from rest import session
import rest

web_api = Blueprint('web_api', __name__)

def init():
    global get_receipt_by_id_stmt
    global get_product_by_id_stmt
    global get_product_by_brand_cc
    global get_product_by_category_cc
    global get_receipt_by_cc

    get_product_by_brand_cc = rest.session.prepare("SELECT * from retail.products_by_supplier WHERE supplier_id = ?")
    get_product_by_category_cc = rest.session.prepare("SELECT * from retail.products_by_category_name WHERE category_name = ?")
    get_product_by_id_stmt = rest.session.prepare("SELECT * from retail.products_by_id WHERE product_id = ?")
    get_receipt_by_id_stmt = rest.session.prepare("SELECT * from retail.receipts WHERE receipt_id = ?")
    get_receipt_by_cc = rest.session.prepare("SELECT * from retail.receipts_by_credit_card WHERE credit_card_number = ?")

@web_api.route('/')
def index():
    return render_template('index.jinja2')

@web_api.route('/brand')
def find_products_by_brand():

    global get_product_by_brand_cc
    global get_product_by_category_cc
    results = None

    brand_id = request.args.get('brand_id')
    category_name = request.args.get('category_name')

    if brand_id:
        results = rest.session.execute(get_product_by_brand_cc,[long(brand_id)])
    elif category_name:
        results = rest.session.execute(get_product_by_category_cc,[category_name])

    return render_template('product_list.jinja2', products = results)


@web_api.route('/product')
def find_product_by_id():

    global get_product_by_id_stmt
    results = None
    product = None
    features = None

    product_id = request.args.get('product_id')

    if product_id is not None:
        results = rest.session.execute(get_product_by_id_stmt,[product_id])

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
        results = rest.session.execute(get_receipt_by_id_stmt,[long(receipt_id)])

    return render_template('receipt_detail.jinja2', scans = results)

@web_api.route('/credit_card')
def find_receipt_by_credit_card():

    global get_receipt_by_cc
    results = None

    cc_no = request.args.get('cc_no')

    if cc_no is not None:
        results = rest.session.execute(get_receipt_by_cc,[long(cc_no)])

    return render_template('credit_card_search.jinja2', receipts = results)

@web_api.route('/search')
def search():
    # this will search the city field in the retail.zipcodes solr core
    # the import parameter is 's'

    keyword = request.args.get('s')
    facet = request.args.get('facet')

    # parameters to solr are rows=30  wt (writer type)=json, and q=city:<keyword> sort=zipcode asc
    parameters = urllib.urlencode({'rows':'300', 'wt': 'json', 'q': "title:" + keyword})
    url='http://localhost:8983/solr/retail.products_by_id/select?' + parameters

    # get the response
    response = urllib2.urlopen(url)

    # fish out the docs from the solr response
    parsed_response = json.loads(response.read())
    docs = parsed_response['response']['docs']

    return render_template('product_list.jinja2', products = docs)


