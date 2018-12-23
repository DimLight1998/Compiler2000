grammar SimC;

arguments:
    expression                 # headExpression
    | arguments ',' expression # tailExpression;

expression:
    Identifier                                                     # identifierExpr
    | Constant                                                     # numericalConstantExpr
    | StringLiteral                                                # stringConstantExpr
    | '(' expression ')'                                           # parensExpr
    | Identifier '[' expression ']'                                # arrayIndexerExpr
    | Identifier '(' arguments? ')'                                # functionCallExpr
    | Identifier op = ('++' | '--')                                # suffixUnaryOpExpr
    | op = ('++' | '--') Identifier                                # prefixUnaryOpExpr
    | op = ('+' | '-' | '!') expression                            # unaryOpExpr
    | expression op = ('*' | '/' | '%') expression                 # mulDivExpr
    | expression op = ('+' | '-') expression                       # addSubExpr
    | expression op = ('<<' | '>>') expression                     # lshRshExpr
    | expression op = ('<' | '>' | '<=' | '>=') expression         # orderExpr
    | expression op = ('==' | '!=') expression                     # equalityExpr
    | expression '&&' expression                                   # logicalAndExpr
    | expression '||' expression                                   # logicalOrExpr
    | <assoc = right> Identifier '=' expression                    # assignmentExpr
    | <assoc = right> Identifier '[' expression ']' '=' expression # arrayAssignmentExpr;

declaration:
    typeSpecifier Identifier ';'                    # variableDeclaration
    | typeSpecifier Identifier '=' expression ';'   # variableInitializeDeclaration
    | typeSpecifier Identifier '[' Constant ']' ';' # arrayDeclaration
    | functionSignature ';'                         # functionDeclaration;

functionSignature:
    typeSpecifier Identifier '(' parameterTypeList? ')' # functionSignatureHeader;

typeSpecifier:
    'char' '*'  # charPointerType
    | 'int' '*' # intPointerType
    | 'void'    # voidType
    | 'char'    # charType
    | 'int'     # intType;

parameterTypeList:
    parameterList             # simpleParameterList
    | parameterList ',' '...' # variableParameterList;

parameterList:
    typeSpecifier Identifier                     # headParameter
    | parameterList ',' typeSpecifier Identifier # tailParameter;

statement:
    '{' blockItemList? '}'                                                # compoundStatement
    | expression? ';'                                                     # simpleStatement
    | 'if' '(' expression ')' statement ('else' statement)?               # ifElseStatement
    | 'while' '(' expression ')' statement                                # whileStatement
    | 'do' statement 'while' '(' expression ')' ';'                       # doWhileStatement
    | 'for' '(' expression? ';' expression? ';' expression? ')' statement # forStatement
    | 'return' expression? ';'                                            # returnStatement;

blockItemList:
    statement                   # headStatement
    | declaration               # headDeclaration
    | blockItemList statement   # tailStatement
    | blockItemList declaration # tailDeclaration;

compilationUnit: translationUnit? EOF # fullSource;

translationUnit:
    externalDeclaration                   # headExternalDeclaration
    | translationUnit externalDeclaration # tailExternalDeclaration;

externalDeclaration:
    functionDefinition # externalFunctionDefinition
    | declaration      # externalNonFunctionDefinition;

functionDefinition:
    functionSignature '{' blockItemList? '}' # functionFullDefinition;

// LEXER
Identifier: [a-zA-Z_] ([a-zA-Z_0-9])*;
Constant: [0-9]+;
StringLiteral: '"' (~["\\\r\n] | '\\' ['"?abfnrtv\\])* '"';
Whitespace: [ \t]+ -> skip;
Newline: ( '\r' '\n'? | '\n') -> skip;
BlockComment: '/*' .*? '*/' -> skip;
LineComment: '//' ~[\r\n]* -> skip;