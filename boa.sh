#!/bin/bash

BASEDIR=$(dirname "$0")

java -cp .:$BASEDIR/dist/boa-compiler.jar:$BASEDIR/lib/hadoop-core-1.0.4.jar:$BASEDIR/lib/commons-lang-2.4.jar:$BASEDIR/lib/commons-math-2.1.jar boa.BoaMain $*
