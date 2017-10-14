#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 file.boa [options]"
    $(dirname "$0")/boa.sh -c
    exit -1
fi

$(dirname "$0")/boa.sh -c -i $*
