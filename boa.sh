#!/usr/bin/env bash

BASEDIR=$(dirname "$0")

java -cp ".:$BASEDIR/dist/boa-compiler.jar:$BASEDIR/lib/*:$BASEDIR/lib/evaluator/*:$BASEDIR/lib/datagen/*:$BASEDIR/lib/datagen-kotlin/*:$BASEDIR/compile" boa.BoaMain $*
