package boa.kotlin.test

enum class ProtocolState {
     WAITING {
             override fun signal() = TALKING
     },
     TALKING {
             override fun signal() = WAITING
     };

     abstract fun signal(): ProtocolState
     
}
