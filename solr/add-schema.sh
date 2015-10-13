#!/bin/bash
KEYSPACE=retail

if [ "$1" == "-r" ] ; then
    ACTION=Reload
    shift
else
    ACTION=Create
fi

if [ "$1" == "" ]; then
   echo "Usage: add-schema [-r] <table name> "
   exit 1
fi

#Strip of the .xml if it exists
TABLE=${1%%.xml}

echo
echo "$ACTION core for table $KEYSPACE.$TABLE"
echo

if [ "$ACTION" == "Create" ] ; then
  dsetool create_core $KEYSPACE.$TABLE schema=$TABLE.xml solrconfig=solrconfig.xml reindex=true
else
  dsetool reload_core $KEYSPACE.$TABLE schema=$TABLE.xml solrconfig=solrconfig.xml reindex=true
fi

