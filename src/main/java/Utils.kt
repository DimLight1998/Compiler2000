import org.bytedeco.javacpp.LLVM

fun <T> ArrayList<T>.push(element: T) {
    this.add(element)
}

fun <T> ArrayList<T>.pop(): T {
    return this.removeAt(this.size - 1)
}

fun <T> ArrayList<T>.top(): T {
    return this[this.size - 1]
}

class FunctionSignature(
        val paramsType: ArrayList<LLVM.LLVMTypeRef>,
        val paramsName: ArrayList<String>,
        val isVariable: Boolean,
        val returnType: LLVM.LLVMTypeRef,
        val functionName: String
)

/**
 * This function is pure.
 */
fun getLLVMType(typeSpecifierContext: SimCParser.TypeSpecifierContext): LLVM.LLVMTypeRef {
    return when (typeSpecifierContext.text) {
        "int" -> LLVM.LLVMInt32Type()
        "char" -> LLVM.LLVMInt8Type()
        "void" -> LLVM.LLVMVoidType()
        "int*" -> LLVM.LLVMPointerType(LLVM.LLVMInt32Type(), 0)
        "char*" -> LLVM.LLVMPointerType(LLVM.LLVMInt8Type(), 0)
        else -> throw Exception("unknown type")
    }
}

/**
 * This function is pure.
 */
fun getLLVMFunctionType(sig: SimCParser.FunctionSignatureHeaderContext): FunctionSignature {
    val paramsType = ArrayList<LLVM.LLVMTypeRef>()
    val paramsName = ArrayList<String>()

    val initParameterList: SimCParser.ParameterListContext?
    val isVariable: Boolean
    val returnType = getLLVMType(sig.typeSpecifier())
    val functionName = sig.Identifier().text

    // decide signature type
    val parameterTypeList = sig.parameterTypeList()
    when (parameterTypeList) {
        null -> {
            isVariable = false
            initParameterList = null
        }
        is SimCParser.SimpleParameterListContext -> {
            isVariable = false
            initParameterList = parameterTypeList.parameterList()
        }
        is SimCParser.VariableParameterListContext -> {
            isVariable = true
            initParameterList = parameterTypeList.parameterList()
        }
        else -> {
            throw Exception("unknown signature")
        }
    }

    // iterate through parameterTypeList
    if (initParameterList != null) {
        var parameterList = initParameterList
        while (parameterList is SimCParser.TailParameterContext) {
            paramsType.add(getLLVMType(parameterList.typeSpecifier()))
            paramsName.add(parameterList.Identifier().text)
            parameterList = parameterList.parameterList()
        }
        if (parameterList is SimCParser.HeadParameterContext) {
            paramsType.add(getLLVMType(parameterList.typeSpecifier()))
            paramsName.add(parameterList.Identifier().text)
        } else {
            throw Exception("unknown parameter list")
        }
    }

    // the grammar is left-recursive, so we need to reverse the list
    paramsType.reverse()
    paramsName.reverse()

    return FunctionSignature(paramsType, paramsName, isVariable, returnType, functionName)
}