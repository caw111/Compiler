package lexer;

public enum TokenType {
    IDENFR,     // Ident
    INTCON,     // int Const
    CHARCON,    // char const
    STRCON,     // string const
    CONTINUETK, // continue
    DEFAULTTK,  // default
    PRINTFTK,   // printf
    RETURNTK,   // return
    STATICTK,   // static
    BREAKTK,    // break
    CONSTTK,    // const
    WHILETK,    // while
    CASETK,     // case
    CHARTK,     // char
    ELSETK,     // else
    MAINTK,     // main
    VOIDTK,     // void
    INTTK,      // int
    IFTK,       // if

    NEQ,        // !=
    LEQ,        // <=
    GEQ,        // >=
    EQL,        // ==
    OR,         // ||
    NOT,        // !
    MOD,        // %
    LPARENT,    // (
    RPARENT,    // )
    MULT,       // *
    PLUS,       // +
    COMMA,      // ,
    DIV,        // /
    COLON,      // :
    SEMICN,     // ;

    LSS,        // <
    ASSIGN,     // =
    GRE,        // >
    LBRACK,     // [
    RBRACK,     // ]
    LBRACE,     // {
    RBRACE,     // }
    MINU,       // -
    SWITCHTK,   // switch
    AND,        // &&

    a,          // 错误类型
    f,          // 错误类型
    g,          // 错误类型
    h,          // 错误类型
    i,          // 错误类型
    j,          // 错误类型
    k,          // 错误类型
    l,          // 错误类型
    m,          // 错误类型
    n,          // 错误类型

    NOTE,       // 注释
    EOF,        // 文件末尾
}
