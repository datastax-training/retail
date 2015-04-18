#!/usr/bin/env python

import calendar
import logging
import logging.handlers
import random
import requests
import socket
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


def _get_pending_receipts(session):
    """
    Get the next receipt to process
    :param session: Cassandra session()
    :return: yielded generator
    """

    get_pending_receipts = session.prepare(
        '''SELECT * FROM metagener.futures WHERE now = ?''')

    now = int(time.time())
    while True:
        results = session.execute(get_pending_receipts, {'now': now})
        for result in results:
            yield result
        now += 1


_number_types = frozenset((int, long, float))

# From: https://github.com/pycassa/pycassa/blob/master/pycassa/util.py#L21
def _convert_time_to_uuid(time_arg, lowest_val=True, randomize=False):
    """
    Converts a datetime or timestamp to a type 1 :class:`uuid.UUID`.
    This is to assist with getting a time slice of columns or creating
    columns when column names are ``TimeUUIDType``. Note that this is done
    automatically in most cases if name packing and value packing are
    enabled.
    Also, be careful not to rely on this when specifying a discrete
    set of columns to fetch, as the non-timestamp portions of the
    UUID will be generated randomly. This problem does not matter
    with slice arguments, however, as the non-timestamp portions
    can be set to their lowest or highest possible values.
    :param datetime:
      The time to use for the timestamp portion of the UUID.
      Expected inputs to this would either be a :class:`datetime`
      object or a timestamp with the same precision produced by
      :meth:`time.time()`. That is, sub-second precision should
      be below the decimal place.
    :type datetime: :class:`datetime` or timestamp
    :param lowest_val:
      Whether the UUID produced should be the lowest possible value
      UUID with the same timestamp as datetime or the highest possible
      value.
    :type lowest_val: bool
    :param randomize:
      Whether the clock and node bits of the UUID should be randomly
      generated.  The `lowest_val` argument will be ignored if this
      is true.
    :type randomize: bool
    :rtype: :class:`uuid.UUID`
    .. versionchanged:: 1.7.0
        Prior to 1.7.0, datetime objects were expected to be in
        local time. In 1.7.0 and beyond, naive datetimes are
        assumed to be in UTC and tz-aware objects will be
        automatically converted to UTC.
    """
    if isinstance(time_arg, uuid.UUID):
        return time_arg

    if hasattr(time_arg, 'utctimetuple'):
        seconds = int(calendar.timegm(time_arg.utctimetuple()))
        microseconds = (seconds * 1e6) + time_arg.time().microsecond
    elif type(time_arg) in _number_types:
        microseconds = int(time_arg * 1e6)
    else:
        raise ValueError('Argument for a v1 UUID column name or value was ' +
                         'neither a UUID, a datetime, or a number')

    # 0x01b21dd213814000 is the number of 100-ns intervals between the
    # UUID epoch 1582-10-15 00:00:00 and the Unix epoch 1970-01-01 00:00:00.
    timestamp = int(microseconds * 10) + 0x01b21dd213814000L

    time_low = timestamp & 0xffffffffL
    time_mid = (timestamp >> 32L) & 0xffffL
    time_hi_version = (timestamp >> 48L) & 0x0fffL

    if randomize:
        rand_bits = random.getrandbits(8 + 8 + 48)
        clock_seq_low = rand_bits & 0xffL  # 8 bits, no offset
        # keep the first two bits as 10 for the uuid variant
        clock_seq_hi_variant = 0b10000000 | (
            0b00111111 & ((rand_bits & 0xff00L) >> 8))  # 8 bits, 8 offset
        node = (rand_bits & 0xffffffffffff0000L) >> 16  # 48 bits, 16 offset
    else:
        # In the event of a timestamp tie, Cassandra compares the two
        # byte arrays directly. This is a *signed* comparison of each byte
        # in the two arrays.  So, we have to make each byte -128 or +127 for
        # this to work correctly.
        #
        # For the clock_seq_hi_variant, we don't get to pick the two most
        # significant bits (they're always 10), so we are dealing with a
        # positive byte range for this particular byte.
        if lowest_val:
            # Make the lowest value UUID with the same timestamp
            clock_seq_low = 0x80L
            clock_seq_hi_variant = 0 & 0x80L  # The two most significant bits
            # will be 10 for the variant
            node = 0x808080808080L  # 48 bits
        else:
            # Make the highest value UUID with the same timestamp

            # uuid timestamps have 100ns precision, while the timestamp
            # we have only has microsecond precision; to create the highest
            # uuid for the same microsecond, add 900ns
            timestamp = int(timestamp + 9)

            clock_seq_low = 0x7fL
            clock_seq_hi_variant = 0xbfL  # The two most significant bits will
            # 10 for the variant
            node = 0x7f7f7f7f7f7fL  # 48 bits
    return uuid.UUID(fields=(time_low, time_mid, time_hi_version,
                             clock_seq_hi_variant, clock_seq_low, node),
                     version=1)


