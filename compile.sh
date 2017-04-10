#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 file.boa [options]"
    exit -1
fi

if [ ! -f "$1" ]; then
    echo "Error: input '$1' is not a file"
    echo "Usage: $0 file.boa [options]"
    exit -2
fi

$(dirname "$0")/boa.sh -c -i $*
