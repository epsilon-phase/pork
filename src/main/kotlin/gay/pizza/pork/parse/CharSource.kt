package gay.pizza.pork.parse

interface CharSource : PeekableSource<Char> {
  companion object {
    @Suppress("ConstPropertyName")
    const val NullChar = 0.toChar()
  }
}
