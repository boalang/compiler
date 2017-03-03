#!/bin/bash

if [ "$#" -eq 0 ]; then
	echo "Usage: $0 file.boa [options]"
fi

BASEDIR=$(dirname "$0")

java -cp ".:$BASEDIR/dist/boa-compiler.jar:$BASEDIR/lib/*:$BASEDIR/lib/evaluator/*:$BASEDIR/compile" boa.BoaMain -e -d dataset/ -i $1 -o $2 $*
