package boa.kotlin.test

class Person(val firstName: String) {
      constructor(firstName: String, lastName: String) : this(firstName) {
          println("hi")
      }
}

class Dog {
      constructor(name: String, owner: Person) {
          println("woof")
      }
}