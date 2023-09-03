package gay.pizza.pork.ast

import gay.pizza.pork.ast.nodes.*

class NodeCoalescer(val handler: (Node) -> Unit) : NodeVisitor<Unit> {
  override fun visitIntLiteral(node: IntLiteral): Unit = handle(node)
  override fun visitStringLiteral(node: StringLiteral): Unit = handle(node)
  override fun visitBooleanLiteral(node: BooleanLiteral): Unit = handle(node)
  override fun visitListLiteral(node: ListLiteral): Unit = handle(node)
  override fun visitSymbol(node: Symbol): Unit = handle(node)
  override fun visitFunctionCall(node: FunctionCall): Unit = handle(node)
  override fun visitDefine(node: Define): Unit = handle(node)
  override fun visitSymbolReference(node: SymbolReference): Unit = handle(node)
  override fun visitLambda(node: Lambda): Unit = handle(node)
  override fun visitParentheses(node: Parentheses): Unit = handle(node)
  override fun visitPrefixOperation(node: PrefixOperation): Unit = handle(node)
  override fun visitIf(node: If): Unit = handle(node)
  override fun visitInfixOperation(node: InfixOperation): Unit = handle(node)
  override fun visitProgram(node: Program): Unit = handle(node)

  private fun handle(node: Node) {
    handler(node)
    node.visitChildren(this)
  }
}
