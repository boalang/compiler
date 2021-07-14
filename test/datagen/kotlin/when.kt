fun f() : Unit {
    when (x) {
        is Int -> print(x)
        in 1..5 -> print(x)
        "foo" -> print(x)
        else -> print(x)
    }
}
