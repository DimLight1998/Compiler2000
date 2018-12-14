import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.FileInputStream

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        var inputFile: String? = null
        if (args.isNotEmpty()) {
            inputFile = args[0]
        }
        val inputStream = if (inputFile == null) {
            System.`in`
        } else {
            FileInputStream(inputFile)
        }

        val lexer = SimCLexer(ANTLRInputStream(inputStream))
        val tokenStream = CommonTokenStream(lexer)
        val parser = SimCParser(tokenStream)
        val tree = parser.compilationUnit()
        val codeGenVisitor = SimCCodeGenVisitor()
        codeGenVisitor.visit(tree)
    }
}