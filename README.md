# Compiler2000说明文档

## 实验环境

c2k使用ANTLR4作为词法和语法解析工具，LLVM为中间代码生成，将C语言翻译到LLVM IR上。我们实现了以下C程序用于测试：

- 回文检测
- KMP
- 四则运算计算

还有一些其他测试语法功能的C程序可以在`tests`文件夹找到。

- `testassign.c`测试连续赋值
- `testlogicbit.c`测试逻辑运算符是否能短路运算
- `testunaryop.c`测试自增减运算符
- `testvoidret.c`测试void函数和全局变量定义是否正常工作

## 使用说明

### 构建

安装llvm、maven、gcc、antlr。

在项目文件夹下使用mvn compile进行编译，maven会自动下载安装依赖库。

### 命令行使用

```bash
usage: c2k [options] [input file]
 -h,--help           show this help information
 -i,--interact       enable interactive mode
 -n,--no-exe         do not generate executable
 -o,--output <arg>   specify output executable name
```

`c2k a.c`可以将C源文件翻译成LLVM IR和LLVM bitcode，最终调用llc和gcc编译成可执行文件（需要环境变量中有llc和gcc，默认输出文件名为`a.out`）。

如果不指定输入文件名，c2k会从标准输入读取源程序，以EOF结束。

`-o`可以指定输出的文件名，输出目录是源文件所在的目录（如果从标准输入读入的话，输出目录是当前目录）。

`-n`可以只生成IR和bitcode，在没有安装llc或gcc的时候最好指定。

`-i`可以进入交互式命令行.

### 交互式命令行

一次可以输入多行，最后敲一下回车输入一个空行结束输入。

每次可以输入以下的类型：

- 函数定义（可以多行）
- 全局变量定义，如`int a;`，`int b[5];`
- 一个表达式，如`a+1`。可以调用函数和使用全局变量，注意结尾没有分号。结束输入后c2k会计算表达式的值并输出。

## 技术特色

### REPL

使用LLVM的ExecutionEngine中的解释器进行即时解释运行。将每个表达式封装成一个不带参数的匿名函数并立即执行，取返回值输出。

### 变量作用域

使用了一个“字典栈”，每个字典中存放当前作用域的变量。当一个变量被引用时从栈顶的字典挨个往下找，找到的第一个就是需要的变量，这样就解决了“变量作用域屏蔽”的问题。

### 变长参数处理

对`...`进行特判，对变长参数调用不同的API来支持变长参数的函数。

### 代码优化

调用LLVM的PassManager，对生成的中间码进行了逐函数和整体的优化。

## 支持的C语言特性

- 变量的定义，如`int a;`，`int a=0;`(只支持局部变量在定义时初始化)
- 变量的赋值
- 数组的定义和下标访问
- 支持定义函数和**声明外部函数**，比如调用`scanf`和`printf`
- 支持调用定长和**变长参数的函数**
- 支持字符串常量，支持**转义字符**
- 前置和后置的自增自减运算符
- 位运算符
- 逻辑运算符，支持**短路运算**
- 比较运算符
- 四则运算
- 支持的数据类型：`int，int*，char，char*，void`
- 各种控制和循环结构
- 支持跳过单行和多行注释
