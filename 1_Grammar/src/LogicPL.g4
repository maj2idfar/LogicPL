grammar LogicPL;

logicPL
    : (function | Comment)* main EOF
    ;

Main
    :  'main'
    ;

main
    : Main { System.out.println("MainBody"); } scope
    ;

scope
    : Lbrace (statement)* Rbrace
    ;

statement
    : declare Semicolon
    | assign_stat Semicolon
    | predicate Semicolon
    | implication
    | loop
    | return_stat Semicolon
    | print_statt Semicolon
    ;

assign_stat
    : var_assign
    | arr_assign
    ;

var_assign
    : Var_func_identifier Asgn exp
    ;

arr_assign
    : element_assign
    | whole_arr_assign
    ;

whole_arr_assign
    : Var_func_identifier Asgn list
    ;

list
    : Lbracket (return_arr_val Comma)* return_arr_val Rbracket
    ;

element_assign
    : Var_func_identifier Lbracket exp Rbracket Asgn exp
    ;

return_stat
    : Returnn { System.out.println("Return"); } return_arr_val
    | Returnn { System.out.println("Return"); }
    ;

Returnn
    : 'return'
    ;

print_statt
    : Printtt { System.out.println("Built-in: print"); } Lpar Var_func_identifier Rpar
    | Printtt { System.out.println("Built-in: print"); } Lpar query Rpar
    | Printtt { System.out.println("Built-in: print"); } Lpar arr_element Rpar
    ;

query
    : logicQuery
    | listQuery
    ;

logicQuery
    : Lbracket QMark predicate Rbracket
    ;

listQuery
    : Lbracket Pred_identifier {System.out.println("Predicate: " + $Pred_identifier.getText());} Lpar QMark Rpar Rbracket
    ;

Printtt
    : 'print'
    ;

implication
    : {System.out.println("Implication");} Lpar exp Rpar AMark Lpar (statement)* Rpar // ????????????????????????????????????????????????????????????????????????????????????????
    ;

predicate
    : Pred_identifier {System.out.println("Predicate: " + $Pred_identifier.getText());} Lpar Var_func_identifier Rpar
    | Pred_identifier {System.out.println("Predicate: " + $Pred_identifier.getText());} Lpar arr_element Rpar
    ;

loop
    : Forr {System.out.println("Loop: for");} Lpar Var_func_identifier ColMark Var_func_identifier Rpar scope
    ;

Forr
    : 'for'
    ;

exp
    : orExp exp_
    ;

exp_
    : OR orExp exp_ { System.out.println("Operator: ||"); }
    |
    ;

orExp
    : andExp orExp_
    ;

orExp_
    : AND andExp orExp_ { System.out.println("Operator: &&"); }
    |
    ;

andExp
    : rel2Exp andExp_
    ;

andExp_
    : Equal rel2Exp andExp_ { System.out.println("Operator: =="); }
    | NotEqual rel2Exp andExp_ { System.out.println("Operator: !="); }
    |
    ;

rel2Exp
    : rel1Exp rel2Exp_
    ;

rel2Exp_
    : Gt rel1Exp rel2Exp_ { System.out.println("Operator: <"); }
    | GtE rel1Exp rel2Exp_ { System.out.println("Operator: <="); }
    | Lt rel1Exp rel2Exp_ { System.out.println("Operator: >"); }
    | LtE rel1Exp rel2Exp_ { System.out.println("Operator: >="); }
    |
    ;

rel1Exp
    : art2Exp rel1Exp_
    ;

rel1Exp_
    : Pos art2Exp rel1Exp_ { System.out.println("Operator: +"); }
    | Neg art2Exp rel1Exp_ { System.out.println("Operator: -"); }
    |
    ;

art2Exp
    : art1Exp art2Exp_
    ;

art2Exp_
    : Mul art1Exp art2Exp_ { System.out.println("Operator: *"); }
    | Div art1Exp art2Exp_ { System.out.println("Operator: /"); }
    | Mod art1Exp art2Exp_ { System.out.println("Operator: %"); }
    |
    ;

art1Exp
    : Pos unaryExp { System.out.println("Operator: +"); }
    | Neg unaryExp { System.out.println("Operator: -"); }
    | IsNot unaryExp { System.out.println("Operator: !"); }
    | unaryExp
    ;

