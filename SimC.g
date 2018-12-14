grammar SimC;

arguments:
	expression					# headExpression
	| arguments ',' expression	# tailExpression;

expression:
	Identifier												# identifierExpr
	| Constant												# numericalConstantExpr
	| StringLiteral											# stringConstantExpr
	| '(' expression ')'									# parensExpr
	| expression '[' expression ']'							# arrayIndexerExpr
	| expression '(' arguments? ')'							# functionCallExpr
	| op = ('+' | '-' | '!') expression						# unaryOpExpr
	| expression op = ('*' | '/' | '%') expression			# mulDivExpr
	| expression op = ('+' | '-') expression				# addSubExpr
	| expression op = ('<<' | '>>') expression				# lshRshExpr
	| expression op = ('<' | '>' | '<=' | '>=') expression	# orderExpr
	| expression op = ('==' | '!=') expression				# equalityExpr
	| expression '&&' expression							# logicalAndExpr
	| expression '||' expression							# logicalOrExpr
	| expression '=' expression								# assignmentExpr;

declaration:
	typeSpecifier directDeclarator ';' # abstractDeclaration;

typeSpecifier:
	'char' '*'	# charPointerType
	| 'int' '*'	# intPointerType
	| 'void'	# voidType
	| 'char'	# charType
	| 'int'		# intType;

directDeclarator:
	Identifier										# variableDeclarator
	| directDeclarator '[' expression? ']'			# arrayDeclarator
	| directDeclarator '(' parameterTypeList? ')'	# functionPrototypeDeclarator;

parameterTypeList:
	parameterList				# simpleParameterList
	| parameterList ',' '...'	# variableParameterList;

parameterList:
	typeSpecifier directDeclarator						# headParameter
	| parameterList ',' typeSpecifier directDeclarator	# tailParameter;

statement:
	'{' blockItemList? '}'													# compoundStatement
	| expression? ';'														# simpleStatement
	| 'if' '(' expression ')' statement ('else' statement)?					# ifElseStatement
	| 'while' '(' expression ')' statement									# whileStatement
	| 'do' statement 'while' '(' expression ')' ';'							# doWhileStatement
	| 'for' '(' expression? ';' expression? ';' expression? ')' statement	# forStatement
	| 'return' expression? ';'												# returnStatement;

blockItemList:
	statement					# headStatement
	| declaration				# headDeclaration
	| blockItemList statement	# tailStatement
	| blockItemList declaration	# tailDeclaration;

compilationUnit: translationUnit? EOF # fullSource;

translationUnit:
	externalDeclaration						# headExternalDeclaration
	| translationUnit externalDeclaration	# tailExternalDeclaration;

externalDeclaration:
	functionDefinition	# externalFunctionDefinition
	| declaration		# externalNonFunctionDefinition;

functionDefinition:
	typeSpecifier directDeclarator '{' blockItemList? '}' # functionFullDefinition;

// LEXER
Identifier: [a-zA-Z_] ([a-zA-Z_0-9])*;
Constant: [0-9]+;
StringLiteral: '"' (~["\\\r\n] | '\\' ['"?abfnrtv\\])* '"';
Whitespace: [ \t]+ -> skip;
Newline: ( '\r' '\n'? | '\n') -> skip;
BlockComment: '/*' .*? '*/' -> skip;
LineComment: '//' ~[\r\n]* -> skip;