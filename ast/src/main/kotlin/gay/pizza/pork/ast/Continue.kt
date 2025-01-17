// GENERATED CODE FROM PORK AST CODEGEN
package gay.pizza.pork.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("continue")
class Continue : Expression() {
  override val type: NodeType = NodeType.Continue

  override fun <T> visit(visitor: NodeVisitor<T>): T =
    visitor.visitContinue(this)

  override fun equals(other: Any?): Boolean {
    if (other !is Continue) return false
    return true
  }

  override fun hashCode(): Int =
    31 * type.hashCode()
}
