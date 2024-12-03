fun f() : Unit {
    when (x) {
        is Int -> print(x)
        in 1..5 -> {
            print(x)
            print(x)
        }
        "foo" -> print(x)
        else -> {
            print(x)
        }
    }
}

var x = when(x) {
    3 -> when (x) {
        !is Int -> print(x)
        !in 1..5 -> {
            print(x)
            print(x)
        }
        "foo" -> print(x)
        else -> {
            print(x)
        }
    }
    else -> print(y)
}
