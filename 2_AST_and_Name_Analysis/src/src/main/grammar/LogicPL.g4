grammar LogicPL;

@header{
import ast.node.*;
import ast.node.expression.*;
import ast.node.statement.*;
import ast.node.declaration.*;
import ast.node.expression.values.*;
import ast.node.expression.operators.*;
import ast.type.primitiveType.*;
import ast.type.*;
}

program returns[Program p]:
    {$p = new Program(); $p.setLine(0);}
    (f = functionDec {$p.addFunc($f.functionDeclaration);})*
    main = mainBlock {$p.setMain($main.main); }
    ;

functionDec returns[FuncDeclaration functionDeclaration]:
    {ArrayList<ArgDeclaration> args = new ArrayList<>();
     ArrayList<Statement> statements = new ArrayList<>();}
    FUNCTION name = identifier
    LPAR (arg1 = functionVarDec {args.add($arg1.argDeclaration);} (COMMA arg = functionVarDec {args.add($arg.argDeclaration);})*)? RPAR COLON returnType = type
    LBRACE ((stmt = statement {statements.add($stmt.statementAtt);})+) RBRACE
    {$functionDeclaration = new FuncDeclaration($name.identifierAtt, $returnType.typeAtt, args, statements); $functionDeclaration.setLine($name.identifierAtt.getLine());}
    ;

functionVarDec returns [ArgDeclaration argDeclaration]:
    t = type arg_iden = identifier {$argDeclaration = new ArgDeclaration($arg_iden.identifierAtt, $t.typeAtt); $argDeclaration.setLine($arg_iden.identifierAtt.getLine());}
    ;

mainBlock returns [MainDeclaration main]:
    {ArrayList<Statement> mainStmts = new ArrayList<>();}
    m = MAIN LBRACE (s = statement {mainStmts.add($s.statementAtt);})+ RBRACE
    {$main = new MainDeclaration(mainStmts); $main.setLine($m.getLine());}
    ;

statement returns [Statement statementAtt]:
    s1 = assignSmt {$statementAtt = $s1.assignStmtAtt;} | (s2 = predicate SEMICOLON  {$statementAtt = $s2.predicateAtt;})
    | s3 = implication {$statementAtt = $s3.implicationAtt;} | s4 = returnSmt {$statementAtt = $s4.returnAtt;}
    | s5 = printSmt {$statementAtt = $s5.PrintStmtAtt;} | s6 = forLoop {$statementAtt = $s6.forAtt;} | s7 = localVarDeclaration {$statementAtt = $s7.localvarDecAtt;}
    ;

assignSmt returns [AssignStmt assignStmtAtt] :
    varr= variable line = ASSIGN exprr = expression SEMICOLON
    {$assignStmtAtt = new AssignStmt($varr.v,  $exprr.e); $assignStmtAtt.setLine($line.getLine());}
    ;

variable returns[Variable v]:
    v1 = identifier {$v = $v1.identifierAtt;}  | v2 = identifier LBRACKET exppppp = expression RBRACKET {$v = new ArrayAccess($v2.identifierAtt.getName(), $exppppp.e); $v.setLine($v2.identifierAtt.getLine()); }
    ;

localVarDeclaration returns[Statement localvarDecAtt]:
     l1 = varDeclaration {$localvarDecAtt = $l1.varDecAtt;}
    | l2 = arrayDeclaration {$localvarDecAtt = $l2.arrayDecAtt;}
    ;

varDeclaration returns [VarDecStmt varDecAtt]:
     t = type vi = identifier {$varDecAtt = new VarDecStmt($vi.identifierAtt, $t.typeAtt); $varDecAtt.setLine($vi.identifierAtt.getLine());} (ASSIGN e = expression {$varDecAtt.setInitialExpression($e.e);} )? SEMICOLON
    ;

arrayDeclaration returns [ArrayDecStmt arrayDecAtt] :
    t = type LBRACKET INT_NUMBER RBRACKET var_iden = identifier {$arrayDecAtt = new ArrayDecStmt($var_iden.identifierAtt, $t.typeAtt, $INT_NUMBER.int); $arrayDecAtt.setLine($var_iden.identifierAtt.getLine());}
    ( aiv = arrayInitialValue {$arrayDecAtt.setInitialValues($aiv.initialValues);}) ?  SEMICOLON
    ;

