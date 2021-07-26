data class Example(@field:Ann val foo: Int, @get:Ann val bar: String, @param:Ann val quux: String)

class C {
    fun f() {
        val x = 3
        @Ann1(5, x) @Ann2 @Ann3(str = "hi") print(x)
    }
}
