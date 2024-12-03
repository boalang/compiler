package boa.kotlin.test

class C {
      fun foo() {
          try {
              bar()
          } catch (e: Exception) {
              quux()
          } finally {
              baz()
          }
      }

    fun f(): Int = try {
        3
    } finally {
        5
    }
}
