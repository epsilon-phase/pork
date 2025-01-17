// GENERATED CODE FROM PORK AST CODEGEN
package gay.pizza.pork.ast

import kotlinx.serialization.Transient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("node")
sealed class Node {
  abstract val type: NodeType

  @Transient
  var data: Any? = null

  open fun <T> visitChildren(visitor: NodeVisitor<T>): List<T> =
    emptyList()

  open fun <T> visit(visitor: NodeVisitor<T>): T =
    visitor.visit(this)
}
