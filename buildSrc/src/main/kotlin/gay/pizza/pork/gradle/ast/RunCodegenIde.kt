package gay.pizza.pork.gradle.ast

import kotlin.io.path.Path

object RunCodegenIde {
  @JvmStatic
  fun main(args: Array<String>) {
    AstCodegen.run(
      pkg = "gay.pizza.pork.gen",
      astDescriptionFile = Path("src/main/ast/pork.yml"),
      outputDirectory = Path("src/main/kotlin/gay/pizza/pork/gen")
    )
  }
}
