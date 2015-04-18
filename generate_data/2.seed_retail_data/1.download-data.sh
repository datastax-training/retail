#!/usr/bin/env bash
set -x # echo on

# http://snap.stanford.edu/data/amazon/
wget -c http://snap.stanford.edu/data/amazon/Electronics.txt.gz -P ../../cache/downloads
wget -c http://snap.stanford.edu/data/amazon/brands.txt.gz -P ../../cache/downloads
