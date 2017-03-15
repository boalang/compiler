#!/bin/bash

if [ "$#" -ne 2 ]; then
	echo "Usage: $0 path/to/input.boa output-dir/ [options]"
	exit -1
fi

if [ ! -f $1 ]; then
	echo "input '$1' is not a file"
	exit -2
fi

if [ -d $2 ]; then
    read -n 1 -p "output directory '$2' exists - delete? [Y/n] " yn
	echo ""

	yn=`echo $yn | tr '[:upper:]' '[:lower:]'`

	if [[ $yn =~ ^(y| ) ]] | [ -z $yn ]; then
        rm -Rf $2
	else
		echo "Please remove or provide a different output directory."
		exit -3
	fi
fi

BASEDIR=$(dirname "$0")

java -cp ".:$BASEDIR/dist/boa-compiler.jar:$BASEDIR/lib/*:$BASEDIR/lib/evaluator/*:$BASEDIR/compile" boa.BoaMain -e -d dataset/ -i $1 -o $2 $*
