grammar Boa;

@header {
import java.util.ArrayList;

import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
}

start returns [Start ast]
	: p=program EOF { $ast = new Start($p.ast); }
	;

program returns [Program ast]
	@init { $ast = new Program(); }
	: (
		d=declaration { $ast.addStatement($d.ast); }
		| s=statement { $ast.addStatement($s.ast); }
	  )+
	;

declaration returns [Statement ast]
	: t=typeDeclaration           { $ast = $t.ast; }
	| s=staticVariableDeclaration { $ast = $s.ast; }
	| v=variableDeclaration       { $ast = $v.ast; }
	;

typeDeclaration returns [TypeDecl ast]
	: TYPE id=identifier EQUALS t=type SEMICOLON { $ast = new TypeDecl($id.ast, $t.ast); }
// FIXME this would be nice, but seems to make a ton of extra error messages
//	| TYPE    identifier EQUALS   type           { notifyErrorListeners("error: ';' expected"); }
	;

staticVariableDeclaration returns [VarDeclStatement ast]
	: STATIC v=variableDeclaration { $ast = $v.ast; $ast.setStatic(true); }
	;

variableDeclaration returns [VarDeclStatement ast]
	: v=forVariableDeclaration SEMICOLON { $ast = $v.ast; }
	|   forVariableDeclaration           { notifyErrorListeners("error: ';' expected"); }
	;

type returns [AbstractType ast]
	: a=arrayType    { $ast = $a.ast; }
	| m=mapType      { $ast = $m.ast; }
	| t=tupleType    { $ast = $t.ast; }
	| o=outputType   { $ast = $o.ast; }
	| f=functionType { $ast = $f.ast; }
	| v=visitorType  { $ast = $v.ast; }
	| s=stackType    { $ast = $s.ast; }
	| set=setType    { $ast = $set.ast; }
	| id=identifier  { $ast = $id.ast; }
	;

component returns [Component ast]
	@init { $ast = new Component(); }
	: (id=identifier COLON { $ast.setIdentifier($id.ast); })? t=type { $ast.setType($t.ast); }
	;

arrayType returns [ArrayType ast]
	: ARRAY OF c=component { $ast = new ArrayType($c.ast); }
	;

tupleType returns [TupleType ast]
	@init { $ast = new TupleType(); }
	: LBRACE (m=member { $ast.addMember($m.ast); } (COMMA m=member { $ast.addMember($m.ast); })* COMMA?)? RBRACE
	;

member returns [Component ast]
	// TODO
	: t=typeDeclaration           //{ $ast = $t.ast; }
	| s=staticVariableDeclaration //{ $ast = $s.ast; }
	| c=component                 { $ast = $c.ast; }
	;

mapType returns [MapType ast]
	: MAP LBRACKET key=component RBRACKET OF value=component { $ast = new MapType($key.ast, $value.ast); }
	;

stackType returns [StackType ast]
	: STACK OF c=component { $ast = new StackType($c.ast); }
	;

setType returns [SetType ast]
	: SET OF c=component { $ast = new SetType($c.ast); }
	;

outputType returns [OutputType ast]
	: OUTPUT (id=identifier { $ast = new OutputType($id.ast); } | tk=SET { $ast = new OutputType(new Identifier($tk.text)); }) (LPAREN el=expressionList RPAREN { $ast.setArgs($el.list); })? (LBRACKET c=component RBRACKET { $ast.addIndice($c.ast); })* OF c=component { $ast.setType($c.ast); } (WEIGHT c=component { $ast.setWeight($c.ast); })? (FORMAT LPAREN el=expressionList RPAREN)?
	;

functionType returns [FunctionType ast]
	@init { $ast = new FunctionType(); }
	: FUNCTION LPAREN (id=identifier COLON t=type { $ast.addArg(new Component($id.ast, $t.ast)); } (COMMA id=identifier COLON t=type { $ast.addArg(new Component($id.ast, $t.ast)); })*)? RPAREN (COLON t=type { $ast.setType($t.ast); })?
	;

visitorType returns [VisitorType ast]
	: t=VISITOR { $ast = new VisitorType(); }
	;

