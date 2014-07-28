#!/bin/sh

java -cp .:dist/boa-compiler.jar:lib/hadoop-core-1.0.4.jar:lib/commons-lang-2.4.jar:lib/commons-math-2.1.jar boa.compiler.BoaCompiler -i $*
