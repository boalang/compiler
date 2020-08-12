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

##yield from not supported
