package boa.kotlin.test

private enum class Test {
     ONE,
     TWO,
     private THREE
}

enum class Color(val rgb: Int) {
     RED(0xFF0000),
     GREEN(0x00FF00),
     BLUE(0x0000FF)
}

enum class ProtocolState {
     WAITING {
             override fun signal() = TALKING
     },
     TALKING {
             override fun signal() = WAITING
     };

     abstract fun signal(): ProtocolState
     
}
