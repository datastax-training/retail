__author__ = 'stevelowenthal'

from cassandra.cluster import Cluster
from cassandra.query import ordered_dict_factory

session = None

def init_cassandra(ip_addresses, keyspace):
    """
    Initialize Cassandra connections
    :param ip_addresses: ip addresses of Cassandra nodes
    :return:
    """
    global session

    cluster = Cluster(ip_addresses)
    session = cluster.connect(keyspace)
    session.row_factory = ordered_dict_factory

