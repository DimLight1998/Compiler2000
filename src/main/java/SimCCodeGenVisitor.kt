import org.bytedeco.javacpp.*
import org.bytedeco.javacpp.LLVM.*

class SimCCodeGenVisitor : SimCBaseVisitor<LLVMValueRef?>() {
    private val module = LLVMModuleCreateWithName("SimCModule")!!
    private val builder = LLVMCreateBuilder()!!
    private val namesChain = ArrayList<HashMap<String, LLVMValueRef>>()
    private val functionPassManager = LLVMCreateFunctionPassManagerForModule(module)!!
    private val passManager = LLVMCreatePassManager()!!
    private var currentBlockHasRet = false

    fun initPassManagers() {
        LLVMAddInstructionCombiningPass(functionPassManager)
        LLVMAddReassociatePass(functionPassManager)
        LLVMAddGVNPass(functionPassManager)
        LLVMAddCFGSimplificationPass(functionPassManager)
        LLVMInitializeFunctionPassManager(functionPassManager)

        LLVMAddGlobalDCEPass(passManager)
    }

    fun dispose() {
        LLVMDisposeBuilder(builder)
        LLVMDisposeModule(module)
        LLVMDisposePassManager(functionPassManager)
        LLVMDisposePassManager(passManager)
    }

    fun writeBitCodeTo(filePath: String) {
        LLVMWriteBitcodeToFile(module, filePath)
    }

    fun writeIRCodeTo(filePath: String) {
        LLVMPrintModuleToFile(module, filePath, null as ByteArray?)
    }

    private fun getVariablePointerByName(name: String): LLVMValueRef {
        var idx = namesChain.size - 1
        while (idx >= 0 && name !in namesChain[idx].keys) idx--
        if (idx == -1) throw Exception("unknown variable")
        return namesChain[idx][name]!!
    }

    override fun visitStringConstantExpr(ctx: SimCParser.StringConstantExprContext): LLVMValueRef? {
        val raw = ctx.text.substring(1, ctx.text.length - 1)
        val data = raw.replace("\\n", "\n").replace("\\r", "\r").replace("\\'", "\'")
                .replace("\\\"", "\"").replace("\\b", "\b").replace("\\t", "\t").replace("\\\\", "\\")
        return LLVMBuildGlobalStringPtr(builder, data, "global_string")
    }

