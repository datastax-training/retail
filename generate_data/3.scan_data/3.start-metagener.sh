#!/usr/bin/env bash
set -x # echo on

nc -l 5005 &

./run-metagener --yaml black-friday.yaml
