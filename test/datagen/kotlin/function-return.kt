package boa.kotlin.test

fun factorial(x: Int) {
    if (x <= 1) return 1
    else return x * factorial(x - 1)
}