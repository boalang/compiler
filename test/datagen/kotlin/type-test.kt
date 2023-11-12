val y = "foo"
val x = if (y is String) 3 else 5
val z = if (y !is String) 3 else 5
