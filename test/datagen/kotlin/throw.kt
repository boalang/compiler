package boa.kotlin.test

class C {
      fun foo() {
          try {
              throw Exception("Foo")
          } catch (e: Exception) {
              quux()
          } finally {
              baz()
          }
      }
}