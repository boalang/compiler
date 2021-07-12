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
}