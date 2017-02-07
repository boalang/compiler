#!/bin/bash

BASEDIR=$(dirname "$0")

java -cp ".:$BASEDIR/dist/boa-compiler.jar:$BASEDIR/lib/*:$BASEDIR/lib/evaluator/*:$BASEDIR/lib/datagen/*:$BASEDIR/compile" boa.BoaMain $*
