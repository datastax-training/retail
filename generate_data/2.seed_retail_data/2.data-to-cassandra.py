#!/usr/bin/env python

import gzip
import logging
import re
import time

from decimal import Decimal
from six.moves import queue
from flask import Flask

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

logger = logging.getLogger('ingestion')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)


def init_cassandra():
    """
    create Cassandra session
    :return: cluster, session
    """

    # grab ip address information from application.cfg
    app = Flask(__name__)
    app.config.from_pyfile('/cornerstone/web/datastax/cornerstone-python/Cornerstone/application.cfg')
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


def parse_products(futures, session):
    """
    Parse file and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    insert_products_statement = session.prepare(
        'INSERT INTO retail.products (product_id, title, price) '
        'VALUES (?, ?, ?)')

    with gzip.open('/cache/downloads/Electronics.txt.gz') as f:
        # compile regular expressions
        p = re.compile('^product')
        p_id = re.compile('^product/productId: (.*)')
        p_title = re.compile('^product/title: (.*)')
        p_price = re.compile('^product/price: (.*)')

        # setup loop variables
        i = 0
        update_rate = 10000
        last_product_id = None
        print 'Every outputted line denotes %s processed lines...' % update_rate
        for line in iter(f.readline, ''):
            # keep track of current progress
            i += 1

            if re.findall(p, line):
                # find product_id
                m = re.match(p_id, line)
                product_id = m.group(1)

                if last_product_id == product_id:
                    f.readline()
                    f.readline()
                    continue
                else:
                    last_product_id = product_id

                # find title
                line = f.readline()
                m = re.match(p_title, line)
                title = m.group(1)

                # find price
                line = f.readline()
                m = re.match(p_price, line)
                price = m.group(1)

                if price == 'unknown':
                    # skip this data if price is unknown
                    continue
                else:
                    # properly parse price information
                    price = Decimal(price)

                values = {'product_id': product_id,
                          'title': title,
                          'price': price}
                if i % update_rate == 0:
                    # update the console on current progress
                    print values

                async_write_full_pipeline(futures, session,
                                          insert_products_statement, values)


def parse_brands(futures, session):
    """
    Parse file and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    insert_brands_statement = session.prepare(
        'INSERT INTO retail.brands (brand) '
        'VALUES (?)')

    with gzip.open('/cache/downloads/brands.txt.gz') as f:
        # compile regular expressions
        p = re.compile('(\w*) (.*)')

        # setup loop variables
        i = 0
        update_rate = 10000
        print 'Every outputted line denotes %s processed lines...' % update_rate
        for line in iter(f.readline, ''):
            # keep track of current progress
            i += 1

            m = re.match(p, line)
            brand = m.group(2).decode('utf-8', 'ignore')

            values = {'brand': brand}
            if i % update_rate == 0:
                # update the console on current progress
                print values

            if brand:
                async_write_full_pipeline(futures, session,
                                          insert_brands_statement, values)


def main():
    # set Cassandra futures queue size
    futures = queue.Queue(maxsize=121)

    # create Cassandra connections
    cluster, session = init_cassandra()

    # parse file
    parse_products(futures, session)
    parse_brands(futures, session)

    # cleanup final requests
    cleanup(futures, cluster, session)


if __name__ == "__main__":
    main()
