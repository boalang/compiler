val foo.baz: Int
  get() { return 3 }

public val foo.bar: Int
  private get() : String { return "hi" }

val foo.foo: Int
  get() = 3
  private set(x: Int) { foo.bar = x }

val foo.bad: String
  get