arrayInitialValue returns [ArrayList<Expression> initialValues]:
    ASSIGN arrList = arrayList {$initialValues = $arrList.values;}
    ;

arrayList returns [ArrayList<Expression> values]:
    { $values = new ArrayList<Expression>();}
    LBRACKET (v = value {$values.add($v.v);}| id = identifier {$values.add($id.identifierAtt);}) (COMMA (v2 = value {$values.add($v2.v);}| id2 = identifier {$values.add($id2.identifierAtt);}))* RBRACKET
    ;

printSmt returns [PrintStmt PrintStmtAtt] :
    p = PRINT LPAR arg = printExpr RPAR SEMICOLON
    {$PrintStmtAtt = new PrintStmt($arg.printExprAtt); $PrintStmtAtt.setLine($p.getLine());}
    ;

printExpr returns [Expression printExprAtt]:
     var = variable {$printExprAtt = $var.v;}
    | q = query {$printExprAtt = $q.queryAtt;}
    ;

query returns [QueryExpression queryAtt]:
      q1 = queryType1 {$queryAtt = $q1.query1Att;}
     | q2 = queryType2 {$queryAtt = $q2.query2Att;}
    ;

queryType1 returns [QueryExpression query1Att]:
    LBRACKET line = QUARYMARK id = predicateIdentifier LPAR var = variable RPAR RBRACKET
    {$query1Att= new QueryExpression($id.predicateIdentifierAtt); $query1Att.setLine($line.getLine()); $query1Att.setVar($var.v);}
    ;
queryType2 returns [QueryExpression query2Att]:
    LBRACKET id = predicateIdentifier LPAR line = QUARYMARK RPAR RBRACKET
    {$query2Att = new QueryExpression($id.predicateIdentifierAtt); $query2Att.setLine($line.getLine()); }
    ;
returnSmt returns [ReturnStmt returnAtt]:
    RETURN (v = value {$returnAtt = new ReturnStmt($v.v);} | iden = identifier {$returnAtt = new ReturnStmt($iden.identifierAtt);})? SEMICOLON {if($returnAtt == null){$returnAtt = new ReturnStmt(null);}}
    {$returnAtt.setLine($RETURN.getLine());}
    ;
forLoop returns [ForloopStmt forAtt]:
    {ArrayList<Statement> bodyStmts = new ArrayList<>();}
    line = FOR LPAR iterator = identifier COLON arrayName = identifier RPAR
    LBRACE ((stmt =statement {bodyStmts.add($stmt.statementAtt);})*) RBRACE
    {$forAtt = new ForloopStmt($iterator.identifierAtt, $arrayName.identifierAtt, bodyStmts); $forAtt.setLine($line.getLine());}
    ;

predicate returns [PredicateStmt predicateAtt]:
    id = predicateIdentifier LPAR v = variable RPAR
    {$predicateAtt = new PredicateStmt($id.predicateIdentifierAtt, $v.v); $predicateAtt.setLine($id.predicateIdentifierAtt.getLine());}
    ;
implication returns [ImplicationStmt implicationAtt]:
    {ArrayList<Statement> results = new ArrayList<Statement>();}
    LPAR  e = expression RPAR a = ARROW LPAR ((s = statement {results.add($s.statementAtt);})+) RPAR
    {$implicationAtt = new ImplicationStmt($e.e, results); $implicationAtt.setLine($a.getLine());}
    ;

expression returns [Expression e]:
    ae= andExpr e2= expression2
    {if($e2.e != null) {$e = new BinaryExpression($ae.e, $e2.e.getRight(), $e2.e.getBinaryOperator()); $e.setLine($e2.e.getLine());} else {$e = $ae.e;}}
    ;

expression2 returns [BinaryExpression e] locals [BinaryExpression ee]:
    OR ae= andExpr e2 = expression2
    {if($e2.e != null) {$ee = new BinaryExpression($ae.e, $e2.e.getRight(), $e2.e.getBinaryOperator()); $ee.setLine($e2.e.getLine()); $e = new BinaryExpression(null, $ee, BinaryOperator.or);} else{$e = new BinaryExpression(null, $ae.e, BinaryOperator.or);}}
    {$e.setLine($OR.getLine());}
    |
    {$e = null;}
    ;

