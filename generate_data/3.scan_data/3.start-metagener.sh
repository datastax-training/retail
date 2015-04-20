#!/usr/bin/env bash
set -x # echo on

YAML_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CACHE_DIR=../../cache


sed "s#\\\$CACHE_DIR#$CACHE_DIR#" $YAML_DIR/black-friday.yaml > ../../cache/black-friday.yaml
sed "s#\\\$CACHE_DIR#$CACHE_DIR#" $YAML_DIR/black-friday.metagener > ../../cache/black-friday.metagener

../../tools/metagener/run --yaml $CACHE_DIR/black-friday.yaml