def _request_from_metagener(endpoint):
    """
    Workaround for retrying requests to Metagener
    :param endpoint: REST API endpoint
    :return:
    """

    while True:
        try:
            return requests.get(endpoint).json()
        except:
            logger.exception(
                'Error seen when reading from Metagener. Retrying..')
            time.sleep(0.1)


def scan_items(futures, session):
    """
    Read from Metagener REST API and save data to Cassandra
    :param futures: queue.Queue
    :param session: Cassandra session()
    :return: None
    """

    # create tcp sockets for Spark streaming use
    TCP_IP = '127.0.0.1'
    TCP_PORT = 5005
    tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_socket.connect((TCP_IP, TCP_PORT))

    # prepare retail statements
    insert_itemscan = session.prepare('''
        INSERT INTO retail.registers
            (store_id, register_id, receipt_id, scan_time, scan_duration,
            quantity, product_id, product, price, msrp, savings)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)''')

    close_receipt = session.prepare('''
        INSERT INTO retail.receipts
            (cashier_id, cashier_first_name, cashier_last_name,
            cashier_last_initial, receipt_id, store_id, register_id,
            close_drawer, payment)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?)''')

    get_product = session.prepare(
        'SELECT product_id, price, title FROM retail.products WHERE '
        'product_id = ?')

    # prepare metagener helper statements
    insert_future_itemscan = session.prepare('''
        INSERT INTO metagener.futures
            (now, store_id, register_id, receipt_id)
        VALUES
            (?, ?, ?, ?)''')

    insert_active = session.prepare('''
        INSERT INTO metagener.active
            (store_id, register_id)
        VALUES
            (?, ?)''')

    is_active = session.prepare(
        'SELECT * FROM metagener.active WHERE store_id = ? AND register_id = ?')

    # setup Metagener REST API calls
    rest_api = 'http://localhost:8080/bulksample/retail/retail.item_scans/'
    batch_size = 1000
    endpoint = '%s%s' % (rest_api, batch_size)

    new_store = 'http://localhost:8080/sample/retail/retail.stores'
    new_payment = 'http://localhost:8080/sample/retail/retail.payments'
    get_employee = 'http://localhost:8080/sample/retail/retail.employees'

    # prepare spark streaming header
    spark_streaming_keys = ['store_id', 'register_id', 'receipt_id',
                            'scan_time', 'scan_duration', 'quantity',
                            'product_id', 'product', 'price', 'msrp', 'savings']
    spark_streaming_header = '#%s\n' % '::'.join(spark_streaming_keys)

    # instantiate our pending receipt generator
    receipts = _get_pending_receipts(session)
    now = int(time.time())
    log_file = 0
    while True:
        # at the beginning of each loop, after batch_size writes, maybe start
        # another register
        response = _request_from_metagener(new_store)['fieldValues']
        store_id = int(response['store_id'])
        max_registers = int(response['express_registers']) + \
                        int(response['full_registers'])
        register_id = random.randint(1, max_registers)

        active_store = session.execute(is_active, {'store_id': store_id,
                                                   'register_id': register_id})

        # check to see if random register has not yet been started
        if not active_store:
            # start register
            values = {
                'now': now + 1,
                'store_id': store_id,
                'register_id': register_id,
                'receipt_id': _convert_time_to_uuid(now + 1)
            }

            async_write_full_pipeline(futures, session,
                                      insert_future_itemscan, values)
            # mark register as active
            values = {
                'store_id': store_id,
                'register_id': register_id,
            }

            async_write_full_pipeline(futures, session,
                                      insert_active, values)

        # prepare spark streaming header
        spark_streaming = spark_streaming_header

        # grab a batch of item scans
        response = _request_from_metagener(endpoint)
        for sample in response['sampleValues']:
            field_values = sample['fieldValues']

            # get product information
            values = {'product_id': field_values['product_id']}
            product_result = session.execute(get_product, values)
            product = product_result[0]['title']
            msrp = product_result[0]['price']

            # calculate sane item discounts
            savings = Decimal(field_values['item_discount'])
            if savings > msrp:
                savings = msrp

            price = msrp - savings

            # grab the next receipt that will be written to
            receipt = receipts.next()
            store_id = receipt['store_id']
            register_id = receipt['register_id']
            receipt_id = receipt['receipt_id']
            now = receipt['now']

            values = {
                'store_id': store_id,
                'register_id': register_id,
                'receipt_id': receipt_id,
                'scan_time': now + int(field_values['scan_duration_seconds']),
                'scan_duration': int(field_values['scan_duration_seconds']),
                'quantity': Decimal(field_values['scan_qty']),
                'product_id': field_values['product_id'],
                'product': product,
                'price': Decimal("{0:.4f}".format(price)),
                'msrp': Decimal("{0:.4f}".format(msrp)),
                'savings': Decimal("{0:.4f}".format(savings)),
            }
            async_write_full_pipeline(futures, session, insert_itemscan, values)

            spark_streaming_values = [values[key] for key in
                                      spark_streaming_keys]
            spark_streaming += '%s\n' % '::'.join(
                str(x) for x in spark_streaming_values)

            # TODO: Handle the express register case
            # arbitrarily continue or close out receipt
            if random.randint(0, 10) < 3:
                # mark receipt as pending at some future time
                values = {
                    'now': now + int(field_values['scan_duration_seconds'] + 1),
                    'store_id': store_id,
                    'register_id': register_id,
                    'receipt_id': receipt_id
                }

                async_write_full_pipeline(futures, session,
                                          insert_future_itemscan, values)
            else:
                # TODO: Handle payments correctly. Fill out payment map
                employee = _request_from_metagener(get_employee)['fieldValues']
                payment = _request_from_metagener(new_payment)['fieldValues']
                values = {
                    'cashier_id': int(employee['employee_id']),
                    'cashier_first_name': employee['first_name'],
                    'cashier_last_name': employee['last_name'],
                    'cashier_last_initial': employee['last_initial'],

                    'receipt_id': receipt_id,
                    'store_id': store_id,
                    'register_id': register_id,
                    'close_drawer': now,
                    'payment': {'payment_type': payment['payment_type']}
                }

                async_write_full_pipeline(futures, session,
                                          close_receipt, values)

                payment_duration = 10

                # start new receipt at this register
                values = {
                    'now': now + payment_duration,
                    'store_id': store_id,
                    'register_id': register_id,
                    'receipt_id': _convert_time_to_uuid(now + payment_duration)
                }

                async_write_full_pipeline(futures, session,
                                          insert_future_itemscan, values)

        # write to files for potential Spark streaming use
        with open(
                'log/spark_streaming/retail.item_scans.txt.{'
                '0:010d}'.format(
                        log_file), 'w') as f:
            f.write(spark_streaming)
        log_file += 1

        # write to socket for current Spark streaming use
        tcp_socket.send(spark_streaming)

    tcp_socket.close()


def main():
    # set Cassandra futures queue size
    futures = queue.Queue(maxsize=31)

    # create Cassandra connections
    cluster, session = init_cassandra()

    scan_items(futures, session)

    # cleanup final requests
    cleanup(futures, cluster, session)


if __name__ == "__main__":
    main()
