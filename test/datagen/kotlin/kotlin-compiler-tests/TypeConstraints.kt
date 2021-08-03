class foo<T>
    where T : R {
}

class foo<T : R> {
}

class foo<T : S, P>
    where T : R,
          P : M,
          T : Q {
    val b = fun <T: A> ()
        where T: B,
              T : T<C>,
              T : D
    val <T> c
        where T: B,
              T : T<C>,
              T : D
}
