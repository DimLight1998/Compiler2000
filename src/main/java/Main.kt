import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.cli.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder


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
        val interactive = Option("i", "interact", false, "enable interactive mode")
        ops.addOption(interactive)
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


        if (!cmd.hasOption("i")) {
            val inputStream = if (inputFile == null) {
                System.`in`
            } else {
                FileInputStream(inputFile)
            }

            val dirFile = if (inputFile == null) {
                File(".")
            } else {
                File(inputFile).parentFile
            }
            val lexer = SimCLexer(ANTLRInputStream(inputStream))
            val tokenStream = CommonTokenStream(lexer)
            val parser = SimCParser(tokenStream)
            val tree = parser.compilationUnit()
            val codeGenVisitor = SimCCodeGenVisitor()
            codeGenVisitor.init()
            codeGenVisitor.visit(tree)
            codeGenVisitor.writeIRCodeTo(dirFile.resolve("$fileName.ll").path)
            codeGenVisitor.writeBitCodeTo(dirFile.resolve("$fileName.bc").path)
            codeGenVisitor.dispose()
            if (!cmd.hasOption("n")) {
                var process = Runtime.getRuntime().exec("llc -relocation-model=pic $fileName.bc", null, dirFile)
                var externalReader = BufferedReader(InputStreamReader(process.errorStream))
                while (true) {
                    val line: String = externalReader.readLine() ?: break
                    println(line)
                }
                process.waitFor()
                process = Runtime.getRuntime().exec("gcc $fileName.s -o $fileName", null, dirFile)
                externalReader = BufferedReader(InputStreamReader(process.errorStream))
                while (true) {
                    val line: String = externalReader.readLine() ?: break
                    println(line)
                }
                process.waitFor()
            }
        } else {
            val codeGenVisitor = SimCCodeGenVisitor()
            codeGenVisitor.init()
            codeGenVisitor.initRepl()
            var line: String?
            val buffer = StringBuilder()
            while (true) {
                if (buffer.isEmpty()) {
                    print("c2k> ")
                } else {
                    print("> ")
                }
                line = readLine()
                if (line == null) {
                    codeGenVisitor.dispose()
                    break
                } else if (line == "") {
                    try {
                        val lexer = SimCLexer(ANTLRInputStream(buffer.toString()))
                        val tokenStream = CommonTokenStream(lexer)
                        val parser = SimCParser(tokenStream)
                        val tree = parser.replEntrance()
                        codeGenVisitor.clearFunction()
                        codeGenVisitor.visit(tree)
                        if (codeGenVisitor.hasFunction()) {
                            println(codeGenVisitor.runOutput())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    buffer.clear()
                } else {
                    buffer.append(line)
                }
            }
        }
    }
}