statement returns [Statement ast]
	: b=block                { $ast = $b.ast; }
	| as=assignmentStatement { $ast = $as.ast; }
	| br=breakStatement      { $ast = $br.ast; }
	| cnt=continueStatement  { $ast = $cnt.ast; }
	| stp=stopStatement      { $ast = $stp.ast; }
	| ds=doStatement         { $ast = $ds.ast; }
	| fors=forStatement      { $ast = $fors.ast; }
	| ifs=ifStatement        { $ast = $ifs.ast; }
	| res=resultStatement    { $ast = $res.ast; }
	| ret=returnStatement    { $ast = $ret.ast; }
	| sw=switchStatement     { $ast = $sw.ast; }
	| each=foreachStatement  { $ast = $each.ast; }
	| exist=existsStatement  { $ast = $exist.ast; }
	| all=ifallStatement     { $ast = $all.ast; }
	| whiles=whileStatement  { $ast = $whiles.ast; }
	| empty=emptyStatement   { $ast = $empty.ast; }
	| emit=emitStatement     { $ast = $emit.ast; }
	| es=expressionStatement { $ast = $es.ast; }
	;

emptyStatement returns [Block ast]
	: SEMICOLON { $ast = new Block(); }
	;

assignmentStatement returns [AssignmentStatement ast]
	: f=factor EQUALS e=expression SEMICOLON { $ast = new AssignmentStatement($f.ast, $e.ast); }
	|   factor EQUALS   expression           { notifyErrorListeners("error: ';' expected"); }
	;

block returns [Block ast]
	@init { $ast = new Block(); }
	: LBRACE (d=declaration { $ast.addStatement($d.ast); } | s=statement { $ast.addStatement($s.ast); })* RBRACE
	;

breakStatement returns [BreakStatement ast]
	: BREAK SEMICOLON { $ast = new BreakStatement(); }
	| BREAK           { notifyErrorListeners("error: ';' expected"); }
	;

continueStatement returns [ContinueStatement ast]
	: CONTINUE SEMICOLON { $ast = new ContinueStatement(); }
	| CONTINUE           { notifyErrorListeners("error: ';' expected"); }
	;

doStatement returns [DoStatement ast]
	: DO s=statement WHILE LPAREN e=expression RPAREN SEMICOLON { $ast = new DoStatement($e.ast, $s.ast); }
	| DO   statement WHILE LPAREN   expression RPAREN           { notifyErrorListeners("error: ';' expected"); }
	;

emitStatement returns [EmitStatement ast]
	: id=identifier { $ast = new EmitStatement($id.ast); } (LBRACKET e=expression RBRACKET { $ast.addIndice($e.ast); })* EMIT e=expression { $ast.setValue($e.ast); } (WEIGHT w=expression { $ast.setWeight($w.ast); })? SEMICOLON
	| identifier (LBRACKET expression RBRACKET)* EMIT expression (WEIGHT expression)? { notifyErrorListeners("error: ';' expected"); }
	;

forStatement returns [ForStatement ast]
	@init { $ast = new ForStatement(); }
	: FOR LPAREN (f=forExpression { $ast.setInit($f.ast); })? SEMICOLON (e=expression { $ast.setCondition($e.ast); })? SEMICOLON (f=forExpression { $ast.setUpdate($f.ast); })? RPAREN s=statement { $ast.setBody($s.ast); }
	;

forExpression returns [Statement ast]
	: v=forVariableDeclaration { $ast = $v.ast; }
	| e=forExpressionStatement { $ast = $e.ast; }
	;

forVariableDeclaration returns [VarDeclStatement ast]
	: id=identifier COLON { $ast = new VarDeclStatement($id.ast); } (t=type { $ast.setType($t.ast); })? (EQUALS e=expression { $ast.setInitializer($e.ast); })?
	;

forExpressionStatement returns [Statement ast]
	: e=expression op=(INCR | DECR) { $ast = new PostfixStatement($e.ast, $op.text); }
	| e=expression                  { $ast = new ExprStatement($e.ast); }
	;

expressionStatement returns [Statement ast]
	: e=forExpressionStatement SEMICOLON { $ast = $e.ast; }
	|   forExpressionStatement           { notifyErrorListeners("error: ';' expected"); }
	;

ifStatement returns [IfStatement ast]
	: IF LPAREN e=expression RPAREN s=statement { $ast = new IfStatement($e.ast, $s.ast); } (ELSE els=statement { $ast.setElse($els.ast); })?
	;

