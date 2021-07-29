fun f() {
    return CoNLLStats( // skip first field (metrics)
      precision = if (outputTotal > 0) correctDouble / outputTotal else 0.0,
      recall = if (goldTotal > 0) correctDouble / goldTotal else 0.0,
      f1Score = if (outputTotal > 0 && goldTotal > 0) 2 * correctDouble / (outputTotal + goldTotal) else 0.0
    )
}

