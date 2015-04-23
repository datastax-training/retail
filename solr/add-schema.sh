#!/bin/bash
set -v

if [ "$1" == "-r" ] ; then
    ACTION=RELOAD
else
    ACTION=CREATE
fi
   
echo "Posting solrconfig ..."
curl --data-binary @solrconfig.xml -H 'Content-type:text/xml; charset=utf-8' "http://localhost:8983/solr/resource/retail.zipcodes/solrconfig.xml"

echo "Posting schema ..."
curl --data-binary @zipcodes.xml -H 'Content-type:text/xml; charset=utf-8' "http://localhost:8983/solr/resource/retail.zipcodes/schema.xml" 

echo "Creating index..."
curl  -X POST "http://localhost:8983/solr/admin/cores?action=$ACTION&name=retail.zipcodes"
echo "Created index."
