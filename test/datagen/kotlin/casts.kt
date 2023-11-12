fun f() {
    val x = 5
    val y = x as Float
    if (x is String) x = 3
    if (x !is String) x = 3
    val z = x as? Float
}
