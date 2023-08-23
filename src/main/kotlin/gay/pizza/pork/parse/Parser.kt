package gay.pizza.pork.parse

import gay.pizza.pork.ast.nodes.*
import gay.pizza.pork.util.StringEscape

class Parser(source: PeekableSource<Token>) {
  private val unsanitizedSource = source

  private fun readIntLiteral(): IntLiteral {
    val token = expect(TokenType.IntLiteral)
    return IntLiteral(token.text.toInt())
  }

  private fun readSymbol(): Symbol {
    val token = expect(TokenType.Symbol)
    return Symbol(token.text)
  }

  private fun readIf(): If {
    expect(TokenType.If)
    val condition = readExpression()
    expect(TokenType.Then)
    val thenExpression = readExpression()
    var elseExpression: Expression? = null
    if (peekType(TokenType.Else)) {
      expect(TokenType.Else)
      elseExpression = readExpression()
    }
    return If(condition, thenExpression, elseExpression)
  }

  private fun readSymbolCases(): Expression {
    val symbol = readSymbol()
    return if (peekType(TokenType.LeftParentheses)) {
      expect(TokenType.LeftParentheses)
      val arguments = collectExpressions(TokenType.RightParentheses, TokenType.Comma)
      expect(TokenType.RightParentheses)
      FunctionCall(symbol, arguments)
    } else if (peekType(TokenType.Equals)) {
      expect(TokenType.Equals)
      Define(symbol, readExpression())
    } else {
      SymbolReference(symbol)
    }
  }

  fun readLambda(): Lambda {
    expect(TokenType.LeftCurly)
    val arguments = mutableListOf<Symbol>()
    while (!peekType(TokenType.In)) {
      val symbol = readSymbol()
      arguments.add(symbol)
      if (peekType(TokenType.Comma)) {
        expect(TokenType.Comma)
        continue
      } else {
        break
      }
    }
    expect(TokenType.In)
    val items = collectExpressions(TokenType.RightCurly)
    expect(TokenType.RightCurly)
    return Lambda(arguments, items)
  }

  fun readExpression(): Expression {
    val token = peek()
    val expression = when (token.type) {
      TokenType.StringLiteral -> {
        expect(TokenType.StringLiteral)
        return StringLiteral(StringEscape.unescape(StringEscape.unquote(token.text)))
      }

      TokenType.IntLiteral -> {
        readIntLiteral()
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
        expect(TokenType.LeftParentheses)
        val expression = readExpression()
        expect(TokenType.RightParentheses)
        Parentheses(expression)
      }

      TokenType.True -> {
        expect(TokenType.True)
        return BooleanLiteral(true)
      }

      TokenType.False -> {
        expect(TokenType.False)
        return BooleanLiteral(false)
      }

      TokenType.Negation -> {
        expect(TokenType.Negation)
        return PrefixOperation(PrefixOperator.Negate, readExpression())
      }

      TokenType.If -> {
        return readIf()
      }

      else -> {
        throw RuntimeException(
          "Failed to parse token: ${token.type} '${token.text}' as expression" +
          " (index ${unsanitizedSource.currentIndex})")
      }
    }

    if (peekType(
        TokenType.Plus,
        TokenType.Minus,
        TokenType.Multiply,
        TokenType.Divide,
        TokenType.Equality,
        TokenType.Inequality)) {
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

  fun readListLiteral(): ListLiteral {
    expect(TokenType.LeftBracket)
    val items = collectExpressions(TokenType.RightBracket, TokenType.Comma)
    expect(TokenType.RightBracket)
    return ListLiteral(items)
  }

  fun readProgram(): Program {
    val items = collectExpressions(TokenType.EndOfFile)
    expect(TokenType.EndOfFile)
    return Program(items)
  }

  private fun collectExpressions(peeking: TokenType, consuming: TokenType? = null): List<Expression> {
    val items = mutableListOf<Expression>()
    while (!peekType(peeking)) {
      val expression = readExpression()
      if (consuming != null && !peekType(peeking)) {
        expect(consuming)
      }
      items.add(expression)
    }
    return items
  }

  private fun peekType(vararg types: TokenType): Boolean {
    val token = peek()
    return types.contains(token.type)
  }

  private fun expect(type: TokenType): Token {
    val token = next()
    if (token.type != type) {
      throw RuntimeException("Expected token type '${type}' but got type ${token.type} '${token.text}'")
    }
    return token
  }

  private fun next(): Token {
    while (true) {
      val token = unsanitizedSource.next()
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

  private fun ignoredByParser(type: TokenType): Boolean = when (type) {
    TokenType.BlockComment -> true
    TokenType.LineComment -> true
    TokenType.Whitespace -> true
    else -> false
  }
}