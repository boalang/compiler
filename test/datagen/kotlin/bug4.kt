fun f() {
    val x = if (y) "bar" else @Ann {
        "foo"
    }
}
