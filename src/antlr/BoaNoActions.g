grammar BoaNoActions;

start
	: program EOF
	;

program
	: (
		declaration
		| statement
	  )+
	;

declaration
	: typeDeclaration
	| staticVariableDeclaration
	| variableDeclaration
	;

typeDeclaration
	: TYPE identifier EQUALS type SEMICOLON
// FIXME this would be nice, but seems to make a ton of extra error messages
//	| TYPE identifier EQUALS type
	;

staticVariableDeclaration
	: STATIC variableDeclaration
	;

variableDeclaration
	: forVariableDeclaration SEMICOLON
	| forVariableDeclaration           { notifyErrorListeners("error: ';' expected"); }
	;

type 
	: arrayType
	| mapType
	| tupleType
	| outputType
	| functionType
	| visitorType
	| stackType
	| setType
	| identifier
	;

component
	: (identifier COLON)? type
	;

arrayType
	: ARRAY OF component
	;

tupleType
	: LBRACE (member (COMMA member)* COMMA?)? RBRACE
	;

member
	: typeDeclaration
	| staticVariableDeclaration
	| component
	;

mapType
	: MAP LBRACKET component RBRACKET OF component
	;

stackType
	: STACK OF component
	;

setType
	: SET OF component
	;

outputType
	: OUTPUT (identifier | SET) (LPAREN expressionList RPAREN)? (LBRACKET component RBRACKET)* OF component (WEIGHT component)? (FORMAT LPAREN expressionList RPAREN)?
	;

functionType
	: FUNCTION LPAREN (identifier COLON type (COMMA identifier COLON type)*)? RPAREN (COLON type)?
	;

visitorType
	: VISITOR
	;

statement
	: block
	| assignmentStatement
	| breakStatement
	| continueStatement
	| stopStatement
	| doStatement
	| forStatement
	| ifStatement
	| resultStatement
	| returnStatement
	| switchStatement
	| foreachStatement
	| existsStatement
	| ifallStatement
	| whileStatement
	| emptyStatement
	| emitStatement
	| expressionStatement
	;

emptyStatement
	: SEMICOLON
	;

assignmentStatement
	: factor EQUALS expression SEMICOLON
	| factor EQUALS expression           { notifyErrorListeners("error: ';' expected"); }
	;

block
	: LBRACE (declaration | statement)* RBRACE
	;

breakStatement
	: BREAK SEMICOLON
	| BREAK           { notifyErrorListeners("error: ';' expected"); }
	;

continueStatement
	: CONTINUE SEMICOLON
	| CONTINUE           { notifyErrorListeners("error: ';' expected"); }
	;

doStatement
	: DO statement WHILE LPAREN expression RPAREN SEMICOLON
	| DO statement WHILE LPAREN expression RPAREN           { notifyErrorListeners("error: ';' expected"); }
	;

emitStatement
	: identifier (LBRACKET expression RBRACKET)* EMIT expression (WEIGHT expression)? SEMICOLON
	| identifier (LBRACKET expression RBRACKET)* EMIT expression (WEIGHT expression)?           { notifyErrorListeners("error: ';' expected"); }
	;

forStatement
	: FOR LPAREN (forExpression)? SEMICOLON (expression)? SEMICOLON (forExpression)? RPAREN statement
	;

forExpression
	: forVariableDeclaration
	| forExpressionStatement
	;

forVariableDeclaration
	: identifier COLON (type)? (EQUALS expression)?
	;

forExpressionStatement
	: expression (INCR | DECR) # postfixStatement
	| expression               # exprStatement
	;

expressionStatement
	: forExpressionStatement SEMICOLON
	| forExpressionStatement           { notifyErrorListeners("error: ';' expected"); }
	;

ifStatement
	: IF LPAREN expression RPAREN statement (ELSE statement)?
	;

resultStatement
	: RESULT expression SEMICOLON
	| RESULT expression           { notifyErrorListeners("error: ';' expected"); }
	;

returnStatement
	: RETURN expression? SEMICOLON
	| RETURN expression?           { notifyErrorListeners("error: ';' expected"); }
	;

switchStatement
	: SWITCH LPAREN expression RPAREN LBRACE
		(switchCase)*
		DEFAULT COLON
		(statement)+
		RBRACE
	;

switchCase
	: CASE expressionList COLON (statement)+
	;

foreachStatement
	: FOREACH LPAREN identifier COLON type SEMICOLON expression RPAREN statement
	;

existsStatement
	: EXISTS LPAREN identifier COLON type SEMICOLON expression RPAREN statement
	;

ifallStatement
	: IFALL LPAREN identifier COLON type SEMICOLON expression RPAREN statement
	;

whileStatement
	: WHILE LPAREN expression RPAREN statement
	;

visitStatement
	: (BEFORE | AFTER)
		(
			  WILDCARD
			| identifier COLON identifier
			| identifier (COMMA identifier)*
		)
		RIGHT_ARROW (declaration | statement)
	;

stopStatement
	: STOP SEMICOLON
	| STOP           { notifyErrorListeners("error: ';' expected"); }
	;

expression
	: conjunction ((TWOOR | OR) conjunction)*
	;

expressionList
	: expression (COMMA expression)*
	| expression (expression { notifyErrorListeners("error: ',' expected"); } | COMMA expression)*
	;

conjunction
	: comparison ((TWOAND | AND) comparison)*
	;

comparison
	: simpleExpression ((EQEQ | NEQ | LT | LTEQ | GT | GTEQ) simpleExpression)?
	;

simpleExpression
	: term ((PLUS | MINUS | ONEOR | XOR) term)*
	;

term
	: factor ((STAR | DIV | MOD | EMIT | RSHIFT | ONEAND) factor)*
	;

factor
	: operand (selector | index | call)*
	;