unaryExp
    : Lpar exp Rpar
    | IntNum
    | FloatNum
    | bool_value
    | fcall
    | arr_element
    | Var_func_identifier
    | logicQuery
    ;

declare
    : arr_declare
    | var_declare
    ;

arr_declare
    : int_arr_declare
    | float_arr_declare
    | bool_arr_declare
    ;

var_declare
    : int_var_declare
    | float_var_declare
    | bool_var_declare
    ;

int_arr_declare
    : Inttt Lbracket IntNum Rbracket Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); } Asgn list
    | Inttt Lbracket IntNum Rbracket Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); }
    ;

int_var_declare
    : Inttt Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); } Asgn exp
    | Inttt Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); }
    ;

float_arr_declare
    : Floatt Lbracket IntNum Rbracket Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); } Asgn list
    | Floatt Lbracket IntNum Rbracket Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); }
    ;

float_var_declare
    : Floatt Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); } Asgn exp
    | Floatt Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); }
    ;

bool_arr_declare
    : Bool Lbracket IntNum Rbracket Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); } Asgn list
    | Bool Lbracket IntNum Rbracket Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); }
    ;

bool_var_declare
    : Bool Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); } Asgn exp
    | Bool Var_func_identifier { System.out.println("VarDec: " + $Var_func_identifier.getText()); }
    ;

IntNum
    : (Digit)+
    ;

FloatNum
    : (Digit)+ Dot (Digit)+
    ;

bool_value
    : Truee | Falsee
    ;

Truee
    : 'true'
    ;

Falsee
    : 'false'
    ;

dataType
    : Inttt
    | Floatt
    | Bool
    ;

Inttt
    : 'int'
    ;

Floatt
    : 'float'
    ;

Bool
    : 'boolean'
    ;

arr_element
    : Var_func_identifier Lbracket exp Rbracket
    ;

return_arr_val
    : bool_value
    | Pos IntNum { System.out.println("Operator: +"); }
    | Neg IntNum { System.out.println("Operator: -"); }
    | IntNum
    | Pos FloatNum { System.out.println("Operator: +"); }
    | Neg FloatNum { System.out.println("Operator: -"); }
    | FloatNum
    | Var_func_identifier
    ;

Lbracket
    : '['
    ;

Rbracket
    : ']'
    ;

Asgn
    : '='
    ;

Lbrace
    : '{'
    ;

Rbrace
    : '}'
    ;

fcall
    : functionCall
    ;

functionCall
    :  Var_func_identifier argumentList
    ;

argumentList
    : Lpar (exp Comma)* exp Rpar
//    | Lpar (Var_func_dentifier Asgn exp Comma)* (Var_func_identifier Asgn exp) Rpar
    | Lpar Rpar
    ;

function
    : Funccc Var_func_identifier { System.out.println("FunctionDec: " + $Var_func_identifier.getText()); } functionargs ColMark dataType funcBody
    ;

Funccc
    : 'function'
    ;

functionargs
    : Lpar (arg Comma)* (arg) Rpar
    | Lpar Rpar
    ;

arg
    : dataType Var_func_identifier {System.out.println("ArgumentDec: " + $Var_func_identifier.getText());}
    ;

funcBody
    : scope
    ;

Lpar
    : '('
    ;

Rpar
    : ')'
    ;

Comment
    : SharpSign ~[\r\n]*-> skip
    ;

Var_func_identifier
    : [a-z][A-Za-z0-9_]*
    ;

Pred_identifier
    : [A-Z][A-Za-z0-9_]*
    ;

Digit
    : [0-9]
    ;

Semicolon
    : ';'
    ;

Comma
    : ','
    ;

WhiteSpace
    : [ \t\r\n]+ -> skip
    ;

Dot
    : '.'
    ;

IsNot
    : '!'
    ;

Mul
    : '*'
    ;

Div
    : '/'
    ;

Mod
    : '%'
    ;

Pos
    : '+'
    ;

Neg
    : '-'
    ;

Gt
    : '<'
    ;

GtE
    : '<='
    ;

Lt
    : '>'
    ;

LtE
    : '>='
    ;

Equal
    : '=='
    ;

NotEqual
    : '!='
    ;

AND
    : '&&'
    ;

OR
    : '||'
    ;

QMark
    : '?'
    ;

AMark
    : '=>'
    ;

ColMark
    : ':'
    ;

SharpSign
    : '#'
    ;
