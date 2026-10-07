grammar SysY;

compUnit
    : decl* funcDef* mainFuncDef EOF
    ;

decl
    : constDecl
    | varDecl
    ;

constDecl
    : 'const' bType constDef (',' constDef)* ';'
    ;

bType
    : 'int'
    | 'char'
    ;

constDef
    : IDENT ('[' constExp ']')? '=' constInitVal
    ;

constInitVal
    : constExp
    | '{' (constExp (',' constExp)*)? '}'
    | STRINGCONST
    ;

varDecl
    : 'static'? bType varDef (',' varDef)* ';'
    ;

varDef
    : IDENT ('[' constExp ']')? ('=' initVal)?
    ;

initVal
    : exp
    | '{' (exp (',' exp)*)? '}'
    | STRINGCONST
    ;

funcDef
    : funcType IDENT '(' funcFParams? ')' block
    ;

mainFuncDef
    : 'int' 'main' '(' ')' block
    ;

funcType
    : 'void'
    | 'int'
    | 'char'
    ;

funcFParams
    : funcFParam (',' funcFParam)*
    ;

funcFParam
    : bType IDENT ('[' ']')?
    ;

block
    : '{' blockItem* '}'
    ;

blockItem
    : decl
    | stmt
    ;

stmt
    : lVal '=' exp ';'
    | exp? ';'
    | block
    | 'if' '(' cond ')' stmt ('else' stmt)?
    | 'while' '(' cond ')' stmt
    | 'switch' '(' exp ')' '{' caseStmt* '}'
    | 'break' ';'
    | 'continue' ';'
    | 'return' exp? ';'
    | 'printf' '(' STRINGCONST (',' exp)* ')' ';'
    ;

caseStmt
    : 'case' number ':' stmt*
    | 'default' ':' stmt*
    ;

exp
    : addExp
    ;

cond
    : lOrExp
    ;

lVal
    : IDENT ('[' exp ']')?
    ;

primaryExp
    : '(' exp ')'
    | lVal
    | number
    ;

number
    : INTCONST
    | CHARCONST
    ;

unaryExp
    : primaryExp
    | IDENT '(' funcRParams? ')'
    | unaryOp unaryExp
    | '(' bType ')' unaryExp
    ;

unaryOp
    : '+'
    | '-'
    | '!'
    ;

funcRParams
    : exp (',' exp)*
    ;

mulExp
    : unaryExp (('*' | '/' | '%') unaryExp)*
    ;

addExp
    : mulExp (('+' | '-') mulExp)*
    ;

relExp
    : addExp (('<' | '>' | '<=' | '>=') addExp)*
    ;

eqExp
    : relExp (('==' | '!=') relExp)*
    ;

lAndExp
    : eqExp ('&&' eqExp)*
    ;

lOrExp
    : lAndExp ('||' lAndExp)*
    ;

constExp
    : addExp
    ;

IDENT
    : [_a-zA-Z] [_a-zA-Z0-9]*
    ;

INTCONST
    : [0-9]+
    ;

CHARCONST
    : '\'' (ESC | ~['\\\r\n]) '\''
    ;

STRINGCONST
    : '"' (ESC | ~["\\\r\n])* '"'
    ;

fragment ESC
    : '\\' [ntr0\\'"]
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
