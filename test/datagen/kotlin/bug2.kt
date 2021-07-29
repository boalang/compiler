fun f() {
    (1..Math.sqrt(number.toDouble()).toInt() step if (number % 2 == 0) 1 else 2)
        .forEach { if (number % it == 0) divisors.add(it) }
}
