package boa.kotlin.test

class Six {
      fun foo(things: List<String>) {
          for(thing: String in things) {
                     println(thing)
          }
      }
      fun bar(thing: List<String) {
          for((index: int, value: String) in things.withIndex()) {
                      println(index, thing)
          }
      }
}