@Retention(AnnotationRetention.SOURCE) @Repeatable annotation class ann

fun bar(x: () -> Unit) {}

fun foo(x: () -> Unit) {
    bar @ann {
        print(1)
    }

    bar @ann @[ann] {
        print(2)
    }

    bar() @ann {
        print(1)
    }

    bar() @[ann] {
        print(2)
    }

    bar() @ann @[ann] {
        print(2)
    }

    bar(@ann {
        print(3)
    })

    if (true) @ann {

    }
    else @[ann] @ann {

    }
}
