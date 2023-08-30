package gay.pizza.pork.parse

import gay.pizza.pork.ast.nodes.*
import gay.pizza.pork.util.StringEscape

class Parser(source: PeekableSource<Token>, val attribution: NodeAttribution) {
  private val unsanitizedSource = source

  private fun readIntLiteral(): IntLiteral = within {
    expect(TokenType.IntLiteral) { IntLiteral(it.text.toInt()) }
  }

  private fun readStringLiteral(): StringLiteral = within {
    expect(TokenType.StringLiteral) {
      val content = StringEscape.unescape(StringEscape.unquote(it.text))
      StringLiteral(content)
    }
  }

  private fun readBooleanLiteral(): BooleanLiteral = within {
    expect(TokenType.True, TokenType.False) {
      BooleanLiteral(it.type == TokenType.True)
    }
  }

  private fun readListLiteral(): ListLiteral = within {
    expect(TokenType.LeftBracket)
    val items = collect(TokenType.RightBracket, TokenType.Comma) {
      readExpression()
    }
    expect(TokenType.RightBracket)
    ListLiteral(items)
  }

  private fun readSymbolRaw(): Symbol =
    expect(TokenType.Symbol) { Symbol(it.text) }

  private fun readSymbolCases(): Expression = within {
    val symbol = readSymbolRaw()
    if (next(TokenType.LeftParentheses)) {
      val arguments = collect(TokenType.RightParentheses, TokenType.Comma) {
        readExpression()
      }
      expect(TokenType.RightParentheses)
      FunctionCall(symbol, arguments)
    } else if (next(TokenType.Equals)) {
      Define(symbol, readExpression())
    } else {
      SymbolReference(symbol)
    }
  }

  private fun readLambda(): Lambda = within {
    expect(TokenType.LeftCurly)
    val arguments = mutableListOf<Symbol>()
    while (!peek(TokenType.In)) {
      val symbol = readSymbolRaw()
      arguments.add(symbol)
      if (next(TokenType.Comma)) {
        continue
      } else {
        break
      }
    }
    expect(TokenType.In)
    val items = collect(TokenType.RightCurly) {
      readExpression()
    }
    expect(TokenType.RightCurly)
    Lambda(arguments, items)
  }

  private fun readParentheses(): Parentheses = within {
    expect(TokenType.LeftParentheses)
    val expression = readExpression()
    expect(TokenType.RightParentheses)
    Parentheses(expression)
  }

  private fun readNegation(): PrefixOperation = within {
    expect(TokenType.Negation) {
      PrefixOperation(PrefixOperator.Negate, readExpression())
    }
  }

  private fun readIf(): If = within {
    expect(TokenType.If)
    val condition = readExpression()
    expect(TokenType.Then)
    val thenExpression = readExpression()
    var elseExpression: Expression? = null
    if (next(TokenType.Else)) {
      elseExpression = readExpression()
    }
    If(condition, thenExpression, elseExpression)
  }

  fun readExpression(): Expression {
    val token = peek()
    val expression = when (token.type) {
      TokenType.IntLiteral -> {
        readIntLiteral()
      }

      TokenType.StringLiteral -> {
        readStringLiteral()
      }

      TokenType.True, TokenType.False -> {
        readBooleanLiteral()
      }

      TokenType.LeftBracket -> {
        readListLiteral()
      }

      TokenType.Symbol -> {
        readSymbolCases()
      }

      TokenType.LeftCurly -> {
        readLambda()
      }

      TokenType.LeftParentheses -> {
        readParentheses()
      }

      TokenType.Negation -> {
        readNegation()
      }

      TokenType.If -> {
        readIf()
      }

      else -> {
        throw RuntimeException(
          "Failed to parse token: ${token.type} '${token.text}' as" +
            " expression (index ${unsanitizedSource.currentIndex})"
        )
      }
    }

    if (peek(
        TokenType.Plus,
        TokenType.Minus,
        TokenType.Multiply,
        TokenType.Divide,
        TokenType.Equality,
        TokenType.Inequality
      )
    ) {
      val infixToken = next()
      val infixOperator = convertInfixOperator(infixToken)
      return InfixOperation(expression, infixOperator, readExpression())
    }

    return expression
  }

  private fun convertInfixOperator(token: Token): InfixOperator =
    when (token.type) {
      TokenType.Plus -> InfixOperator.Plus
      TokenType.Minus -> InfixOperator.Minus
      TokenType.Multiply -> InfixOperator.Multiply
      TokenType.Divide -> InfixOperator.Divide
      TokenType.Equality -> InfixOperator.Equals
      TokenType.Inequality -> InfixOperator.NotEquals
      else -> throw RuntimeException("Unknown Infix Operator")
    }

  fun readProgram(): Program {
    val items = collect(TokenType.EndOfFile) { readExpression() }
    expect(TokenType.EndOfFile)
    return Program(items)
  }

  private fun <T> collect(
    peeking: TokenType,
    consuming: TokenType? = null,
    read: () -> T
  ): List<T> {
    val items = mutableListOf<T>()
    while (!peek(peeking)) {
      val expression = read()
      if (consuming != null) {
        next(consuming)
      }
      items.add(expression)
    }
    return items
  }

  private fun peek(vararg types: TokenType): Boolean {
    val token = peek()
    return types.contains(token.type)
  }

  private fun next(type: TokenType): Boolean {
    return if (peek(type)) {
      expect(type)
      true
    } else false
  }

  private fun expect(vararg types: TokenType): Token {
    val token = next()
    if (!types.contains(token.type)) {
      throw RuntimeException(
        "Expected one of ${types.joinToString(", ")} " +
          " but got type ${token.type} '${token.text}'"
      )
    }
    return token
  }

  private fun <T: Node> expect(vararg types: TokenType, consume: (Token) -> T): T =
    consume(expect(*types))

  private fun next(): Token {
    while (true) {
      val token = unsanitizedSource.next()
      attribution.push(token)
      if (ignoredByParser(token.type)) {
        continue
      }
      return token
    }
  }

  private fun peek(): Token {
    while (true) {
      val token = unsanitizedSource.peek()
      if (ignoredByParser(token.type)) {
        unsanitizedSource.next()
        continue
      }
      return token
    }
  }

  private fun <T: Node> within(block: () -> T): T {
    attribution.enter()
    return attribution.exit(block())
  }

  private fun ignoredByParser(type: TokenType): Boolean = when (type) {
    TokenType.BlockComment -> true
    TokenType.LineComment -> true
    TokenType.Whitespace -> true
    else -> false
  }
}
