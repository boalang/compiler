grammar Boa;

@header {
import java.util.ArrayList;

import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
}

@parser::members {
protected int getStartLine() {
	return getCurrentToken().getLine();
}
protected int getStartColumn() {
	return getCurrentToken().getCharPositionInLine();
}
protected int getEndLine() {
	Token t = _input.LT(-1);
	if (t == null) t = getCurrentToken();
	return t.getLine();
}
protected int getEndColumn() {
	Token t = _input.LT(-1);
	if (t == null) t = getCurrentToken();
	return t.getCharPositionInLine() + t.getText().length() - 1;
}
protected void isSemicolon() {
	if (!getCurrentToken().getText().equals(";")) {
		notifyErrorListeners("error: ';' expected");
		return;
	}
	setState(getState() + 1);
	match(SEMICOLON);
}
}

start returns [Start ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: p=program EOF { $ast = new Start($p.ast); }
	;

program returns [Program ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new Program();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: (s=programStatement { $ast.addStatement($s.ast); })+
	;

programStatement returns [Statement ast]
	: d=declaration { $ast = $d.ast; }
	| s=statement   { $ast = $s.ast; }
	;

declaration returns [Statement ast]
	: t=typeDeclaration           { $ast = $t.ast; }
	| s=staticVariableDeclaration { $ast = $s.ast; }
	| v=variableDeclaration       { $ast = $v.ast; }
	;

typeDeclaration returns [TypeDecl ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: TYPE id=identifier EQUALS t=type { isSemicolon(); $ast = new TypeDecl($id.ast, $t.ast); }
	;

staticVariableDeclaration returns [VarDeclStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: STATIC v=variableDeclaration { $ast = $v.ast; $ast.setStatic(true); }
	;

variableDeclaration returns [VarDeclStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: v=forVariableDeclaration  { isSemicolon(); $ast = $v.ast; }
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
	| e=enumType     { $ast = $e.ast; }
	| id=identifier  { $ast = $id.ast; }
	;

component returns [Component ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new Component();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: (id=identifier COLON { $ast.setIdentifier($id.ast); })? t=type { $ast.setType($t.ast); }
	;
	
enumBodyDeclaration returns [EnumBodyDeclaration ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: id=identifier EQUALS e=expression { $ast = new EnumBodyDeclaration($id.ast, $e.ast); }
	;

arrayType returns [ArrayType ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: ARRAY OF m=component { $ast = new ArrayType($m.ast); }
	;

tupleType returns [TupleType ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new TupleType();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: LBRACE (m=member { $ast.addMember($m.ast); } (COMMA m=member { $ast.addMember($m.ast); })* COMMA?)? RBRACE
	;
	
enumType returns [EnumType ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new EnumType();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: ENUM LBRACE (m=enumBodyDeclaration { $ast.addMember($m.ast); } (COMMA m=enumBodyDeclaration { $ast.addMember($m.ast); })* COMMA?)? RBRACE
	;

member returns [Component ast]
	// TODO
	: t=typeDeclaration           //{ $ast = $t.ast; }
	| s=staticVariableDeclaration //{ $ast = $s.ast; }
	| c=component                 { $ast = $c.ast; }
	;

mapType returns [MapType ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: MAP LBRACKET key=component RBRACKET OF value=component { $ast = new MapType($key.ast, $value.ast); }
	;

stackType returns [StackType ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: STACK OF m=component { $ast = new StackType($m.ast); }
	;

setType returns [SetType ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: SET OF m=component { $ast = new SetType($m.ast); }
	;

outputType returns [OutputType ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: OUTPUT (tk=SET { $ast = new OutputType(new Identifier($tk.text)); } | id=identifier { $ast = new OutputType($id.ast); }) (LPAREN el=expressionList RPAREN { $ast.setArgs($el.list); })? (LBRACKET m=component RBRACKET { $ast.addIndice($m.ast); })* OF m=component { $ast.setType($m.ast); } (WEIGHT m=component { $ast.setWeight($m.ast); })? (FORMAT LPAREN el=expressionList RPAREN)?
	;

functionType returns [FunctionType ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new FunctionType();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: FUNCTION LPAREN (id=identifier COLON t=type { $ast.addArg(new Component($id.ast, $t.ast)); } (COMMA id=identifier COLON t=type { $ast.addArg(new Component($id.ast, $t.ast)); })*)? RPAREN (COLON t=type { $ast.setType($t.ast); })?
	| FUNCTION LPAREN ((id=identifier COLON t=type { $ast.addArg(new Component($id.ast, $t.ast)); } | identifier { notifyErrorListeners("function arguments require an identifier and type"); }) (COMMA id=identifier COLON t=type { $ast.addArg(new Component($id.ast, $t.ast)); } | COMMA identifier { notifyErrorListeners("function arguments require an identifier and type"); })*)? RPAREN (COLON t=type { $ast.setType($t.ast); })?
	;

visitorType returns [VisitorType ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
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
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: SEMICOLON { $ast = new Block(); }
	;

assignmentStatement returns [AssignmentStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: f=factor EQUALS e=expression { isSemicolon(); $ast = new AssignmentStatement($f.ast, $e.ast); }
	;

block returns [Block ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new Block();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: LBRACE (s=programStatement { $ast.addStatement($s.ast); })* RBRACE
	;

breakStatement returns [BreakStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: BREAK { isSemicolon(); $ast = new BreakStatement(); }
	;

continueStatement returns [ContinueStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: CONTINUE { isSemicolon(); $ast = new ContinueStatement(); }
	;

doStatement returns [DoStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: DO s=statement WHILE LPAREN e=expression RPAREN { isSemicolon(); $ast = new DoStatement($e.ast, $s.ast); }
	;

emitStatement returns [EmitStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: id=identifier { $ast = new EmitStatement($id.ast); } (LBRACKET e=expression RBRACKET)* EMIT { notifyErrorListeners("error: expected 'expression ' before keyword 'weight'"); } WEIGHT w=expression { isSemicolon(); }
	| id=identifier { $ast = new EmitStatement($id.ast); } (LBRACKET e=expression RBRACKET { $ast.addIndice($e.ast); })* EMIT e=expression { $ast.setValue($e.ast); } (WEIGHT w=expression { $ast.setWeight($w.ast); })? { isSemicolon(); }
	;

forStatement returns [ForStatement ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new ForStatement();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: FOR LPAREN (f=forExpression { $ast.setInit($f.ast); })? SEMICOLON (e=expression { $ast.setCondition($e.ast); })? SEMICOLON (f=forExpression { $ast.setUpdate($f.ast); })? RPAREN s=programStatement { $ast.setBody($s.ast); }
	;

forExpression returns [Statement ast]
	: v=forVariableDeclaration { $ast = $v.ast; }
	| e=forExpressionStatement { $ast = $e.ast; }
	;

forVariableDeclaration returns [VarDeclStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: id=identifier COLON { $ast = new VarDeclStatement($id.ast); } (t=type { $ast.setType($t.ast); })? (EQUALS ({ notifyErrorListeners("error: output variable declarations should not include '='"); } ot=outputType { $ast.setType($ot.ast); } | e=expression { $ast.setInitializer($e.ast); }))?
	;

forExpressionStatement returns [Statement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: e=expression op=(INCR | DECR) { $ast = new PostfixStatement($e.ast, $op.text); }
	| e=expression                  { $ast = new ExprStatement($e.ast); }
	;

expressionStatement returns [Statement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: e=forExpressionStatement { isSemicolon(); $ast = $e.ast; }
	;

ifStatement returns [IfStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: IF LPAREN e=expression RPAREN s=programStatement { $ast = new IfStatement($e.ast, $s.ast); } (ELSE els=programStatement { $ast.setElse($els.ast); })?
	;

returnStatement returns [ReturnStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: RETURN { $ast = new ReturnStatement(); } (e=expression { $ast.setExpr($e.ast); })? { isSemicolon(); }
	;

switchStatement returns [SwitchStatement ast]
	locals [Block b, int l, int c]
	@init { $b = new Block(); $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: SWITCH
		LPAREN e=expression RPAREN { $ast = new SwitchStatement($e.ast); }
		LBRACE (sc=switchCase { $ast.addCase($sc.ast); })*
		DEFAULT COLON (s=programStatement { $b.addStatement($s.ast); })+ RBRACE { $ast.setDefault(new SwitchCase(true, $b)); }
	;

switchCase returns [SwitchCase ast]
	locals [Block b, int l, int c]
	@init { $b = new Block(); $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: CASE el=expressionList { $ast = new SwitchCase(false, $b, $el.list); } COLON (s=programStatement { $b.addStatement($s.ast); })+
	;

foreachStatement returns [ForeachStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: FOREACH LPAREN id=identifier COLON t=type SEMICOLON e=expression RPAREN s=programStatement { $ast = new ForeachStatement(new Component($id.ast, $t.ast), $e.ast, $s.ast); }
	;

existsStatement returns [ExistsStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: EXISTS LPAREN id=identifier COLON t=type SEMICOLON e=expression RPAREN s=programStatement { $ast = new ExistsStatement(new Component($id.ast, $t.ast), $e.ast, $s.ast); }
	;

ifallStatement returns [IfAllStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: IFALL LPAREN id=identifier COLON t=type SEMICOLON e=expression RPAREN s=programStatement { $ast = new IfAllStatement(new Component($id.ast, $t.ast), $e.ast, $s.ast); }
	;

whileStatement returns [WhileStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: WHILE LPAREN e=expression RPAREN s=programStatement { $ast = new WhileStatement($e.ast, $s.ast); }
	;

visitStatement returns [VisitStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: (b=BEFORE | AFTER | { notifyErrorListeners("error: visit statements must start with 'before' or 'after'"); }) { $ast = new VisitStatement($b != null); }
		(
			  WILDCARD { $ast.setWildcard(true); }
			| id=identifier COLON t=identifier { $ast.setComponent(new Component($id.ast, $t.ast)); }
			| id=identifier { $ast.addId($id.ast); } (COMMA id=identifier { $ast.addId($id.ast); })*
		)
		RIGHT_ARROW (s=programStatement { $ast.setBody($s.ast); })
	;

stopStatement returns [StopStatement ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: STOP { isSemicolon(); $ast = new StopStatement(); }
	;

expression returns [Expression ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: j=conjunction { $ast = new Expression($j.ast); } ((TWOOR | OR) j=conjunction { $ast.addRhs($j.ast); })*
	;

expressionList returns [ArrayList<Expression> list]
	@init { $list = new ArrayList<Expression>(); }
	: e=expression { $list.add($e.ast); } (COMMA e=expression   { $list.add($e.ast); })*
	| e=expression { $list.add($e.ast); } ({ notifyErrorListeners("error: ',' expected"); } e=expression { $list.add($e.ast); } | COMMA e=expression { $list.add($e.ast); })*
	;

conjunction returns [Conjunction ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: m=comparison { $ast = new Conjunction($m.ast); } (op=(TWOAND | AND) { $ast.addOp($op.text); } m=comparison { $ast.addRhs($m.ast); })*
	;

comparison returns [Comparison ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: e=simpleExpression op=(EQEQ | NEQ | LT | LTEQ | GT | GTEQ) e2=simpleExpression { $ast = new Comparison($e.ast, $op.text, $e2.ast); }
	| e=simpleExpression                                                             { $ast = new Comparison($e.ast); }
	;

simpleExpression returns [SimpleExpr ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: t=term { $ast = new SimpleExpr($t.ast); } (op=(PLUS | MINUS | ONEOR | XOR) { $ast.addOp($op.text); } t=term { $ast.addRhs($t.ast); })*
	;

term returns [Term ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: f=factor { $ast = new Term($f.ast); } (op=(STAR | DIV | MOD | EMIT | RSHIFT | ONEAND) { $ast.addOp($op.text); } f=factor { $ast.addRhs($f.ast); })*
	;

factor returns [Factor ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: op=operand { $ast = new Factor($op.ast); }
		(
			  s=selector { $ast.addOp($s.ast); }
			| i=index    { $ast.addOp($i.ast); }
			| m=call     { $ast.addOp($m.ast); }
		)*
	;

selector returns [Selector ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: DOT id=identifier { $ast = new Selector($id.ast); }
	;

index returns [Index ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: LBRACKET s=expression { $ast = new Index($s.ast); } (COLON end=expression { $ast.setEnd($end.ast); })? RBRACKET
	;

call returns [Call ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
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
	| uf=unaryFactor                               { $ast = $uf.ast; }
	| DOLLAR                                       // TODO
	| pe=parenExpression                           { $ast = $pe.ast; }
	| id=identifier                                { $ast = $id.ast; }
	;

unaryFactor returns [UnaryFactor ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: op=(PLUS | MINUS | NEG | INV | NOT) f=factor { $ast = new UnaryFactor($op.text, $f.ast); }
	;

parenExpression returns [ParenExpression ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: LPAREN e=expression RPAREN                   { $ast = new ParenExpression($e.ast); }
	;

functionExpression returns [FunctionExpression ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: t=functionType b=block { $ast = new FunctionExpression($t.ast, $b.ast); }
	| id=identifier  b=block { $ast = new FunctionExpression($id.ast, $b.ast); }
	;

visitorExpression returns [VisitorExpression ast]
	locals [Block b, int l, int c]
	@init { $b = new Block(); $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: t=visitorType LBRACE (s=visitStatement { $b.addStatement($s.ast); } | { notifyErrorListeners("error: only 'before' and 'after' visit statements allowed inside visitor bodies"); } programStatement)+ RBRACE { $ast = new VisitorExpression($t.ast, $b); }
	;

composite returns [Composite ast]
	locals [int l, int c]
	@init {
		$l = getStartLine(); $c = getStartColumn();
		$ast = new Composite();
	}
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
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
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: lhs=expression COLON rhs=expression { $ast = new boa.compiler.ast.Pair($lhs.ast, $rhs.ast); }
	;

identifier returns [Identifier ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); $ast = new Identifier("<ERROR>"); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: lit=Identifier { $ast = new Identifier($lit.text); }
	| lit=FORMAT     { $ast = new Identifier($lit.text); }
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

integerLiteral returns [IntegerLiteral ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: lit=IntegerLiteral { $ast = new IntegerLiteral($lit.text); }
	;

floatingPointLiteral returns [FloatLiteral ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: lit=FloatingPointLiteral { $ast = new FloatLiteral($lit.text); }
	;

characterLiteral returns [CharLiteral ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: lit=CharacterLiteral { $ast = new CharLiteral($lit.text); }
	;

stringLiteral returns [StringLiteral ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
	: lit=StringLiteral { $ast = new StringLiteral(false, $lit.text); }
	| lit=RegexLiteral  { $ast = new StringLiteral(true, $lit.text); }
	;

timeLiteral returns [TimeLiteral ast]
	locals [int l, int c]
	@init { $l = getStartLine(); $c = getStartColumn(); }
	@after { $ast.setPositions($l, $c, getEndLine(), getEndColumn()); }
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
DEFAULT  : 'default';
CONTINUE : 'continue';
FUNCTION : 'function';
VISITOR  : 'visitor';
BEFORE   : 'before';
AFTER    : 'after';
STOP     : 'stop';
ENUM	 : 'enum';

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
