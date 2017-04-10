#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 file.boa [options]"
    $(dirname "$0")/boa.sh -c
    exit -1
fi

if [ ! -f "$1" ]; then
    echo "input '$1' is not a file"
    echo "Usage: $0 file.boa [options]"
    $(dirname "$0")/boa.sh -c
    exit -2
fi

$(dirname "$0")/boa.sh -c -i $*
