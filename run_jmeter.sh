#!/bin/sh

~/TARs/apache-jmeter-2.13.1/bin/jmeter -Jhostname=127.0.0.1 -JnumUsers=1 -t ~/repos/black-friday/jmeter_files/techsupply.jmx -l ~/repos/black-friday/jmeter_files/techsupply.log
