import org.bytedeco.javacpp.*
import org.bytedeco.javacpp.LLVM.*

class SimCCodeGenVisitor : SimCBaseVisitor<LLVMValueRef?>() {
    override fun visitTailExpression(ctx: SimCParser.TailExpressionContext): LLVMValueRef? {
        return super.visitTailExpression(ctx)
    }

    override fun visitHeadExpression(ctx: SimCParser.HeadExpressionContext): LLVMValueRef? {
        return super.visitHeadExpression(ctx)
    }

    override fun visitStringConstantExpr(ctx: SimCParser.StringConstantExprContext): LLVMValueRef? {
        return super.visitStringConstantExpr(ctx)
    }

    override fun visitAssignmentExpr(ctx: SimCParser.AssignmentExprContext): LLVMValueRef? {
        return super.visitAssignmentExpr(ctx)
    }

    override fun visitOrderExpr(ctx: SimCParser.OrderExprContext): LLVMValueRef? {
        return super.visitOrderExpr(ctx)
    }

    override fun visitAddSubExpr(ctx: SimCParser.AddSubExprContext): LLVMValueRef? {
        return super.visitAddSubExpr(ctx)
    }

    override fun visitLogicalAndExpr(ctx: SimCParser.LogicalAndExprContext): LLVMValueRef? {
        return super.visitLogicalAndExpr(ctx)
    }

    override fun visitArrayIndexerExpr(ctx: SimCParser.ArrayIndexerExprContext): LLVMValueRef? {
        return super.visitArrayIndexerExpr(ctx)
    }

    override fun visitParensExpr(ctx: SimCParser.ParensExprContext): LLVMValueRef? {
        return super.visitParensExpr(ctx)
    }

    override fun visitNumericalConstantExpr(ctx: SimCParser.NumericalConstantExprContext): LLVMValueRef? {
        println(ctx.Constant().text)
        return super.visitNumericalConstantExpr(ctx)
    }

    override fun visitLogicalOrExpr(ctx: SimCParser.LogicalOrExprContext): LLVMValueRef? {
        return super.visitLogicalOrExpr(ctx)
    }

    override fun visitUnaryOpExpr(ctx: SimCParser.UnaryOpExprContext): LLVMValueRef? {
        return super.visitUnaryOpExpr(ctx)
    }

    override fun visitLshRshExpr(ctx: SimCParser.LshRshExprContext): LLVMValueRef? {
        return super.visitLshRshExpr(ctx)
    }

    override fun visitFunctionCallExpr(ctx: SimCParser.FunctionCallExprContext): LLVMValueRef? {
        return super.visitFunctionCallExpr(ctx)
    }

    override fun visitMulDivExpr(ctx: SimCParser.MulDivExprContext): LLVMValueRef? {
        return super.visitMulDivExpr(ctx)
    }

    override fun visitEqualityExpr(ctx: SimCParser.EqualityExprContext): LLVMValueRef? {
        return super.visitEqualityExpr(ctx)
    }

    override fun visitIdentifierExpr(ctx: SimCParser.IdentifierExprContext): LLVMValueRef? {
        return super.visitIdentifierExpr(ctx)
    }

    override fun visitAbstractDeclaration(ctx: SimCParser.AbstractDeclarationContext): LLVMValueRef? {
        return super.visitAbstractDeclaration(ctx)
    }

    override fun visitCharPointerType(ctx: SimCParser.CharPointerTypeContext): LLVMValueRef? {
        return super.visitCharPointerType(ctx)
    }

    override fun visitIntPointerType(ctx: SimCParser.IntPointerTypeContext): LLVMValueRef? {
        return super.visitIntPointerType(ctx)
    }

    override fun visitVoidType(ctx: SimCParser.VoidTypeContext): LLVMValueRef? {
        return super.visitVoidType(ctx)
    }

    override fun visitCharType(ctx: SimCParser.CharTypeContext): LLVMValueRef? {
        return super.visitCharType(ctx)
    }

    override fun visitIntType(ctx: SimCParser.IntTypeContext): LLVMValueRef? {
        return super.visitIntType(ctx)
    }