resultStatement returns [ResultStatement ast]
	: RESULT e=expression SEMICOLON { $ast = new ResultStatement($e.ast); }
	| RESULT   expression           { notifyErrorListeners("error: ';' expected"); }
	;

returnStatement returns [ReturnStatement ast]
	: RETURN { $ast = new ReturnStatement(); } (e=expression { $ast.setExpr($e.ast); })? SEMICOLON
	| RETURN                                      expression?                                      { notifyErrorListeners("error: ';' expected"); }
	;

switchStatement returns [SwitchStatement ast]
	locals [Block b = new Block();]
	: SWITCH
		LPAREN e=expression RPAREN { $ast = new SwitchStatement($e.ast); }
		LBRACE (sc=switchCase { $ast.addCase($sc.ast); })*
		DEFAULT COLON (s=statement { $b.addStatement($s.ast); })+ RBRACE { $ast.setDefault(new SwitchCase(true, $b)); }
	;

switchCase returns [SwitchCase ast]
	locals [Block b = new Block();]
	: CASE el=expressionList { $ast = new SwitchCase(false, $b, $el.list); } COLON (s=statement { $b.addStatement($s.ast); })+
	;

foreachStatement returns [ForeachStatement ast]
	: FOREACH LPAREN id=identifier COLON t=type SEMICOLON e=expression RPAREN s=statement { $ast = new ForeachStatement(new Component($id.ast, $t.ast), $e.ast, $s.ast); }
	;

existsStatement returns [ExistsStatement ast]
	: EXISTS LPAREN id=identifier COLON t=type SEMICOLON e=expression RPAREN s=statement { $ast = new ExistsStatement(new Component($id.ast, $t.ast), $e.ast, $s.ast); }
	;

ifallStatement returns [IfAllStatement ast]
	: IFALL LPAREN id=identifier COLON t=type SEMICOLON e=expression RPAREN s=statement { $ast = new IfAllStatement(new Component($id.ast, $t.ast), $e.ast, $s.ast); }
	;

whileStatement returns [WhileStatement ast]
	: WHILE LPAREN e=expression RPAREN s=statement { $ast = new WhileStatement($e.ast, $s.ast); }
	;

visitStatement returns [VisitStatement ast]
	: (b=BEFORE | AFTER) { $ast = new VisitStatement($b != null); }
		(
			  WILDCARD { $ast.setWildcard(true); }
			| id=identifier COLON t=identifier { $ast.setComponent(new Component($id.ast, $t.ast)); }
			| id=identifier { $ast.addId($id.ast); } (COMMA id=identifier { $ast.addId($id.ast); })*
		)
		RIGHT_ARROW (d=declaration { $ast.setBody($d.ast); } | s=statement { $ast.setBody($s.ast); })
	;

stopStatement returns [StopStatement ast]
	: STOP SEMICOLON { $ast = new StopStatement(); }
	| STOP           { notifyErrorListeners("error: ';' expected"); }
	;

expression returns [Expression ast]
	: c=conjunction { $ast = new Expression($c.ast); } ((TWOOR | OR) c=conjunction { $ast.addRhs($c.ast); })*
	;

expressionList returns [ArrayList<Expression> list]
	@init { $list = new ArrayList<Expression>(); }
	: e=expression { $list.add($e.ast); } (COMMA e=expression   { $list.add($e.ast); })*
	|   expression                        (COMMA?  expression)* { notifyErrorListeners("error: ',' expected"); }
	;

conjunction returns [Conjunction ast]
	: c=comparison { $ast = new Conjunction($c.ast); } (op=(TWOAND | AND) { $ast.addOp($op.text); } c=comparison { $ast.addRhs($c.ast); })*
	;

comparison returns [Comparison ast]
	: e=simpleExpression op=(EQEQ | NEQ | LT | LTEQ | GT | GTEQ) e2=simpleExpression { $ast = new Comparison($e.ast, $op.text, $e2.ast); }
	| e=simpleExpression                                                             { $ast = new Comparison($e.ast); }
	;

simpleExpression returns [SimpleExpr ast]
	: t=term { $ast = new SimpleExpr($t.ast); } (op=(PLUS | MINUS | ONEOR | XOR) { $ast.addOp($op.text); } t=term { $ast.addRhs($t.ast); })*
	;

