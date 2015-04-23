#!/usr/bin/env python

import csv
import logging
import time

from decimal import Decimal
from six.moves import queue
from flask import Flask

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

# setup logger
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


def parse_zipcodes(futures, session):
    """
    Parse file and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    # prepare commonly used statements
    insert_zip_codes = session.prepare('''
        INSERT INTO retail.zipcodes
            (zipcode, city, state, lat, long, population, wages)
        VALUES
            (?, ?, ?, ?, ?, ?, ?)''')

    # read from zipcodes from csv file
    with open('free-zipcode-database.csv', 'rb') as csvfile:
        csv_reader = csv.DictReader(csvfile, delimiter=',', quotechar='"')
        for row in csv_reader:
            # create insert values object
            city = row['LocationText'].rsplit(',', 1)[0]
            values = {
                'zipcode': row['Zipcode'],
                'city': city if city else row['City'],
                'state': row['State'],
                'lat': float(row['Lat']) if row['Lat'] else None,
                'long': float(row['Long']) if row['Long'] else None,
                'population': int(row['EstimatedPopulation']) if row[
                    'EstimatedPopulation'] else None,
                'wages': int(row['TotalWages']) if row['TotalWages'] else None
            }

            async_write_full_pipeline(futures, session,
                                      insert_zip_codes, values)


def main():
    # set Cassandra futures queue size
    futures = queue.Queue(maxsize=31)

    # create Cassandra connections
    cluster, session = init_cassandra()

    # parse file
    parse_zipcodes(futures, session)

    # cleanup final requests
    cleanup(futures, cluster, session)


if __name__ == "__main__":
    main()
