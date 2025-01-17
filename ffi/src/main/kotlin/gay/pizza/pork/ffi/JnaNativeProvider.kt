package gay.pizza.pork.ffi

import com.sun.jna.Function
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import gay.pizza.pork.ast.ArgumentSpec
import gay.pizza.pork.evaluator.CallableFunction
import gay.pizza.pork.evaluator.NativeProvider
import gay.pizza.pork.evaluator.None

class JnaNativeProvider : NativeProvider {
  override fun provideNativeFunction(definition: String, arguments: List<ArgumentSpec>): CallableFunction {
    val functionDefinition = FfiFunctionDefinition.parse(definition)
    val library = NativeLibrary.getInstance(functionDefinition.library)
    val function = library.getFunction(functionDefinition.function)
      ?: throw RuntimeException("Failed to find function ${functionDefinition.function} in library ${functionDefinition.library}")
    return CallableFunction { functionArgs ->
      val ffiArgs = mutableListOf<Any?>()
      for ((index, spec) in arguments.withIndex()) {
        val ffiType = functionDefinition.parameters[index]
        if (spec.multiple) {
          val variableArguments = functionArgs.values
            .subList(index, functionArgs.values.size)
          ffiArgs.addAll(variableArguments)
          break
        } else {
          val converted = convert(ffiType, functionArgs.values[index])
          ffiArgs.add(converted)
        }
      }
      invoke(function, ffiArgs.toTypedArray(), functionDefinition.returnType)
    }
  }

  private fun invoke(function: Function, values: Array<Any?>, type: String): Any = when (rewriteType(type)) {
    "void*" -> function.invokePointer(values)
    "int" -> function.invokeInt(values)
    "long" -> function.invokeLong(values)
    "float" -> function.invokeFloat(values)
    "double" -> function.invokeDouble(values)
    "void" -> function.invokeVoid(values)
    "char*" -> function.invokeString(values, false)
    else -> throw RuntimeException("Unsupported ffi return type: $type")
  } ?: None

  private fun rewriteType(type: String): String = when (type) {
    "size_t" -> "long"
    else -> type
  }

  private fun convert(type: String, value: Any?): Any? = when (rewriteType(type)) {
    "short" -> numberConvert(type, value) { toShort() }
    "unsigned short" -> numberConvert(type, value) { toShort() }
    "int" -> numberConvert(type, value) { toInt() }
    "unsigned int" -> numberConvert(type, value) { toInt() }
    "long" -> numberConvert(type, value) { toLong() }
    "unsigned long" -> numberConvert(type, value) { toLong() }
    "double" -> numberConvert(type, value) { toDouble() }
    "float" -> numberConvert(type, value) { toFloat() }
    "char*" -> notNullConvert(type, value) { toString() }
    "void*" -> nullableConvert(value) {
      if (value is Long) {
        Pointer(value)
      } else {
        value as Pointer
      }
    }
    else -> throw RuntimeException("Unsupported ffi type: $type")
  }

  private fun <T> notNullConvert(type: String, value: Any?, into: Any.() -> T): T {
    if (value == null) {
      throw RuntimeException("Null values cannot be used for converting to type $type")
    }
    return into(value)
  }

  private fun <T> nullableConvert(value: Any?, into: Any.() -> T): T? {
    if (value == null || value == None) {
      return null
    }
    return into(value)
  }

  private fun <T> numberConvert(type: String, value: Any?, into: Number.() -> T): T {
    if (value == null || value == None) {
      throw RuntimeException("Null values cannot be used for converting to numeric type $type")
    }

    if (value !is Number) {
      throw RuntimeException("Cannot convert value '$value' into type $type")
    }
    return into(value)
  }
}
