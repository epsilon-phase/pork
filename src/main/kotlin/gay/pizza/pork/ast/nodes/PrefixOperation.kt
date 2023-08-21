package gay.pizza.pork.ast.nodes

import gay.pizza.pork.ast.NodeType
import gay.pizza.pork.ast.NodeVisitor

class PrefixOperation(val op: PrefixOperator, val expression: Expression) : Expression() {
  override val type: NodeType = NodeType.PrefixOperation

  override fun <T> visitChildren(visitor: NodeVisitor<T>): List<T> =
    visitor.visitNodes(expression)

  override fun equals(other: Any?): Boolean {
    if (other !is PrefixOperation) return false
    return other.op == op && other.expression == expression
  }

  override fun hashCode(): Int {
    var result = op.hashCode()
    result = 31 * result + expression.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}
