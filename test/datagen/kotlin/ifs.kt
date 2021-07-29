val y = 3
val xEE = if (y == 3) 1 else 2
val xEB = if (y == 3) 1 else { 2 }
val xBE = if (y == 3) { 1 } else 2
val xBB = if (y == 3) { 1 } else { 2 }

fun asStmt() {
    if (y == 3)
        xEE = 1
    else
        xEE = 2
    if (y == 3) {
        xBE = 1
    } else
        xBE = 2
    if (y == 3)
        xEB = 1
    else {
        xEB = 2
    }
    if (y == 3) {
        xBB = 1
    } else {
        xBB = 2
    }

    if (y == 3)
        ;
    else
        xNoT = 2

    if (y == 3)
        xNoF = 1
    else
        ;
}

fun asArgument() {
    CoNLLStats(
      precision = if (outputTotal > 0) correctDouble / outputTotal else 0.0,
      recall = if (goldTotal > 0) correctDouble / goldTotal else 0.0,
      f1Score = if (outputTotal > 0 && goldTotal > 0) 2 * correctDouble / (outputTotal + goldTotal) else 0.0
    )
}