    override fun visitAssignmentExpr(ctx: SimCParser.AssignmentExprContext): LLVMValueRef? {
        val rhs = visit(ctx.expression())

        val variable = getVariablePointerByName(ctx.Identifier().text)
        if (LLVMGetTypeKind(LLVMTypeOf(rhs)) == LLVMArrayTypeKind) {
            // ! this is a workaround
            val array = getVariablePointerByName(ctx.expression().text)
            val indices = arrayOf(LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), 0, 0))
            val indexed = LLVMBuildInBoundsGEP(builder, array, PointerPointer(*indices), 2, "arr_ptr")
            LLVMBuildStore(builder, indexed, variable)
        } else {
            LLVMBuildStore(builder, rhs, variable)
        }
        return rhs
    }

    override fun visitArrayAssignmentExpr(ctx: SimCParser.ArrayAssignmentExprContext): LLVMValueRef? {
        val rhs = visit(ctx.expression(1))
        val array = getVariablePointerByName(ctx.Identifier().text)

        // ! this ia a workaround
        val value = visit(ctx.expression(0))
        return if (LLVMGetTypeKind(LLVMGetElementType(LLVMTypeOf(array))) == LLVMArrayTypeKind) {
            val indices = arrayOf(LLVMConstInt(LLVMInt32Type(), 0, 0), value)
            val indexed = LLVMBuildInBoundsGEP(builder, array, PointerPointer(*indices), indices.size, "arr_ptr")
            LLVMBuildStore(builder, rhs, indexed)
            rhs
        } else {
            val indices = arrayOf(value)
            val variable = getVariablePointerByName(ctx.Identifier().text)
            val loaded = LLVMBuildLoad(builder, variable, "loaded")
            val indexed = LLVMBuildInBoundsGEP(builder, loaded, PointerPointer(*indices), indices.size, "arr_ptr")
            LLVMBuildStore(builder, rhs, indexed)
            rhs
        }
    }

    override fun visitOrderExpr(ctx: SimCParser.OrderExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        var rhs = visit(ctx.expression(1))

        lhs = LLVMBuildIntCast(builder, lhs, LLVMInt32Type(), "cast_lhs")
        rhs = LLVMBuildIntCast(builder, rhs, LLVMInt32Type(), "cast_rhs")
        return when (ctx.op.text) {
            "<" -> LLVMBuildICmp(builder, LLVMIntSLT, lhs, rhs, "cmp_tmp")
            ">" -> LLVMBuildICmp(builder, LLVMIntSGT, lhs, rhs, "cmp_tmp")
            "<=" -> LLVMBuildICmp(builder, LLVMIntSLE, lhs, rhs, "cmp_tmp")
            ">=" -> LLVMBuildICmp(builder, LLVMIntSGE, lhs, rhs, "cmp_tmp")
            else -> throw Exception("unknown op")
        }
    }

    override fun visitAddSubExpr(ctx: SimCParser.AddSubExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        var rhs = visit(ctx.expression(1))

        lhs = LLVMBuildIntCast(builder, lhs, LLVMInt32Type(), "cast_lhs")
        rhs = LLVMBuildIntCast(builder, rhs, LLVMInt32Type(), "cast_rhs")
        return when (ctx.op.text) {
            "+" -> LLVMBuildAdd(builder, lhs, rhs, "add_tmp")
            "-" -> LLVMBuildSub(builder, lhs, rhs, "sub_tmp")
            else -> throw Exception("unknown op")
        }
    }

    override fun visitLogicalAndExpr(ctx: SimCParser.LogicalAndExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        lhs = LLVMBuildICmp(builder, LLVMIntNE, lhs, LLVMConstInt(LLVMTypeOf(lhs), 0, 0), "to_i1")
        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))
        val positiveBB = LLVMAppendBasicBlock(function, "then")
        val negativeBB = LLVMAppendBasicBlock(function, "else")
        val mergeBB = LLVMAppendBasicBlock(function, "end_if")
        val retPointer = LLVMBuildAlloca(builder, LLVMInt32Type(), "logic_and_temp")
        if (!currentBlockHasRet) {
            LLVMBuildCondBr(builder, lhs, positiveBB, negativeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, positiveBB)

        var rhs = visit(ctx.expression(1))
        rhs = LLVMBuildICmp(builder, LLVMIntNE, rhs, LLVMConstInt(LLVMTypeOf(rhs), 0, 0), "to_i1")
        rhs = LLVMBuildSelect(builder, rhs, LLVMConstInt(LLVMInt32Type(), 1, 0), LLVMConstInt(LLVMInt32Type(), 0, 0), "select_rhs")
        LLVMBuildStore(builder, rhs, retPointer)

        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, mergeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, negativeBB)

        lhs = LLVMBuildSelect(builder, lhs, LLVMConstInt(LLVMInt32Type(), 1, 0), LLVMConstInt(LLVMInt32Type(), 0, 0), "select_lhs")
        LLVMBuildStore(builder, lhs, retPointer)

        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, mergeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, mergeBB)

        return LLVMBuildLoad(builder, retPointer, "ret_load")
    }


    override fun visitLogicalOrExpr(ctx: SimCParser.LogicalOrExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        lhs = LLVMBuildICmp(builder, LLVMIntNE, lhs, LLVMConstInt(LLVMTypeOf(lhs), 0, 0), "to_i1")
        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))
        val positiveBB = LLVMAppendBasicBlock(function, "then")
        val negativeBB = LLVMAppendBasicBlock(function, "else")
        val mergeBB = LLVMAppendBasicBlock(function, "end_if")
        val retPointer = LLVMBuildAlloca(builder, LLVMInt32Type(), "logic_or_temp")
        if (!currentBlockHasRet) {
            LLVMBuildCondBr(builder, lhs, positiveBB, negativeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, positiveBB)

        lhs = LLVMBuildSelect(builder, lhs, LLVMConstInt(LLVMInt32Type(), 1, 0), LLVMConstInt(LLVMInt32Type(), 0, 0), "select_lhs")
        LLVMBuildStore(builder, lhs, retPointer)

        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, mergeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, negativeBB)

        var rhs = visit(ctx.expression(1))
        rhs = LLVMBuildICmp(builder, LLVMIntNE, rhs, LLVMConstInt(LLVMTypeOf(rhs), 0, 0), "to_i1")
        rhs = LLVMBuildSelect(builder, rhs, LLVMConstInt(LLVMInt32Type(), 1, 0), LLVMConstInt(LLVMInt32Type(), 0, 0), "select_rhs")
        LLVMBuildStore(builder, rhs, retPointer)

        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, mergeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, mergeBB)

        return LLVMBuildLoad(builder, retPointer, "ret_load")
    }

    override fun visitBitAndExpr(ctx: SimCParser.BitAndExprContext): LLVMValueRef? {
        val lhs = visit(ctx.expression(0))
        val rhs = visit(ctx.expression(1))

        return LLVMBuildAnd(builder, lhs, rhs, "and_tmp")
    }

    override fun visitBitOrExpr(ctx: SimCParser.BitOrExprContext): LLVMValueRef? {
        val lhs = visit(ctx.expression(0))
        val rhs = visit(ctx.expression(1))

        return LLVMBuildOr(builder, lhs, rhs, "or_tmp")
    }

    override fun visitArrayIndexerExpr(ctx: SimCParser.ArrayIndexerExprContext): LLVMValueRef? {
        val array = getVariablePointerByName(ctx.Identifier().text)
        val value = visit(ctx.expression())!!
        val name = ctx.Identifier().text

        return if (LLVMGetTypeKind(LLVMGetElementType(LLVMTypeOf(array))) == LLVMArrayTypeKind) {
            val indices = arrayOf(LLVMConstInt(LLVMInt32Type(), 0, 0), value)
            val indexed = LLVMBuildInBoundsGEP(builder, array, PointerPointer(*indices), 2, "arr_ptr")
            LLVMBuildLoad(builder, indexed, "arr_${name}_load")
        } else {
            val indices = arrayOf(value)
            val variable = getVariablePointerByName(ctx.Identifier().text)
            val loaded = LLVMBuildLoad(builder, variable, "loaded")
            val indexed = LLVMBuildInBoundsGEP(builder, loaded, PointerPointer(*indices), indices.size, "arr_ptr")
            LLVMBuildLoad(builder, indexed, "ptr_${name}_load")
        }
    }

    override fun visitParensExpr(ctx: SimCParser.ParensExprContext): LLVMValueRef? {
        return visit(ctx.expression())
    }

    override fun visitNumericalConstantExpr(ctx: SimCParser.NumericalConstantExprContext): LLVMValueRef? {
        val value = ctx.Constant().text.toLong()
        return LLVMConstInt(LLVMInt32Type(), value, 1)
    }

    override fun visitUnaryOpExpr(ctx: SimCParser.UnaryOpExprContext): LLVMValueRef? {
        val operand = visit(ctx.expression())
        return when (ctx.op.text) {
            "+" -> operand
            "-" -> LLVMBuildNeg(builder, operand, "neg_tmp")
            "~" -> LLVMBuildNot(builder, operand, "not_tmp")
            "!" -> {
                val condition = LLVMBuildICmp(builder, LLVMIntNE, operand, LLVMConstInt(LLVMTypeOf(operand), 0, 0), "to_i1")
                LLVMBuildSelect(builder, condition, LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), 1, 0), "not_select")
            }
            else -> throw Exception("unknown op")
        }
    }

    override fun visitPrefixUnaryOpExpr(ctx: SimCParser.PrefixUnaryOpExprContext): LLVMValueRef? {
        val pointer = getVariablePointerByName(ctx.Identifier().text)
        val operand = LLVMBuildLoad(builder, pointer, "${ctx.Identifier().text}_load")
        return when (ctx.op.text) {
            "++" -> {
                val result = LLVMBuildAdd(builder, operand, LLVMConstInt(LLVMTypeOf(operand), 1, 1), "unary_add_tmp")
                LLVMBuildStore(builder, result, pointer)
                result
            }
            "--" -> {
                val result = LLVMBuildSub(builder, operand, LLVMConstInt(LLVMTypeOf(operand), 1, 1), "unary_sub_tmp")
                LLVMBuildStore(builder, result, pointer)
                result
            }
            else -> throw Exception("unknown op")
        }
    }

    override fun visitSuffixUnaryOpExpr(ctx: SimCParser.SuffixUnaryOpExprContext): LLVMValueRef? {
        val pointer = getVariablePointerByName(ctx.Identifier().text)
        val operand = LLVMBuildLoad(builder, pointer, "${ctx.Identifier().text}_load")
        return when (ctx.op.text) {
            "++" -> {
                val result = LLVMBuildAdd(builder, operand, LLVMConstInt(LLVMTypeOf(operand), 1, 1), "suffix_unary_add_tmp")
                LLVMBuildStore(builder, result, pointer)
                operand
            }
            "--" -> {
                val result = LLVMBuildSub(builder, operand, LLVMConstInt(LLVMTypeOf(operand), 1, 1), "suffix_unary_sub_tmp")
                LLVMBuildStore(builder, result, pointer)
                operand
            }
            else -> throw Exception("unknown op")
        }
    }

    override fun visitLshRshExpr(ctx: SimCParser.LshRshExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        var rhs = visit(ctx.expression(1))

        lhs = LLVMBuildIntCast(builder, lhs, LLVMInt32Type(), "cast_lhs")
        rhs = LLVMBuildIntCast(builder, rhs, LLVMInt32Type(), "cast_rhs")
        return when (ctx.op.text) {
            "<<" -> LLVMBuildShl(builder, lhs, rhs, "shl_tmp")
            ">>" -> LLVMBuildAShr(builder, lhs, rhs, "shr_tmp")
            else -> throw Exception("unknown op")
        }
    }

    override fun visitFunctionCallExpr(ctx: SimCParser.FunctionCallExprContext): LLVMValueRef? {
        val function = LLVMGetNamedFunction(module, ctx.Identifier().text)
        val returnType = LLVMGetReturnType(LLVMGetElementType(LLVMTypeOf(function)))

        // get parameters
        val parameters = ArrayList<LLVMValueRef>()
        var arguments = ctx.arguments()

        fun getNextParameter(expression: SimCParser.ExpressionContext?) {
            if (expression is SimCParser.IdentifierExprContext) {
                val name = expression.text
                val array = getVariablePointerByName(name)
                if (LLVMGetTypeKind(LLVMGetElementType(LLVMTypeOf(array))) == LLVMArrayTypeKind) {
                    val indices = arrayOf(
                            LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), 0, 0)
                    )
                    val indexed = LLVMBuildInBoundsGEP(
                            builder, array, PointerPointer(*indices), indices.size, "arr_ptr"
                    )
                    parameters.add(indexed)
                } else {
                    parameters.add(visit(expression)!!)
                }
            } else {
                parameters.add(visit(expression)!!)
            }
        }

        while (arguments != null) {
            when (arguments) {
                is SimCParser.HeadExpressionContext -> {
                    val expression = arguments.expression()
                    getNextParameter(expression)
                    arguments = null
                }
                is SimCParser.TailExpressionContext -> {
                    val expression = arguments.expression()
                    getNextParameter(expression)
                    arguments = arguments.arguments()
                }
            }
        }

        // the grammar is left recursive, so reverse the list
        return LLVMBuildCall(builder, function, PointerPointer(*parameters.reversed().toTypedArray()),
                parameters.size, if (returnType == LLVMVoidType()) "" else "${ctx.Identifier().text}_result"
        )
    }

    override fun visitMulDivExpr(ctx: SimCParser.MulDivExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        var rhs = visit(ctx.expression(1))

        lhs = LLVMBuildIntCast(builder, lhs, LLVMInt32Type(), "cast_lhs")
        rhs = LLVMBuildIntCast(builder, rhs, LLVMInt32Type(), "cast_rhs")
        return when (ctx.op.text) {
            "*" -> LLVMBuildMul(builder, lhs, rhs, "mul_tmp")
            "/" -> LLVMBuildSDiv(builder, lhs, rhs, "s_div_tmp")
            "%" -> LLVMBuildSRem(builder, lhs, rhs, "s_rem_tmp")
            else -> throw Exception("unknown op")
        }
    }

    override fun visitEqualityExpr(ctx: SimCParser.EqualityExprContext): LLVMValueRef? {
        var lhs = visit(ctx.expression(0))
        var rhs = visit(ctx.expression(1))

        lhs = LLVMBuildIntCast(builder, lhs, LLVMInt32Type(), "cast_lhs")
        rhs = LLVMBuildIntCast(builder, rhs, LLVMInt32Type(), "cast_rhs")
        return when (ctx.op.text) {
            "==" -> LLVMBuildICmp(builder, LLVMIntEQ, lhs, rhs, "cmp_tmp")
            "!=" -> LLVMBuildICmp(builder, LLVMIntNE, lhs, rhs, "cmp_tmp")
            else -> throw Exception("unknown op")
        }
    }

    override fun visitIdentifierExpr(ctx: SimCParser.IdentifierExprContext): LLVMValueRef? {
        val pointer = getVariablePointerByName(ctx.Identifier().text)
        return LLVMBuildLoad(builder, pointer, "${ctx.Identifier().text}_load")
    }

    override fun visitVariableDeclaration(ctx: SimCParser.VariableDeclarationContext): LLVMValueRef? {
        val type = getLLVMType(ctx.typeSpecifier())
        val name = ctx.Identifier().text
        if (name in namesChain.top().keys)
            throw Exception("variable redefined")

        val pointer = LLVMBuildAlloca(builder, type, name)
        LLVMBuildStore(builder, LLVMConstNull(type), pointer)
        namesChain.top()[name] = pointer
        return null
    }

    override fun visitVariableInitializeDeclaration(ctx: SimCParser.VariableInitializeDeclarationContext): LLVMValueRef? {
        val type = getLLVMType(ctx.typeSpecifier())
        val name = ctx.Identifier().text
        if (name in namesChain.top().keys)
            throw Exception("variable redefined")
        val expression = visit(ctx.expression())
        val pointer = LLVMBuildAlloca(builder, type, name)
        LLVMBuildStore(builder, expression, pointer)
        namesChain.top()[name] = pointer
        return null
    }

    override fun visitArrayDeclaration(ctx: SimCParser.ArrayDeclarationContext): LLVMValueRef? {
        val baseType = getLLVMType(ctx.typeSpecifier())
        val name = ctx.Identifier().text
        val size = ctx.Constant().text.toInt()
        val arrayType = LLVMArrayType(baseType, size)
        if (name in namesChain.top().keys)
            throw Exception("variable redefined")

        val pointer = LLVMBuildAlloca(builder, arrayType, name)
        namesChain.top()[name] = pointer

        return null
    }

    override fun visitFunctionDeclaration(ctx: SimCParser.FunctionDeclarationContext): LLVMValueRef? {
        return visit(ctx.functionSignature())
    }

    override fun visitFunctionSignatureHeader(ctx: SimCParser.FunctionSignatureHeaderContext): LLVMValueRef? {
        // for function signature we need to add a function
        val functionSignature = getLLVMFunctionType(ctx)

        val functionType = LLVMFunctionType(
                functionSignature.returnType,
                PointerPointer(*(functionSignature.paramsType.toTypedArray())),
                functionSignature.paramsType.size,
                if (functionSignature.isVariable) 1 else 0
        )
        val function = LLVMAddFunction(module, functionSignature.functionName, functionType)
        LLVMSetLinkage(function, LLVMExternalLinkage)

        // set params name
        for (i in 0 until functionSignature.paramsName.size) {
            val paramName = functionSignature.paramsName[i]
            val param = LLVMGetParam(function, i)
            LLVMSetValueName(param, paramName)
        }

        return function
    }

    override fun visitCompoundStatement(ctx: SimCParser.CompoundStatementContext): LLVMValueRef? {
        namesChain.push(HashMap())
        visitChildren(ctx)
        namesChain.pop()
        return null
    }

    override fun visitIfElseStatement(ctx: SimCParser.IfElseStatementContext): LLVMValueRef? {
        var condition = visit(ctx.expression())
        condition = LLVMBuildICmp(builder, LLVMIntNE, condition, LLVMConstInt(LLVMTypeOf(condition), 0, 0), "to_i1")

        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))

        val positiveBB = LLVMAppendBasicBlock(function, "then")
        val negativeBB = LLVMAppendBasicBlock(function, "else")
        val mergeBB = LLVMAppendBasicBlock(function, "end_if")
        if (!currentBlockHasRet) {
            LLVMBuildCondBr(builder, condition, positiveBB, negativeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, positiveBB)
        visit(ctx.statement(0))
        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, mergeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, negativeBB)
        if (ctx.statement(1) != null)
            visit(ctx.statement(1))
        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, mergeBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, mergeBB)
        return null
    }

    override fun visitWhileStatement(ctx: SimCParser.WhileStatementContext): LLVMValueRef? {
        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))

        val checkBB = LLVMAppendBasicBlock(function, "condition_check")
        val bodyBB = LLVMAppendBasicBlock(function, "loop_body")
        val endWhileBB = LLVMAppendBasicBlock(function, "end_while")

        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, checkBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, checkBB)
        var condition = visit(ctx.expression())
        condition = LLVMBuildICmp(builder, LLVMIntNE, condition, LLVMConstInt(LLVMTypeOf(condition), 0, 0), "to_i1")

        if (!currentBlockHasRet) {
            LLVMBuildCondBr(builder, condition, bodyBB, endWhileBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, bodyBB)
        visit(ctx.statement())
        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, checkBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, endWhileBB)
        return null
    }

    override fun visitDoWhileStatement(ctx: SimCParser.DoWhileStatementContext): LLVMValueRef? {
        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))

        val bodyBB = LLVMAppendBasicBlock(function, "loop_body")
        val endWhileBB = LLVMAppendBasicBlock(function, "end_while")

        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, bodyBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, bodyBB)
        visit(ctx.statement())
        var condition = visit(ctx.expression())
        condition = LLVMBuildICmp(builder, LLVMIntNE, condition, LLVMConstInt(LLVMTypeOf(condition), 0, 0), "to_i1")

        if (!currentBlockHasRet) {
            LLVMBuildCondBr(builder, condition, bodyBB, endWhileBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, endWhileBB)
        return null
    }

    override fun visitForStatement(ctx: SimCParser.ForStatementContext): LLVMValueRef? {
        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))

        val checkBB = LLVMAppendBasicBlock(function, "condition_check")
        val bodyBB = LLVMAppendBasicBlock(function, "loop_body")
        val endForBB = LLVMAppendBasicBlock(function, "end_for")

        if (ctx.expression(0) != null)
            visit(ctx.expression(0))
        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, checkBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, checkBB)
        var condition = if (ctx.expression(1) != null) {
            visit(ctx.expression(1))
        } else {
            LLVMConstInt(LLVMInt32Type(), 1, 0)
        }
        condition = LLVMBuildICmp(builder, LLVMIntNE, condition, LLVMConstInt(LLVMTypeOf(condition), 0, 0), "to_i1")

        if (!currentBlockHasRet) {
            LLVMBuildCondBr(builder, condition, bodyBB, endForBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, bodyBB)
        visit(ctx.statement())
        if (ctx.expression(2) != null)
            visit(ctx.expression(2))
        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, checkBB)
        }

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, endForBB)
        return null
    }

    override fun visitReturnStatement(ctx: SimCParser.ReturnStatementContext): LLVMValueRef? {
        currentBlockHasRet = true
        if (ctx.expression() == null)
            return LLVMBuildRetVoid(builder)
        val retVal = visit(ctx.expression())
        return LLVMBuildRet(builder, retVal)
    }

    override fun visitExternalNonFunctionDefinition(
            ctx: SimCParser.ExternalNonFunctionDefinitionContext
    ): LLVMValueRef? {
        // here we handle global variables declaration

        val declaration = ctx.declaration()
        when (declaration) {
            is SimCParser.VariableDeclarationContext -> {
                val type = getLLVMType(declaration.typeSpecifier())
                val name = declaration.Identifier().text
                val pointer = LLVMAddGlobal(module, type, name)
                LLVMSetLinkage(pointer, LLVMCommonLinkage)
                LLVMSetInitializer(pointer, LLVMConstNull(type))
                namesChain.top()[name] = pointer
            }
            is SimCParser.ArrayDeclarationContext -> {
                val baseType = getLLVMType(declaration.typeSpecifier())
                val name = declaration.Identifier().text
                val size = declaration.Constant().text.toInt()
                val arrayType = LLVMArrayType(baseType, size)
                val array = LLVMAddGlobal(module, arrayType, name)
                LLVMSetLinkage(array, LLVMCommonLinkage)
                LLVMSetInitializer(array, LLVMConstNull(arrayType))
                namesChain.top()[name] = array
            }
            else -> {
                visit(ctx.declaration())
            }
        }
        return null
    }

    override fun visitFullSource(ctx: SimCParser.FullSourceContext?): LLVMValueRef? {
        // global naming space
        namesChain.push(HashMap())
        val ret = visitChildren(ctx)
        LLVMRunPassManager(passManager, module)
        return ret
    }

    override fun visitFunctionFullDefinition(ctx: SimCParser.FunctionFullDefinitionContext): LLVMValueRef? {
        val signature = ctx.functionSignature()
        val functionType = getLLVMFunctionType(signature as SimCParser.FunctionSignatureHeaderContext)
        val function = visit(signature)

        // this is a definition, should set up a names chain part
        val namesChainPart = HashMap<String, LLVMValueRef>()

        namesChain.push(namesChainPart)

        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, LLVMAppendBasicBlock(function, "entry"))
        val numParams = LLVMCountParams(function)

        for (i in 0 until numParams) {
            val param = LLVMGetParam(function, i)
            val paramName = functionType.paramsName[i]
            val paramType = functionType.paramsType[i]
            val ptr = LLVMBuildAlloca(builder, paramType, paramName)
            LLVMBuildStore(builder, param, ptr)
            namesChainPart[paramName] = ptr
        }

        val startBB = LLVMAppendBasicBlock(function, "start")
        if (!currentBlockHasRet) {
            LLVMBuildBr(builder, startBB)
        }
        currentBlockHasRet = false
        LLVMPositionBuilderAtEnd(builder, startBB)

        if (ctx.blockItemList() != null)
            visit(ctx.blockItemList())

        namesChain.pop()

        if (!currentBlockHasRet) {
            val type = getLLVMType(signature.typeSpecifier())
            if (type == LLVMVoidType()) {
                LLVMBuildRetVoid(builder)
            } else {
                LLVMBuildRet(builder, LLVMConstNull(type))
            }
            currentBlockHasRet = true
        }

        LLVMRunFunctionPassManager(functionPassManager, function)
        LLVMVerifyFunction(function, LLVMPrintMessageAction)
        return function
    }
}