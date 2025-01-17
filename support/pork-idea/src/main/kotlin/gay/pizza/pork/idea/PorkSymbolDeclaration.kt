package gay.pizza.pork.idea

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import gay.pizza.pork.ast.NodeType

@Suppress("UnstableApiUsage")
class PorkSymbolDeclaration(val element: PsiElement) : PsiSymbolDeclaration {
  override fun getDeclaringElement(): PsiElement = element
  override fun getRangeInDeclaringElement(): TextRange {
    return getSymbolElement().textRange
  }

  override fun getSymbol(): Symbol {
    val element = getSymbolElement()
    return PorkFunctionSymbol(element.text.trim())
  }

  private fun getSymbolElement(): PsiElement {
    return element.children.first {
      it.elementType == PorkElementTypes.elementTypeFor(NodeType.Symbol)
    }
  }
}
