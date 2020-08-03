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

#### [Resolved]
def fun11(a):
    def fun22(b):
        print(b)
    if 2==3:
        a=4
    return fun22

fun11(12)(7) ## call like not supported now

#### method's default parameter value missing in boa ast [Resolved]
def create_model(input_shape, anchors_stride_base, num_classes, load_pretrained=True, freeze_body=2,
            weights_path='model_data/yolo_weights.h5'):
            
#### anchors = [float(x) for x in anchors.split(',')] : float method name missing [Resolved]

#### char2idx = {c: i for i, c in enumerate(idx2char)} [resolved]


####  time_series[i:i + seq_length, :] [resolved]

#### Import statements [RESOLVED]
This kind of local import is not handled: `from ..foo.bar import a as b, c`

#### Import Error [RESOLVED]
These imports are similar but parsed differently

```
from sklearn.model_selection import KFold
from sklearn.model_selection import cross_val_score
```
AST:

```
"statements": [
            {
               "kind": "EXPRESSION",
               "expressions": [
                  {
                     "kind": "VARACCESS",
                     "variable": "cross_val_score"
                  }
               ]
            }
         ],
         "imports": [
            "sklearn.model_selection.KFold",
            "sklearn.model_selection."
         ]
```

#### Multiple assignments can be in the same line [RESOLVED]
```
a, b = cifar10.load_data()  
```
or
```
(x_train, y_train), (x_test, y_test) = cifar10.load_data()
```

#### Arguments of print statement [RESOLVED]
The arguments are added as tuple and for `x_train.shape` AST is EMPTY.
`print('x_train shape:', x_train.shape)`

#### Array indexing
This kind of array handling is not handled:
`input_shape=x_train.shape[1:]`

#### Parameters of the classes are not added [RESOLVED]
`class Antirectifier(layers.Layer):`

#### Block comments are added as expression statements
```
class Antirectifier(layers.Layer):
    '''This is the combination of a sample-wise
    L2 normalization with the concatenation of the
    positive part of the input with the negative part
    of the input. The result is a tensor of samples that are
    twice as large as the input samples.
    '''
```

#### Return type of methods might not be needed in Python
For the following method empty return type is added: 
```
def compute_output_shape(self, input_shape):
        return tuple(shape)
```
:
```
"name": "call",
"return_type": {
  "name": "",
  "kind": "OTHER"
},
"arguments": [
  {
     "name": "self"
  },
  {
     "name": "inputs"
  }
],
```
#### Two attached method calls [RESOLVED]
```
lines = f.read().split('\n')
```
For above code an EMPTY expression is added somehow
```
"expressions": [
  {
     "kind": "VARACCESS",
     "variable": "f"
  },
  {
     "kind": "VARACCESS",
     "variable": "read"
  },
  {
     "kind": "EMPTY"
  }
                                 ],
```

#### This array comprehension is not handled. [RESOLVED]
Does not print any AST:
```
reverse_input_char_index = dict(
    (i, char) for char, i in input_token_index.items())
```

#### char in this part : for char, i not printed in ast
reverse_input_char_index = dict(
    (i, char) for char, i in input_token_index.items())

#### All keywords should be handled as literal, not VARACCESS [RESOLVED]

#### Decorator handling [RESOLVED]
Library issue fixed.

#### Exec statement should be handled. [RESOLVED]
Similar to print.
Multiple args of print should also be handled. 

## Low Priority

#### Conditional expression/shorthand if not supported [RESOLVED]
```
print("A") if a > b else print("B")
```

#### Following assign operation not supported [RESOLVED]
```
x>>=5
x<<=5
x **= 5
~x
x//=5
```

#### `global` statement not handled [RESOLVED]

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

#### Check for Python 2 support [Checked]

#### Error for the following expression condition: [RESOLVED]
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

#### xy = xy[::-1] 

## General datagen issues (can be fixed later)

1. If repo contains both .java file and .py file, datagen is producing error.
