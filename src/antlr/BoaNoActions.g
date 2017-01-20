grammar BoaNoActions;

@parser::members {
protected void isSemicolon() {
	if (!getCurrentToken().getText().equals(";")) {
		notifyErrorListeners("error: ';' expected");
		return;
	}
	setState(getState() + 1);
	match(SEMICOLON);
}
}

start
	: program EOF
	;

program
	: programStatement+
	;

programStatement
	: declaration
	| statement
	;

declaration
	: typeDeclaration
	| staticVariableDeclaration
	| variableDeclaration
	;

typeDeclaration
	: TYPE identifier EQUALS type { isSemiColon(); }
	;

staticVariableDeclaration
	: STATIC variableDeclaration
	;

variableDeclaration
	: forVariableDeclaration { isSemiColon(); }
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
	| enumType
	| identifier
	;

component
	: (identifier COLON)? type
	;
	
enumBodyDeclaration
	: identifier EQUALS expression
	;

arrayType
	: ARRAY OF component
	;

tupleType
	: LBRACE (member (COMMA member)* COMMA?)? RBRACE
	;

enumType
	: ENUM LBRACE (enumBodyDeclaration (COMMA enumBodyDeclaration)* COMMA?)? RBRACE
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
	: OUTPUT (SET | identifier) (LPAREN expressionList RPAREN)? (LBRACKET component RBRACKET)* OF component (WEIGHT component)? (FORMAT LPAREN expressionList RPAREN)?
	;

functionType
	: FUNCTION LPAREN (identifier COLON type (COMMA identifier COLON type)*)? RPAREN (COLON type)?
	| FUNCTION LPAREN ((identifier COLON type | identifier { notifyErrorListeners("function arguments require an identifier and type"); }) (COMMA identifier COLON type | COMMA identifier { notifyErrorListeners("function arguments require an identifier and type"); })*)? RPAREN (COLON type)?
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
	: factor EQUALS expression { isSemiColon(); }
	;

block
	: LBRACE programStatement* RBRACE
	;

breakStatement
	: BREAK { isSemiColon(); }
	;

continueStatement
	: CONTINUE { isSemiColon(); }
	;

doStatement
	: DO statement WHILE LPAREN expression RPAREN { isSemiColon(); }
	;

emitStatement
	: identifier (LBRACKET expression RBRACKET)* EMIT { notifyErrorListeners("error: expected 'expression' before keyword 'weight'"); } WEIGHT expression { isSemicolon(); }
	| identifier (LBRACKET expression RBRACKET)* EMIT expression                                                                       (WEIGHT expression)? { isSemicolon(); }
	;

forStatement
	: FOR LPAREN (forExpression)? SEMICOLON (expression)? SEMICOLON (forExpression)? RPAREN programStatement
	;

forExpression
	: forVariableDeclaration
	| forExpressionStatement
	;

forVariableDeclaration
	: identifier COLON (type)? (EQUALS ({ notifyErrorListeners("error: output variable declarations should not include '='"); } outputType | expression))?
	;

forExpressionStatement
	: expression (INCR | DECR) # postfixStatement
	| expression               # exprStatement
	;

expressionStatement
	: forExpressionStatement { isSemicolon(); }
	;

ifStatement
	: IF LPAREN expression RPAREN programStatement (ELSE programStatement)?
	;

returnStatement
	: RETURN expression? { isSemicolon(); }
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
	: FOREACH LPAREN identifier COLON type SEMICOLON expression RPAREN programStatement
	;

existsStatement
	: EXISTS LPAREN identifier COLON type SEMICOLON expression RPAREN programStatement
	;

ifallStatement
	: IFALL LPAREN identifier COLON type SEMICOLON expression RPAREN programStatement
	;

whileStatement
	: WHILE LPAREN expression RPAREN programStatement
	;

visitStatement
	: (BEFORE | AFTER | { notifyErrorListeners("error: visit statements must start with 'before' or 'after'"); })
		(
			  WILDCARD
			| identifier COLON identifier
			| identifier (COMMA identifier)*
		)
		RIGHT_ARROW programStatement
	;

stopStatement
	: STOP { isSemicolon(); }
	;

expression
	: conjunction ((TWOOR | OR) conjunction)*
	;

expressionList
	: expression (COMMA expression)*
	| expression ({ notifyErrorListeners("error: ',' expected"); } expression | COMMA expression)*
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
	| identifier   block
	;

visitorExpression
	: visitorType LBRACE (visitStatement | { notifyErrorListeners("error: only 'before' and 'after' visit statements allowed inside visitor bodies"); } programStatement)+ RBRACE
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
	| lit=OF       { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=IF       { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=DO       { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=MAP      { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=STACK    { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=SET      { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=FOR      { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=FOREACH  { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=IFALL    { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=EXISTS   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=NOT      { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=TYPE     { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=ELSE     { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=CASE     { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=OUTPUT   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=WHILE    { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=BREAK    { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=ARRAY    { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=STATIC   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=SWITCH   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=RETURN   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=WEIGHT   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=DEFAULT  { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=CONTINUE { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=FUNCTION { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=VISITOR  { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=BEFORE   { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=AFTER    { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
	| lit=STOP     { notifyErrorListeners("keyword '" + $lit.text + "' can not be used as an identifier"); }
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
DEFAULT  : 'default';
CONTINUE : 'continue';
FUNCTION : 'function';
VISITOR  : 'visitor';
BEFORE   : 'before';
AFTER    : 'after';
STOP     : 'stop';
ENUM 	 : 'enum';

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
	: DecimalNumeral
	| HexNumeral 
	| OctalNumeral 
	| BinaryNumeral 
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
	: Digit+ DOT Digit* ExponentPart?
	| DOT Digit+ ExponentPart?
	| Digit+ ExponentPart
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
