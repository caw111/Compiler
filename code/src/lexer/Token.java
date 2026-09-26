package lexer;

public final class Token {
    private final TokenType type;   // Token 的类型
    private final String lexeme;    // 字符文本
    private final int line;         // 文本所在行数
    // private final int column;       // 文本所在行的第几个

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        if(this.type != TokenType.a) {
            if(this.type != TokenType.NOTE) {
                return this.type + " " + lexeme + "\n";
            } else {
                return "";
            }
        } else {
            return this.line + " a\n";
        }
    }

//        @Override
//        public String toString() {
//            return this.type + " " + lexeme + "\n";
//        }

}