selector
	: DOT identifier
	;

index
	: LBRACKET expression (COLON expression)? RBRACKET
	;

call
	: LPAREN (expressionList)? RPAREN
	;

operand
	: stringLiteral
	| characterLiteral
	| timeLiteral
	| integerLiteral
	| floatingPointLiteral
	| composite
	| functionExpression
	| visitorExpression
	| unaryFactor
	| DOLLAR
	| statementExpression
	| parenExpression
	| identifier
	;

unaryFactor
	: (PLUS | MINUS | NEG | INV | NOT) factor
	;

parenExpression
	: LPAREN expression RPAREN
	;

functionExpression
	: functionType block
	;

visitorExpression
	: visitorType LBRACE (visitStatement)+ RBRACE
	| visitorType LBRACE (statement { notifyErrorListeners("error: only 'before' and 'after' visit statements allowed inside visitor bodies"); } | visitStatement)+ RBRACE
	;

statementExpression
	: QUESTION block
	;

composite
	: LBRACE (expressionList | pair (COMMA pair)* | COLON)? RBRACE
// FIXME this would be nice, but seems to make a ton of extra error messages
//	| LBRACE (expressionList | pair (COMMA pair)* | COLON)? { notifyErrorListeners("error: '}' expected"); }
	;

pair
	: expression COLON expression
	;

identifier
	: Identifier
	| FORMAT
	;

integerLiteral
	: IntegerLiteral
	;

floatingPointLiteral
	: FloatingPointLiteral
	;

characterLiteral
	: CharacterLiteral
	;

stringLiteral
	: StringLiteral
	| RegexLiteral
	;

timeLiteral
	: TimeLiteral
	;


////////////
// LEXING //
////////////

//
// keywords
//

OF       : 'of';
IF       : 'if';
DO       : 'do';
MAP      : 'map';
STACK    : 'stack';
SET      : 'set';
FOR      : 'for';
FOREACH  : 'foreach';
IFALL    : 'ifall';
EXISTS   : 'exists';
NOT      : 'not';
TYPE     : 'type';
ELSE     : 'else';
CASE     : 'case';
OUTPUT   : 'output';
FORMAT   : 'format';
WHILE    : 'while';
BREAK    : 'break';
ARRAY    : 'array';
STATIC   : 'static';
SWITCH   : 'switch';
RETURN   : 'return';
WEIGHT   : 'weight';
RESULT   : 'result';
DEFAULT  : 'default';
CONTINUE : 'continue';
FUNCTION : 'function';
VISITOR  : 'visitor';
BEFORE   : 'before';
AFTER    : 'after';
STOP     : 'stop';

//
// separators
//

SEMICOLON : ';';
COLON     : ':';
COMMA     : ',';
DOT       : '.';
LBRACE    : '{';
RBRACE    : '}';
LPAREN    : '(';
RPAREN    : ')';
LBRACKET  : '[';
RBRACKET  : ']';

//
// operators
//

OR     : 'or';
ONEOR  : '|';
TWOOR  : '||';
AND    : 'and';
ONEAND : '&';
TWOAND : '&&';
INCR   : '++';
DECR   : '--';
EQEQ   : '==';
NEQ    : '!=';
LT     : '<';
LTEQ   : '<=';
GT     : '>';
GTEQ   : '>=';
PLUS   : '+';
MINUS  : '-';
XOR    : '^';
STAR   : '*';
DIV    : '/';
MOD    : '%';
RSHIFT : '>>';
NEG    : '~';
INV    : '!';

//
// other
//

WILDCARD    : '_';
QUESTION    : '?';
DOLLAR      : '$';
EQUALS      : '=';
EMIT        : '<<';
RIGHT_ARROW : '->';

//
// literals
//

IntegerLiteral
	: [-]? DecimalNumeral
	| [-]? HexNumeral 
	| [-]? OctalNumeral 
	| [-]? BinaryNumeral 
	;

fragment
DecimalNumeral
	: NonZeroDigit Digit* 
	;

fragment
Digit
	: [0]
	| NonZeroDigit
	;

fragment
NonZeroDigit
	: [1-9]
	;

fragment
HexNumeral
	: [0] [xX] [0-9a-fA-F]+
	;

fragment
OctalNumeral
	: [0] [0-7]*
	;

fragment
BinaryNumeral
	: [0] [bB] [01]+
	;

FloatingPointLiteral
	: [-]? Digit+ DOT Digit* ExponentPart?
	| [-]? DOT Digit+ ExponentPart?
	| [-]? Digit+ ExponentPart
	;

fragment
ExponentPart
	: [eE] [+-]? Digit+
	;

CharacterLiteral
	: ['] SingleCharacter [']
	| ['] EscapeSequence [']
	;

fragment
SingleCharacter
	: ~['\\\n\r]
	;

RegexLiteral
	: [`] RegexCharacter* [`]
	;

fragment
RegexCharacter
	: ~[`\n\r]
	;

StringLiteral
	: ["] StringCharacter* ["]
	;

fragment
StringCharacter
	: ~["\\\n\r]
	| EscapeSequence
	;

fragment
EscapeSequence
	: [\\] [btnfr"'\\]
	| OctalEscape
	;

fragment
OctalEscape
	: [\\] [0-7]
	| [\\] [0-7] [0-7]
	| [\\] [0-3] [0-7] [0-7]
	;

TimeLiteral
	: IntegerLiteral [tT]?
	| [T] StringLiteral
	;

//
// identifiers
//

Identifier
	: [a-zA-Z] [a-zA-Z0-9_]*
	;

//
// whitespace and comments
//

WS
	: [ \t\r\n\f\u000C]+ -> skip
	;

LINE_COMMENT
	: [#] ~[\r\n]* -> skip
	;
