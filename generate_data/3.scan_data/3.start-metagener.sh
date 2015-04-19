#!/usr/bin/env bash
set -x # echo on

YAML_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

sed "s#\\\$YAML_DIR#$YAML_DIR#" $YAML_DIR/black-friday.yaml > ../../cache/black-friday.tmp.yaml

../../tools/metagener/run --yaml ../../cache/black-friday.tmp.yaml