andExpr returns [Expression e] :
    eE = eqExpr aE= andExpr2
    {if($aE.e != null) {$e = new BinaryExpression($eE.e, $aE.e.getRight(), $aE.e.getBinaryOperator()); $e.setLine($aE.e.getLine());} else {$e = $eE.e;}}
    ;

andExpr2 returns [BinaryExpression e] locals [BinaryExpression ee]:
    AND eE=eqExpr aE = andExpr2
    {if($aE.e != null) {$ee = new BinaryExpression($eE.e, $aE.e.getRight(), $aE.e.getBinaryOperator()); $ee.setLine($aE.e.getLine()); $e = new BinaryExpression(null, $ee, BinaryOperator.and);} else{$e = new BinaryExpression(null, $eE.e, BinaryOperator.and);}}
    {$e.setLine($AND.getLine());}
    |
    {$e = null;}
    ;

eqExpr returns [Expression e]:
    cE = compExpr eE =eqExpr2
    {if($eE.e != null) {$e = new BinaryExpression($cE.e, $eE.e.getRight(), $eE.e.getBinaryOperator()); $e.setLine($eE.e.getLine());} else {$e = $cE.e;}}
    ;

eqExpr2 returns [BinaryExpression e] locals [BinaryOperator opt, BinaryExpression ee]:
    (op = EQ {$opt = BinaryOperator.eq;}| op = NEQ {$opt = BinaryOperator.neq;}) l = compExpr r = eqExpr2
    {if($r.e != null) {$ee = new BinaryExpression($l.e, $r.e.getRight(), $r.e.getBinaryOperator()); $ee.setLine($r.e.getLine()); $e = new BinaryExpression(null, $ee, $opt);} else{$e = new BinaryExpression(null, $l.e, $opt);}}
    {$e.setLine($op.getLine());}
    |
    {$e = null;}
    ;

compExpr returns [Expression e]:
    a = additive c = compExpr2
    {if($c.e != null) {$e = new BinaryExpression($a.e, $c.e.getRight(), $c.e.getBinaryOperator()); $e.setLine($c.e.getLine());} else {$e = $a.e;}}
    ;

compExpr2 returns [BinaryExpression e] locals [BinaryOperator opt, BinaryExpression ee]:
    (op = LT {$opt = BinaryOperator.lt;}| op = LTE {$opt = BinaryOperator.lte;}| op = GT {$opt = BinaryOperator.gt;}| op = GTE{$opt = BinaryOperator.gte;}) l = additive r = compExpr2
    {if($r.e != null) {$ee = new BinaryExpression($l.e, $r.e.getRight(), $r.e.getBinaryOperator()); $ee.setLine($r.e.getLine()); $e = new BinaryExpression(null, $ee, $opt);} else{$e = new BinaryExpression(null, $l.e, $opt);}}
    {$e.setLine($op.getLine());}
    |
    {$e = null;}
    ;

additive returns [Expression e]:
    m = multicative a = additive2
    {if($a.e != null) {$e = new BinaryExpression($m.e, $a.e.getRight(), $a.e.getBinaryOperator()); $e.setLine($a.e.getLine());} else {$e = $m.e;}}
    ;

additive2 returns [BinaryExpression e] locals [BinaryOperator opt, BinaryExpression ee]:
    (op = PLUS {$opt = BinaryOperator.add;} | op = MINUS {$opt = BinaryOperator.sub;}) l = multicative r = additive2
    {if($r.e != null) {$ee = new BinaryExpression($l.e, $r.e.getRight(), $r.e.getBinaryOperator()); $ee.setLine($r.e.getLine()); $e = new BinaryExpression(null, $ee, $opt);} else{$e = new BinaryExpression(null, $l.e, $opt);}}
    {$e.setLine($op.getLine());}
    |
    {$e = null;}
    ;

multicative returns [Expression e]:
    u =  unary m = multicative2
    {if($m.be != null) {$e = new BinaryExpression($u.e, $m.be.getRight(), $m.be.getBinaryOperator()); $e.setLine($m.be.getLine());} else {$e = $u.e;}}
    ;

