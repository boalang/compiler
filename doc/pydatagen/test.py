# for, while, else, break, continue
primes = [2, 3, 5, 7]
for prime in primes:
    print(prime)

for x in range(5):
    print(x)

for x in range(3, 8, 2):
    print(x)

count = 0
while count < 5:
    print(count)
    count += 1
    break

while True:
    print(count)
    count += 1
    continue

# Prints out 1,2,3,4
for i in range(1, 10):
    if(i%5==0):
        break
    print(i)
else:
    print("this is not printed")

while True:
    print(count)
else:
    print("this is not printed")



raise Exception("Sorry, no numbers below zero")
raise
raise Exception ## handled but added as VARACCESS
