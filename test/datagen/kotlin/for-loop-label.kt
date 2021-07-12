package boa.kotlin.test

fun foo(things: List<String>) {
    loop@ for(thing: String in things) {
        println(thing)
    }
}