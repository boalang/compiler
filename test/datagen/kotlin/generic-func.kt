fun <T> f(v: T) {
    return v + v
}

fun f2() {
    val expected = properties.map(KCallable<*>::name)
    return f<Int>(3);
}
