package gay.pizza.pork.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("listLiteral")
class ListLiteral(val items: List<Expression>) : Expression() {
  override val type: NodeType = NodeType.ListLiteral

  override fun <T> visitChildren(visitor: NodeVisitor<T>): List<T> =
    visitor.visitAll(items)

  override fun equals(other: Any?): Boolean {
    if (other !is ListLiteral) return false
    return other.items == items
  }

  override fun hashCode(): Int {
    var result = items.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}