package gay.pizza.pork.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path
import gay.pizza.pork.frontend.FileFrontend

class TokenizeCommand : CliktCommand(help = "Tokenize Program", name = "tokenize") {
  val path by argument("file").path(mustExist = true, canBeDir = false)

  override fun run() {
    val frontend = FileFrontend(path)
    val tokenStream = frontend.tokenize()
    for (token in tokenStream.tokens) {
      println("${token.start} ${token.type.name} '${sanitize(token.text)}'")
    }
  }

  private fun sanitize(input: String): String =
    input
      .replace("\n", "\\n")
      .replace("\r", "\\r")
}