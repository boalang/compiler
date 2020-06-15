## Handled statements and variations:

1. For
  - For-else
2. While
  - while-else
3. If
  - if-elif-else
4. Continue
5. Break
6. Raise
7. Delete
8. Return
9. Try-except
  - try-except-finally-else
10. Assert
11. Pass
12. With
13. Yield

# Issues:

## High Priority

#### All keywords should be handled as literal, not VARACCESS

## Low Priority

#### Following assign operation not supported
```
x>>=5
x<<=5
x **= 5
~x
x//=5
```

#### `global` statement not handled
Handled in PythonDLTK library but not handled in Boa.
```
def myfunc2():
  global x
  x = "hello"
```


#### `nonlocal` keyword is not handled in library
In PythonDLTK project the following code returns true.

```
def outer():
    x = "local"

    def inner():
        nonlocal x
        x = "nonlocal"
        print("inner:", x)

    inner()
    print("outer:", x)
```

#### Classname inside Raise statement:
```
if True:
  raise ValueError
```
In above code, ValueError should not be added as VARACCESS.
```
"statements": [
                  {
                     "kind": "RAISE",
                     "expressions": [
                        {
                           "kind": "VARACCESS",
                           "variable": "ValueError"
                        }
                     ]
                  }
               ]
```

#### Check for Python 2 support

#### Error for the following expression condition:
```
if not type(x) is int:
  //something
```

#### Integer division not supported [RESOLVED]
```
a = 5//2
```

#### For Statement: [RESOLVED]
```
numbers = [2, 3, 5, 7]
for num in numbers:
  print(prime)
```

In the following example, inside the for statement expressions are added like this:

```
"expressions": [
                  {
                     "kind": "VARACCESS",
                     "variable": "nun"
                  },
                  {
                     "kind": "VARACCESS",
                     "variable": "numbers"
                  }
               ]
```
But it should be like:
```
"variable_declaration": {
   "name": "num",
   "variable_type": {
      "name": "int",
      "kind": "OTHER"
   }
},
"expressions": [
   {
      "kind": "VARACCESS",
      "variable": "numbers"
   }
]
```
#### Expression issue: [RESOLVED]
```
while True:
    print(count)
```
In the above example, `True` has been added in the AST as VARACCESS but it might be constant/literal.

```
"conditions": [
                  {
                     "kind": "VARACCESS",
                     "variable": "True"
                  }
               ]
```

## General datagen issues (can be fixed later)

1. If repo contains both .java file and .py file, datagen is producing error.
