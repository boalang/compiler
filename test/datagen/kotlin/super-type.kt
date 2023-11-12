package boa.kotlin.test

class Derived(p: Int) : Base, Foo(p) {
      fun foo() {
          println("P is ${p}.")
      }
}