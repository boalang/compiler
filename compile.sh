#!/bin/bash

BASEDIR=$(dirname "$0")

java -cp ".:$BASEDIR/dist/boa-compiler.jar:$BASEDIR/lib/*" boa.compiler.BoaCompiler -i $*