term returns [Term ast]
	: f=factor { $ast = new Term($f.ast); } (op=(STAR | DIV | MOD | EMIT | RSHIFT | ONEAND) { $ast.addOp($op.text); } f=factor { $ast.addRhs($f.ast); })*
	;

factor returns [Factor ast]
	: op=operand { $ast = new Factor($op.ast); }
		(
			  s=selector { $ast.addOp($s.ast); }
			| i=index    { $ast.addOp($i.ast); }
			| c=call     { $ast.addOp($c.ast); }
		)*
	;

selector returns [Selector ast]
	: DOT id=identifier { $ast = new Selector($id.ast); }
	;

index returns [Index ast]
	: LBRACKET s=expression { $ast = new Index($s.ast); } (COLON end=expression { $ast.setEnd($end.ast); })? RBRACKET
	;

call returns [Call ast]
	: LPAREN { $ast = new Call(); } (el=expressionList { $ast.setArgs($el.list); })? RPAREN
	;

operand returns [Operand ast]
	: s=stringLiteral                              { $ast = $s.ast; }
	| c=characterLiteral                           { $ast = $c.ast; }
	| t=timeLiteral                                { $ast = $t.ast; }
	| i=integerLiteral                             { $ast = $i.ast; }
	| fp=floatingPointLiteral                      { $ast = $fp.ast; }
	| comp=composite                               { $ast = $comp.ast; }
	| fe=functionExpression                        { $ast = $fe.ast; }
	| v=visitorExpression                          { $ast = $v.ast; }
	| op=(PLUS | MINUS | NEG | INV | NOT) f=factor { $ast = new UnaryFactor($op.text, $f.ast); }
	| DOLLAR                                       // TODO
	| se=statementExpression                       { $ast = $se.ast; }
	| LPAREN e=expression RPAREN                   { $ast = new ParenExpression($e.ast); }
	| id=identifier                                { $ast = $id.ast; }
	;

functionExpression returns [FunctionExpression ast]
	: t=functionType b=block { $ast = new FunctionExpression($t.ast, $b.ast); }
	;

visitorExpression returns [VisitorExpression ast]
	locals [Block b = new Block();]
	: t=visitorType LBRACE (s=visitStatement { $b.addStatement($s.ast); })+ RBRACE { $ast = new VisitorExpression($t.ast, $b); }
	|   visitorType LBRACE (statement { notifyErrorListeners("error: only 'before' and 'after' visit statements allowed inside visitor bodies"); } | visitStatement)+ RBRACE
	;

statementExpression returns [StatementExpr ast]
	: QUESTION b=block { $ast = new StatementExpr($b.ast); }
	;

composite returns [Composite ast]
	@init { $ast = new Composite(); }
	: LBRACE
		(
			  el=expressionList { $ast = new Composite($el.list); }
			| { $ast = new Composite(); } p=pair { $ast.addPair($p.ast); } (COMMA p=pair { $ast.addPair($p.ast); })*
			| COLON { $ast = new Composite(true); }
		)?
		RBRACE
// FIXME this would be nice, but seems to make a ton of extra error messages
//	| LBRACE (expressionList | pair (COMMA pair)* | COLON)? { notifyErrorListeners("error: '}' expected"); }
	;

pair returns [boa.compiler.ast.Pair ast]
	: lhs=expression COLON rhs=expression { $ast = new boa.compiler.ast.Pair($lhs.ast, $rhs.ast); }
	;

identifier returns [Identifier ast]
	: lit=Identifier { $ast = new Identifier($lit.text); }
	| lit=FORMAT     { $ast = new Identifier($lit.text); }
	;

integerLiteral returns [IntegerLiteral ast]
	: lit=IntegerLiteral { $ast = new IntegerLiteral($lit.text); }
	;

floatingPointLiteral returns [FloatLiteral ast]
	: lit=FloatingPointLiteral { $ast = new FloatLiteral($lit.text); }
	;

characterLiteral returns [CharLiteral ast]
	: lit=CharacterLiteral { $ast = new CharLiteral($lit.text); }
	;

stringLiteral returns [StringLiteral ast]
	: lit=StringLiteral { $ast = new StringLiteral(false, $lit.text); }
	| lit=RegexLiteral  { $ast = new StringLiteral(true, $lit.text); }
	;

timeLiteral returns [TimeLiteral ast]
	: lit=TimeLiteral { $ast = new TimeLiteral($lit.text); }
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
