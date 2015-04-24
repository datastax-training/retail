from flask import Blueprint, jsonify, request, render_template
# from rest import session
import rest

web_api = Blueprint('receipts_api', __name__)

get_receipt_by_id_stmt = None
get_receipt_by_cc = None


@web_api.route('/receipt')
def find_receipt_by_id():

    global get_receipt_by_id_stmt
    results = None

    receipt_id = request.args.get('receipt_id')

    if receipt_id is not None:
        if get_receipt_by_id_stmt is None:
           get_receipt_by_id_stmt = rest.session.prepare("SELECT * from retail.receipts WHERE receipt_id = ?")

        results = rest.session.execute(get_receipt_by_id_stmt,[long(receipt_id)])

    return render_template('receipt_search.jinja2', scans = results)

@web_api.route('/credit_card')
def find_receipt_by_credit_card():

    global get_receipt_by_cc
    results = None

    cc_no = request.args.get('cc_no')

    if cc_no is not None:
        if get_receipt_by_cc is None:
           get_receipt_by_cc = rest.session.prepare("SELECT * from retail.receipts_by_credit_card WHERE credit_card_number = ?")

        cc_no_long = long(cc_no)
        results = rest.session.execute(get_receipt_by_cc,[long(cc_no)])

    return render_template('credit_card_search.jinja2', receipts = results)
