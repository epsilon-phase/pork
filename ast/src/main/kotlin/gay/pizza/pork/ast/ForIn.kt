// GENERATED CODE FROM PORK AST CODEGEN
package gay.pizza.pork.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("forIn")
class ForIn(val symbol: Symbol, val expression: Expression, val block: Block) : Expression() {
  override val type: NodeType = NodeType.ForIn

  override fun <T> visitChildren(visitor: NodeVisitor<T>): List<T> =
    visitor.visitNodes(symbol, expression, block)

  override fun <T> visit(visitor: NodeVisitor<T>): T =
    visitor.visitForIn(this)

  override fun equals(other: Any?): Boolean {
    if (other !is ForIn) return false
    return other.symbol == symbol && other.expression == expression && other.block == block
  }

  override fun hashCode(): Int {
    var result = symbol.hashCode()
    result = 31 * result + expression.hashCode()
    result = 31 * result + block.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}
