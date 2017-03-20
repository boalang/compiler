#!/bin/bash

if [ "$#" -eq 0 ]; then
    echo "Usage: $0 file.boa [options]"
    exit -1
fi

if [ ! -f "$1" ]; then
    echo "input '$1' is not a file"
    exit -2
fi

BASEDIR=$(dirname "$0")

$BASEDIR/boa.sh -c -i $*
