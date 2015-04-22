#!/usr/bin/env python

import calendar
import logging
import logging.handlers
import random
import requests
import time
import uuid

from decimal import Decimal
from flask import Flask
from six.moves import queue

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

# setup logger
logger = logging.getLogger('ingestion')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
ch.setLevel(logging.WARN)
logger.addHandler(ch)


def init_cassandra():
    """
    create Cassandra session
    :return: cluster, session
    """

    # grab ip address information from application.cfg
    app = Flask(__name__)
    app.config.from_pyfile('../../web/application.cfg')
    ip_addresses = app.config['DSE_CLUSTER'].split(',')

    # connect to Cassandra
    cluster = Cluster(ip_addresses)
    session = cluster.connect()
    session.row_factory = ordered_dict_factory

    return cluster, session


def cleanup(futures, cluster, session):
    """
    Cleanup all pending threads before shutting down the Cluster() object
    :param futures: Queue of futures
    :param cluster: Cluster() object for shutdown()
    :param session: Session() object for retries
    :return:
    """
    while True:
        try:
            old_future = futures.get_nowait()
            old_future.result()
        except queue.Empty:
            break
        except Exception:
            logger.exception('Operation failed:')
            time.sleep(2)

            logger.info('Retrying: %s' % old_future.query)
            future = session.execute_async(old_future.query)
            futures.put_nowait(future)
    cluster.shutdown()


def async_write_full_pipeline(futures, session, prepared_statement, values):
    """
    Ensure the driver's pipeline stays full.
    :param futures: Queue of futures
    :param session: Cassandra session()
    :param prepared_statement: Prepared statement that will be executed
    :param values: Values to send with prepared statement
    :return:
    """

    # check if futures warmed up
    if futures.full():
        while True:
            # clear old future
            old_future = futures.get_nowait()
            try:
                old_future.result()
                break
            except Exception:
                logger.exception('Operation failed:')
                time.sleep(2)

                logger.info('Retrying: %s' % old_future.query)
                future = session.execute_async(old_future.query)
                futures.put_nowait(future)

    future = session.execute_async(prepared_statement, values)
    futures.put_nowait(future)


def populate_stores(futures, session):
    """
    Read from Metagener REST API and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    insert_store = session.prepare('''
        INSERT INTO retail.stores
            (store_id, tax_rate, express_registers, full_registers, street,
            city, state, zipcode)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?)''')

    get_city_state = session.prepare('''
        SELECT city, state FROM retail.zipcodes WHERE zipcode = ?''')

    rest_api = 'http://localhost:8080/bulksample/retail/retail.stores/'
    batch_size = 1000
    endpoint = '%s%s' % (rest_api, batch_size)

    for i in range(30):
        print "endpoint %s" % endpoint
        response = requests.get(endpoint).json()
        for sample in response['sampleValues']:
            field_values = sample['fieldValues']

            zipcode = field_values['zipcode']

            while True:
                try:
                    zipcode_result = session.execute(get_city_state,
                                                     {'zipcode': zipcode})
                    break
                except Exception:
                    logger.exception('Operation failed:')
                    time.sleep(2)

            city = zipcode_result[0]['city']
            state = zipcode_result[0]['state']

            street_address = '%s %s' % (
                field_values['street_no'], field_values['street'])

            tax_rate = Decimal("{0:.4f}".format(field_values['tax_rate']))
            if state in ['DE', 'MT', 'NH', 'OR']:
                tax_rate = Decimal(0)

            values = {
                'store_id': int(field_values['store_id']),
                'tax_rate': tax_rate,
                'express_registers': int(field_values['express_registers']),
                'full_registers': int(field_values['full_registers']),
                'street': street_address,
                'city': city,
                'state': state,
                'zipcode': zipcode
            }

            async_write_full_pipeline(futures, session, insert_store, values)


def populate_employees(futures, session):
    """
    Read from Metagener REST API and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    insert_employee = session.prepare('''
        INSERT INTO retail.employees
            (employee_id, store_id, first_name, last_name, last_initial)
        VALUES
            (?, ?, ?, ?, ?)''')

    rest_api = 'http://localhost:8080/bulksample/retail/retail.employees/'
    batch_size = 10000
    endpoint = '%s%s' % (rest_api, batch_size)

    for i in range(60):
        response = requests.get(endpoint).json()
        for sample in response['sampleValues']:
            field_values = sample['fieldValues']

            values = {
                'employee_id': int(field_values['employee_id']),
                'store_id': int(field_values['store_id']),
                'first_name': field_values['first_name'],
                'last_name': field_values['last_name'],
                'last_initial': field_values['last_initial'],
            }

            async_write_full_pipeline(futures, session, insert_employee, values)


def populate_receipts(futures, session):
    """
    Read from Metagener REST API and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    insert_receipts = session.prepare('''
        INSERT INTO retail.receipts
            (store_id, receipt_id, total, cashier_first_name, cashier_id, cashier_last_initial, cashier_last_name, close_drawer, payment, register_id, savings, subtotal, tax_rate)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)''')

    rest_api = 'http://localhost:8080/bulksample/retail/retail.receipts/'
    batch_size = 10000
    endpoint = '%s%s' % (rest_api, batch_size)

    for i in range(60):
        response = requests.get(endpoint).json()
        for sample in response['sampleValues']:
            field_values = sample['fieldValues']

            values = {
                'store_id': int(field_values['store_id']),
                'receipt_id': field_values['receipt_id'],
                'total': field_values['total'],
                'cashier_first_name': field_values['cashier_first_name'],
                'cashier_id': int(field_values['cashier_id']),
                'cashier_last_initial': field_values['cashier_last_initial'],
                'cashier_last_name': field_values['cashier_last_name'],
                'close_drawer': field_values['close_drawer'],
                'payment': field_values['payment'],
                'register_id': int(field_values['register_id']),
                'savings': field_values['savings'],
                'subtotal': field_values['subtotal'],
                'tax_rate': field_values['tax_rate']
            }

            async_write_full_pipeline(futures, session, insert_receipts, values)


def main():
    # set Cassandra futures queue size
    futures = queue.Queue(maxsize=31)

    # create Cassandra connections
    cluster, session = init_cassandra()

    populate_receipts(futures, session)
    #populate_stores(futures, session)
    populate_employees(futures, session)

    # cleanup final requests
    cleanup(futures, cluster, session)


if __name__ == "__main__":
    main()
