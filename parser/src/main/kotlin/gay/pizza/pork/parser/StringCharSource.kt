package gay.pizza.pork.parser

class StringCharSource(val input: String) : CharSource {
  private var index = 0
  override val currentIndex: Int
    get() = index

  override fun next(): Char {
    if (index == input.length) {
      return CharSource.NullChar
    }
    val char = input[index]
    index++
    return char
  }

  override fun peek(): Char {
    if (index == input.length) {
      return CharSource.NullChar
    }
    return input[index]
  }
}