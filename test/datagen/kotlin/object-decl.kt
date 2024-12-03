package boa.kotlin.test

object Greeter {
       fun greet(greeted: String) {
           println("Hello ${bar}!")
       }
}

class C {
    private fun getObject() = object {
        val x: String = "x"
    }
}
