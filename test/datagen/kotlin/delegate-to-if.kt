object EdgeToEdge
  : EdgeToEdgeImpl by if (Build.VERSION.SDK_INT >= 21) EdgeToEdgeApi21() else EdgeToEdgeBase()
