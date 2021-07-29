fun <T> f(v: T) {
    return v + v
}

fun f2() {
    return f<Int>(3);
}