    override fun visitVariableDeclarator(ctx: SimCParser.VariableDeclaratorContext): LLVMValueRef? {
        return super.visitVariableDeclarator(ctx)
    }

    override fun visitArrayDeclarator(ctx: SimCParser.ArrayDeclaratorContext): LLVMValueRef? {
        return super.visitArrayDeclarator(ctx)
    }

    override fun visitFunctionPrototypeDeclarator(ctx: SimCParser.FunctionPrototypeDeclaratorContext): LLVMValueRef? {
        return super.visitFunctionPrototypeDeclarator(ctx)
    }

    override fun visitSimpleParameterList(ctx: SimCParser.SimpleParameterListContext): LLVMValueRef? {
        return super.visitSimpleParameterList(ctx)
    }

    override fun visitVariableParameterList(ctx: SimCParser.VariableParameterListContext): LLVMValueRef? {
        return super.visitVariableParameterList(ctx)
    }

    override fun visitHeadParameter(ctx: SimCParser.HeadParameterContext): LLVMValueRef? {
        return super.visitHeadParameter(ctx)
    }

    override fun visitTailParameter(ctx: SimCParser.TailParameterContext): LLVMValueRef? {
        return super.visitTailParameter(ctx)
    }

    override fun visitCompoundStatement(ctx: SimCParser.CompoundStatementContext): LLVMValueRef? {
        return super.visitCompoundStatement(ctx)
    }

    override fun visitSimpleStatement(ctx: SimCParser.SimpleStatementContext): LLVMValueRef? {
        return super.visitSimpleStatement(ctx)
    }

    override fun visitIfElseStatement(ctx: SimCParser.IfElseStatementContext): LLVMValueRef? {
        return super.visitIfElseStatement(ctx)
    }

    override fun visitWhileStatement(ctx: SimCParser.WhileStatementContext): LLVMValueRef? {
        return super.visitWhileStatement(ctx)
    }

    override fun visitDoWhileStatement(ctx: SimCParser.DoWhileStatementContext): LLVMValueRef? {
        return super.visitDoWhileStatement(ctx)
    }

    override fun visitForStatement(ctx: SimCParser.ForStatementContext): LLVMValueRef? {
        return super.visitForStatement(ctx)
    }

    override fun visitReturnStatement(ctx: SimCParser.ReturnStatementContext): LLVMValueRef? {
        return super.visitReturnStatement(ctx)
    }

    override fun visitHeadDeclaration(ctx: SimCParser.HeadDeclarationContext): LLVMValueRef? {
        return super.visitHeadDeclaration(ctx)
    }

    override fun visitTailDeclaration(ctx: SimCParser.TailDeclarationContext): LLVMValueRef? {
        return super.visitTailDeclaration(ctx)
    }

    override fun visitHeadStatement(ctx: SimCParser.HeadStatementContext): LLVMValueRef? {
        return super.visitHeadStatement(ctx)
    }

    override fun visitTailStatement(ctx: SimCParser.TailStatementContext): LLVMValueRef? {
        return super.visitTailStatement(ctx)
    }

    override fun visitFullSource(ctx: SimCParser.FullSourceContext): LLVMValueRef? {
        return super.visitFullSource(ctx)
    }

    override fun visitTailExternalDeclaration(ctx: SimCParser.TailExternalDeclarationContext): LLVMValueRef? {
        return super.visitTailExternalDeclaration(ctx)
    }

    override fun visitHeadExternalDeclaration(ctx: SimCParser.HeadExternalDeclarationContext): LLVMValueRef? {
        return super.visitHeadExternalDeclaration(ctx)
    }

    override fun visitExternalFunctionDefinition(ctx: SimCParser.ExternalFunctionDefinitionContext): LLVMValueRef? {
        return super.visitExternalFunctionDefinition(ctx)
    }

    override fun visitExternalNonFunctionDefinition(ctx: SimCParser.ExternalNonFunctionDefinitionContext): LLVMValueRef? {
        return super.visitExternalNonFunctionDefinition(ctx)
    }

    override fun visitFunctionFullDefinition(ctx: SimCParser.FunctionFullDefinitionContext): LLVMValueRef? {
        return super.visitFunctionFullDefinition(ctx)
    }
}