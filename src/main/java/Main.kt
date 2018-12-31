import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.cli.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        val ops = Options()
        val outputName = Option("o", "output", true, "specify output executable name")
        ops.addOption(outputName)
        val noExecutable = Option("n", "no-exe", false, "do not generate executable")
        ops.addOption(noExecutable)
        val showHelp = Option("h", "help", false, "show this help information")
        ops.addOption(showHelp)
        val cmdParser = DefaultParser()
        val formatter = HelpFormatter()

        var inputFile: String? = null

        val cmd = cmdParser.parse(ops, args)

        if (cmd.hasOption("h")) {
            formatter.printHelp("c2k [options] [input file]", ops)
            return
        }

        var fileName = "a.out"
        if (cmd.hasOption("o")) {
            fileName = cmd.getOptionValue("o")
        }

        if (cmd.args.size > 1) {
            formatter.printHelp("c2k [options] [input file]", ops)
            throw ParseException("only 1 positional argument for input filename")
        }
        if (cmd.args.size == 1) {
            inputFile = cmd.args[0]
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
        if (!cmd.hasOption("n")) {
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
}