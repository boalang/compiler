#!/bin/bash

if [ "$#" -lt 2 ]; then
    echo "Usage: $0 path/to/input.boa output-dir/ [options]"
    exit -1
fi

if [ ! -f $1 ]; then
    echo "Error: input '$1' is not a file"
    echo "Usage: $0 path/to/input.boa output-dir/ [options]"
    exit -2
fi

if [ -d $2 ]; then
    read -n 1 -p "output directory '$2' exists - delete? [Y/n] " yn
    echo ""

    yn=`echo $yn | tr '[:upper:]' '[:lower:]'`

    if [ "$yn" == "y" ] || [ "$yn" == "" ]; then
        rm -Rf $2
    else
        echo "Please remove or provide a different output directory."
        exit -3
    fi
fi

$(dirname "$0")/boa.sh -e -d dataset/ -i $1 -o $2 $*
