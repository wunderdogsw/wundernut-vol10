grammar Doggo;

@header {
    package lang.doggo.generated;
}

program
    : statements? result EOF
    ;

statements
    : statement+
    ;

statement
    : assignment
    | ifelse
    | loop
    ;

assignment: ID OP_ASSIGN expr;

ifelse: 'RUF?' expr 'VUH' ifstmts=statements ('ROWH' elstmts=statements)? 'ARRUF';

loop:   'GRRR' expr 'BOW' statements 'BORF';

result: expr;

expr
    : left=expr infix_op right=expr #infix_expr
    | ID                            #eval_expr
    | literal                       #literal_expr
    ;

infix_op
    : OP_ADD
    | OP_SUB
    | OP_MUL
    | OP_GT
    | OP_LT
    ;

literal
    : NUMBER
    ;

OP_ASSIGN:  'AWOO';
OP_ADD:     'WOOF';
OP_GT:      'YAP' ;
OP_LT:      'YIP' ;
OP_MUL:     'ARF' ;
OP_SUB:     'BARK';

ID:         [a-z]+;
NUMBER:     [0-9]+;

WS:         [ \t\r\n] -> skip;