multicative2 returns [BinaryExpression be] locals [BinaryOperator opt, BinaryExpression ee]:
    (op = MULT {$opt = BinaryOperator.mult;} | op = MOD {$opt = BinaryOperator.mod;}| op = DIV {$opt = BinaryOperator.div;}) l = unary r = multicative2
    {if($r.be != null) {$ee = new BinaryExpression($l.e, $r.be.getRight(), $r.be.getBinaryOperator()); $ee.setLine($r.be.getLine()); $be = new BinaryExpression(null, $ee, $opt);} else{$be = new BinaryExpression(null, $l.e, $opt);}}
    {$be.setLine($op.getLine());}
    |
    {$be = null;}
    ;

unary returns [Expression e] locals [UnaryOperator unaryAtt]:
    otherAtt = other {$e = $otherAtt.e;}
    |
     (op = PLUS {$unaryAtt = UnaryOperator.plus;} | op = MINUS {$unaryAtt = UnaryOperator.minus;} | op = NOT {$unaryAtt = UnaryOperator.not;}) exp = other
     {$e = new UnaryExpression($unaryAtt, $exp.e); $e.setLine($op.getLine());}
    ;

other returns [Expression e]:
    LPAR exp = expression RPAR {$e = $exp.e;}| v = variable {$e = $v.v;} | v_ = value {$e = $v_.v;}
    | q1 = queryType1 {$e = $q1.query1Att;} | fc = functionCall {$e = $fc.functionCallAtt;}
    ;

functionCall returns [FunctionCall functionCallAtt]:
    {ArrayList<Expression> args = new ArrayList<Expression>();}
    i = identifier LPAR (arg1 = expression {args.add($arg1.e);} (COMMA arg2 = expression {args.add($arg2.e);})*)? RPAR
    {$functionCallAtt = new FunctionCall(args, $i.identifierAtt); $functionCallAtt.setLine($i.identifierAtt.getLine());}
    ;

value returns [Value v]:
    val = numericValue {$v = $val.numericValueAtt;}
    | t = TRUE {$v = new BooleanValue(true); $v.setLine($t.getLine());}
    | f = FALSE {$v = new BooleanValue(false); $v.setLine($f.getLine());}
    | MINUS v2 = numericValue {$v2.numericValueAtt.negateConstant(); $v = $v2.numericValueAtt;}
    ;

numericValue returns [Value numericValueAtt]:
    i = INT_NUMBER {$numericValueAtt = new IntValue($i.int); $numericValueAtt.setLine($i.getLine());}
    | f = FLOAT_NUMBER {$numericValueAtt = new FloatValue(Float.parseFloat($f.text)); $numericValueAtt.setLine($f.getLine());}
    ;

identifier returns [Identifier identifierAtt]:
    i = IDENTIFIER {$identifierAtt = new Identifier($i.text); $identifierAtt.setLine($i.getLine());}
    ;

predicateIdentifier returns [Identifier predicateIdentifierAtt]:
    pi = PREDICATE_IDENTIFIER {$predicateIdentifierAtt = new Identifier($pi.text); $predicateIdentifierAtt.setLine($pi.getLine());}
    ;

type returns [Type typeAtt]:
    BOOLEAN {$typeAtt = new BooleanType();}
    | INT {$typeAtt = new IntType();}
    | FLOAT {$typeAtt = new FloatType();}
    ;




FUNCTION : 'function';
BOOLEAN : 'boolean';
INT : 'int';
FLOAT: 'float';
MAIN: 'main';
PRINT: 'print';
RETURN: 'return';
FOR: 'for';
TRUE: 'true';
FALSE: 'false';

LPAR: '(';
RPAR: ')';
COLON: ':';
COMMA: ',';
LBRACE: '{';
RBRACE: '}';
SEMICOLON: ';';
ASSIGN: '=';
LBRACKET: '[';
RBRACKET: ']';
QUARYMARK: '?';
ARROW: '=>';
OR: '||';
AND: '&&';
EQ: '==';
GT: '>';
LT: '<';
GTE: '>=';
LTE: '<=';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
MOD: '%';
NEQ: '!=';
NOT: '!';


WS : [ \t\r\n]+ -> skip ;
COMMENT : '#' ~[\r\n]* -> skip ;

IDENTIFIER : [a-z][a-zA-Z0-9_]* ;
PREDICATE_IDENTIFIER : [A-Z][a-zA-Z0-9]* ;
INT_NUMBER : [0-9]+;
FLOAT_NUMBER: ([0-9]*[.])?[0-9]+;