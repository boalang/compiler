fun f() {
    val items = listOf(1, 2, 3, 4, 5)

    items.fold(0, { 
        acc: Int, i: Int -> acc + i
    })

    val x = { print("hi") }
    x()
}
