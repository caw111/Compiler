package lexer;

import java.util.Map;

public class TokenTypeMap {
    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("continue", TokenType.CONTINUETK),
            Map.entry("default", TokenType.DEFAULTTK),
            Map.entry("printf", TokenType.PRINTFTK),
            Map.entry("return", TokenType.RETURNTK),
            Map.entry("static", TokenType.STATICTK),
            Map.entry("break", TokenType.BREAKTK),
            Map.entry("const", TokenType.CONSTTK),
            Map.entry("while", TokenType.WHILETK),
            Map.entry("case", TokenType.CASETK),
            Map.entry("char", TokenType.CHARTK),
            Map.entry("else", TokenType.ELSETK),
            Map.entry("main", TokenType.MAINTK),
            Map.entry("void", TokenType.VOIDTK),
            Map.entry("int", TokenType.INTTK),
            Map.entry("if", TokenType.IFTK),
            Map.entry("switch", TokenType.SWITCHTK),

            Map.entry("!=", TokenType.NEQ),
            Map.entry("<=", TokenType.LEQ),
            Map.entry(">=", TokenType.GEQ),
            Map.entry("==", TokenType.EQL),
            Map.entry("||", TokenType.OR),
            Map.entry("&&", TokenType.AND),
            Map.entry("!", TokenType.NOT),
            Map.entry("%", TokenType.MOD),
            Map.entry("(", TokenType.LPARENT),
            Map.entry(")", TokenType.RPARENT),
            Map.entry("*", TokenType.MULT),
            Map.entry("+", TokenType.PLUS),
            Map.entry(",", TokenType.COMMA),
            Map.entry("/", TokenType.DIV),
            Map.entry(":", TokenType.COLON),
            Map.entry(";", TokenType.SEMICN),
            Map.entry("<", TokenType.LSS),
            Map.entry(">", TokenType.GRE),
            Map.entry("[", TokenType.LBRACK),
            Map.entry("]", TokenType.RBRACK),
            Map.entry("{", TokenType.LBRACE),
            Map.entry("}", TokenType.RBRACE),
            Map.entry("-", TokenType.MINU),

            Map.entry("=", TokenType.ASSIGN)
    );

    public static Map<String, TokenType> getKeywords() {
        return keywords;
    }
}
