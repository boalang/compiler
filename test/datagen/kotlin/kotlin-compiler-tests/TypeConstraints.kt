class foo<T : S>
    where T : R,
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
