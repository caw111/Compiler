package lexer;

public class Lexer {
    private final String source;
    private int pos = 0;
    private int line = 1;
    private String errorContent = "";

    public Lexer(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    private void error() {
        errorContent += line + " a\n";
    }

    public String getErrorContent() {
        return errorContent;
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private void updateLine() {
        if(peek() == '\n') line++;
    }

    private char peek() {
        if(!isAtEnd()) {
            return source.charAt(pos);
        }
        else return '\0';
    }

    private char nextPeek() {
        if(pos+1 >= source.length()) {
            return '\0';
        }
        return source.charAt(pos+1);
    }

    private char advance() {
        if(isAtEnd()) return '\0';
        return source.charAt(pos++);
    }

    private void skipWhiteSpace() {
        while(!isAtEnd()) {
            updateLine();
            char ch = peek();
            if(ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
                advance();
            } else {
                break;
            }
        }
    }

    public Token nextToken() {
        skipWhiteSpace();

        // System.out.println(pos);
        char c = peek();
        if(c == '\0') return null;

        if(Character.isLetter(c) || c == '_') {
            return scanIdentifier();
        }

        if(Character.isDigit(c)) {
            return scanNumber();
        }

        return scanSymbol();
    }

    private Token scanIdentifier() {
        int start = pos;

        while(!isAtEnd() && ( Character.isLetterOrDigit(peek()) || peek()=='_')) {
            advance();
        }

        String lexeme = source.substring(start, pos) ;
        TokenType type = TokenTypeMap.getKeywords().getOrDefault(lexeme, TokenType.IDENFR);

        return new Token(type, lexeme, line);
    }

    private Token scanNumber() {
        int start = pos;

        while(!isAtEnd() && Character.isDigit(peek())) {
            advance();
        }

        String lexeme = source.substring(start, pos);

        TokenType type = TokenType.INTCON;

        return new Token(type, lexeme, line);
    }

    private Token scanSymbol() {
        char c = advance();
        boolean isEscape = false; // 记录当前是否会发生转义
        int start ;
        switch(c) {
            case '+':
                return new Token(TokenType.PLUS, "+", line);
            case '-':
                return new Token(TokenType.MINU, "-", line);
            case '*':
                return new Token(TokenType.MULT, "*", line);
            case ':':
                return new Token(TokenType.COLON, ":", line);
            case ';':
                return new Token(TokenType.SEMICN, ";", line);
            case '[':
                return new Token(TokenType.LBRACK, "[", line);
            case ']':
                return new Token(TokenType.RBRACK, "]", line);
            case '{':
                return new Token(TokenType.LBRACE, "{", line);
            case '}':
                return new Token(TokenType.RBRACE, "}", line);
            case ',':
                return new Token(TokenType.COMMA, ",", line);
            case '(':
                return new Token(TokenType.LPARENT, "(", line);
            case ')':
                return new Token(TokenType.RPARENT, ")", line);
            case '%':
                return new Token(TokenType.MOD, "%", line);
            case '!':
                if (!isAtEnd() && peek() == '=') {
                    advance();
                    return new Token(TokenType.NEQ, "!=", line);
                }
                return new Token(TokenType.NOT, "!", line);
            case '<':
                if(!isAtEnd() && peek() == '=') {
                    advance();
                    return new Token(TokenType.LEQ, "<=", line);
                }
                return new Token(TokenType.LSS, "<", line);
            case '=':
                if(!isAtEnd() && peek() == '=') {
                    advance();
                    return new Token(TokenType.EQL, "==", line);
                }
                return new Token(TokenType.ASSIGN, "=", line);
            case '>':
                if(!isAtEnd() && peek() == '=') {
                    advance();
                    return new Token(TokenType.GEQ, ">=", line);
                }
                return new Token(TokenType.GRE, ">", line);
            case '|':
                if(!isAtEnd() && peek() == '|') {
                    advance();
                    return new Token(TokenType.OR, "||", line);
                }
                error();
                return new Token(TokenType.OR, "||", line);
            case '&':
                if(!isAtEnd() && peek() == '&') {
                    advance();
                    return new Token(TokenType.AND, "&&", line);
                }
                error();
                return new Token(TokenType.AND, "&&", line);
            case '\'':
                start = pos-1;
                while(!isAtEnd()) {
                    if(peek() == '\\') {
                        isEscape = !isEscape;
                        advance();
                    }
                    if(peek() != '\'') {
                        if(isEscape) isEscape=false;
                        advance();
                    } else if(isEscape) {
                        isEscape=false;
                        advance();
                    } else {
                        advance();
                        break;
                    }
                }
                return new Token(TokenType.CHARCON, source.substring(start, pos), line);
            case '\"':
                start = pos-1;
                while(!isAtEnd()) {
                    if(peek() == '\\') {
                        isEscape = !isEscape;
                        advance();
                    }
                    if(peek() != '\"') {
                        if(isEscape) isEscape=false;
                        advance();
                    } else if(isEscape) {
                        isEscape=false;
                        advance();
                    } else {
                        advance();
                        break;
                    }
                }
                return new Token(TokenType.STRCON, source.substring(start, pos), line);
            case '/' :
                if(!isAtEnd()) {
                    if(peek() == '/') {
                        do {
                            advance();
                        } while (!isAtEnd() && peek() != '\n');
                        return new Token(TokenType.NOTE, "", line);
                    } else if(peek() == '*') {
                        do {
                            if(peek() == '\n') updateLine();
                            advance();
                            // System.out.println(peek() + " " + nextPeek());
                        } while (!isAtEnd() && !(peek() == '*' && nextPeek() == '/'));
                        advance();
                        advance();
                        return new Token(TokenType.NOTE, "", line);
                    } else {
                        return new Token(TokenType.DIV, "/", line);
                    }
                }
            default:
                return new Token(TokenType.a, Character.toString(c), line);
        }
    }
}
