class C {
    val x = 5
    var y = "foo"
}
class Child : MyInterface {
    override fun bar() {
        // body
    }
}
data class User(val name: String, val age: Int)

data class Person(val name: String) {
    var age: Int = 0
}
enum class Direction {
    NORTH, SOUTH, WEST, EAST
}
enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}
interface MyInterface {
    fun bar()
    fun foo() {
      // optional body
    }
}
fun f() {
for (item in collection) print(item)                                                                                
                                                                                                                        
for (item: Int in ints) {                                                                                               
}                                                                                                                       
for (i in 1..3) {                                                                                                       
    println(i)                                                                                                          
}                                                                                                                       
/*                                                                                                                      
for (i in 6 downTo 0 step 2) {                                                                                          
    println(i)                                                                                                          
}                                                                                                                       
*/                                                                                                                      
for (i in array.indices) {                                                                                              
    println(array[i])                                                                                                   
}                                                                                                                       
for ((index, value) in array.withIndex()) {                                                                             
    println("the element at $index is $value")                                                                          
}                                                                                                                       
    while (y)                                                                                                           
        println(3)                                                                                                      
    lab@ do {                                                                                                           
        print("hi")                                                                                                     
        break@lab                                                                                                       
        continue@f                                                                                                      
    } while (x)                                                                                                         
}
