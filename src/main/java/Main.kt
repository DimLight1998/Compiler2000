import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


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
        codeGenVisitor.initPassManagers()
        codeGenVisitor.visit(tree)
        codeGenVisitor.writeIRCodeTo(File("tests/ir.ll").path)
        codeGenVisitor.writeBitCodeTo(File("tests/bitCode.bc").path)
        codeGenVisitor.dispose()
        val dirFile = if (inputFile == null) {
            File(".")
        } else {
            File(inputFile).parentFile
        }
        val fileName = "output" // todo
        var process = Runtime.getRuntime().exec("llc -relocation-model=pic bitCode.bc", null, dirFile)
        var externalReader = BufferedReader(InputStreamReader(process.inputStream))
        while (true) {
            val line: String = externalReader.readLine() ?: break
            println(line)
        }
        process.waitFor()
        process = Runtime.getRuntime().exec("gcc bitCode.s -o $fileName", null, dirFile)
        externalReader = BufferedReader(InputStreamReader(process.inputStream))
        while (true) {
            val line: String = externalReader.readLine() ?: break
            println(line)
        }
        process.waitFor()
    }
}