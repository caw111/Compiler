package syntax;

import lexer.Token;
import lexer.TokenType;
import syntax.AST.AstNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/* 语法分析类 */
public class Parser {
    private final List<Token> tokenList;
    private String errorContent = "";
    private int pos = 0;
    private AstNode root;
    private final Deque<AstNode> nodeStack = new ArrayDeque<>();

    public Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public AstNode start() {
        compUnit();
        return root;
    }

    public void error(TokenType tokenType, Token token) {
        if(tokenType == TokenType.SEMICN) {
            errorContent += token.getLine() + " i\n";
        } else if(tokenType == TokenType.RPARENT) {
            errorContent += token.getLine() + " j\n";
        } else if(tokenType == TokenType.RBRACK) {
            errorContent += token.getLine() + " k\n";
        }
    }

    private boolean isAtEnd() {
        return pos >= tokenList.size();
    }

    private Token peek() {
        if(isAtEnd()) return null;
        return tokenList.get(pos);
    }

    private Token peek(int x) {
        if(pos + x >= 0 && pos + x < tokenList.size())
            return tokenList.get(pos+x);
        return null;
    }

    private Token nextPeek() {
        if(pos+1 >= tokenList.size()) return null;
        return tokenList.get(pos+1);
    }

    private Token advance() {
        Token token;
        if(!isAtEnd()) {
            token = tokenList.get(pos++);
            if(!nodeStack.isEmpty()) {
                nodeStack.peek().addChild(token);
            }
            return token;
        }
        return null;
    }

    private TokenType peekType() {
        if(isAtEnd()) return null;
        return tokenList.get(pos).getType();
    }

    // 可以向后偏移x个单位
    private TokenType peekType(int x) {
        if(pos + x >= tokenList.size()) return null;
        return tokenList.get(pos+x).getType();
    }

    private boolean checkType(TokenType tokenType) {
        if(isAtEnd()) return false;
        return tokenType == peekType();
    }

    private void expect(TokenType tokenType) {
        if(peekType() == tokenType) {
            advance();
        } else {
            Token token = peek(-1);
            error(tokenType, token);
        }
    }

    /* 边解析 边建立语法树AST*/
    private void enterNode(String name) {
        AstNode node = new AstNode(name);

        if(nodeStack.isEmpty()) {
            root = node;
        } else {
            nodeStack.peek().addChild(node);
        }

        nodeStack.push(node);
    }

    private void exitNode() {
        nodeStack.pop();
    }

    // 将已解析的内容包装成左操作数，当前节点继续接收运算符和右操作数。
    private void wrapLeftOperand() {
        AstNode current = nodeStack.peek();
        AstNode left = new AstNode(current.getName());
        left.getChildren().addAll(current.getChildren());
        current.getChildren().clear();
        current.addChild(left);
    }


    private boolean isFuncTypeStart() {
        return checkType(TokenType.VOIDTK)
                || checkType(TokenType.INTTK)
                || checkType(TokenType.CHARTK);
    }

    private boolean isFuncDefStart() {
        return isFuncTypeStart()
                && peekType(1) == TokenType.IDENFR
                && peekType(2) == TokenType.LPARENT;
    }

    private void compUnit() {
        enterNode("<CompUnit>");

        while (isDeclStart()) {
            decl();
        }

        while (isFuncDefStart()) {
            funcDef();
        }

        mainFuncDef();

        exitNode();
    }

    private void decl() {
        // enterNode("<Decl>");

        if(checkType(TokenType.CONSTTK)) {
            constDecl();
        } else if(checkType(TokenType.STATICTK) || checkType(TokenType.INTTK) || checkType(TokenType.CHARTK)){
            varDecl();
        }

        // exitNode();
    }

    private void constDecl() {
        enterNode("<ConstDecl>");

        advance();
        bType();
        constDef();
        while(checkType(TokenType.COMMA)) {
            advance();
            constDef();
        }
        expect(TokenType.SEMICN);

        exitNode();
    }

    private void bType() {
        // enterNode("<BType>");

        if(checkType(TokenType.INTTK)) {
            advance();
        } else if(checkType(TokenType.CHARTK)) {
            advance();
        }

        // exitNode();
    }

    private void constDef() {
        enterNode("<ConstDef>");

        advance();
        if(checkType(TokenType.LBRACK)) {
            advance();
            constExp();
            expect(TokenType.RBRACK);
        }
        advance();
        constInitVal();

        exitNode();
    }

    private void constInitVal() {
        enterNode("<ConstInitVal>");

        if(checkType(TokenType.STRCON)) {
            advance();
        } else if(checkType(TokenType.LBRACE)) {
            advance();
            if(!checkType(TokenType.RBRACE)) {
                constExp();
                while(checkType(TokenType.COMMA)) {
                    advance();
                    constExp();
                }
            }
            advance();
        } else {
            constExp();
        }

        exitNode();
    }

    private void varDecl() {
        enterNode("<VarDecl>");

        if(checkType(TokenType.STATICTK)) {
            advance();
        }
        bType();
        varDef();
        while(checkType(TokenType.COMMA)) {
            advance();
            varDef();
        }
        expect(TokenType.SEMICN);

        exitNode();
    }

    private void varDef() {
        enterNode("<VarDef>");

        advance();
        if(checkType(TokenType.LBRACK)) {
            advance();
            constExp();
            expect(TokenType.RBRACK);
        }
        if(checkType(TokenType.ASSIGN)) {
            advance();
            initVal();
        }

        exitNode();
    }

    private void initVal() {
        enterNode("<InitVal>");

        if(checkType(TokenType.STRCON)) {
            advance();
        } else if(checkType(TokenType.LBRACE)) {
            advance();
            if(!checkType(TokenType.RBRACE)) {
                exp();
                while(checkType(TokenType.COMMA)) {
                    advance();
                    exp();
                }
            }
            advance();
        } else {
            exp();
        }

        exitNode();
    }

    private void funcDef() {
        enterNode("<FuncDef>");

        funcType();
        advance();
        advance();
        if(peekType() == TokenType.INTTK || peekType() == TokenType.CHARTK) {
            funcFParams();
        }
        expect(TokenType.RPARENT);
        block();

        exitNode();
    }

    private void mainFuncDef() {
        enterNode("<MainFuncDef>");

        advance();
        advance();
        advance();
        expect(TokenType.RPARENT);
        block();

        exitNode();
    }

    private void funcType() {
        enterNode("<FuncType>");

        advance();

        exitNode();
    }

    private void funcFParams() {
        enterNode("<FuncFParams>");

        funcFParam();
        while(checkType(TokenType.COMMA)) {
            advance();
            funcFParam();
        }

        exitNode();
    }

    private void funcFParam() {
        enterNode("<FuncFParam>");

        bType();
        advance();
        if(checkType(TokenType.LBRACK)) {
            advance();
            expect(TokenType.RBRACK);
        }

        exitNode();
    }

    private void block() {
        enterNode("<Block>");

        advance();
        while(!isAtEnd() && !checkType(TokenType.RBRACE)) {
            blockItem();
        }

        if (checkType(TokenType.RBRACE)) {
            advance();
        }

        exitNode();
    }

    private boolean isDeclStart() {
        if (checkType(TokenType.CONSTTK)
                || checkType(TokenType.STATICTK)) {
            return true;
        }
        if (checkType(TokenType.INTTK)
                || checkType(TokenType.CHARTK)) {
            // FuncDef: FuncType IDENT '('
            if (peekType(1) == TokenType.IDENFR
                    && peekType(2) == TokenType.LPARENT) {
                return false;
            }
            // MainFuncDef
            if (peekType(1) == TokenType.MAINTK) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void blockItem() {
        // enterNode("<BlockItem>");

        if(isDeclStart()) {
            decl();
        } else {
            stmt();
        }

        // exitNode();
    }

    private boolean isAssignStmt() {
        if (!checkType(TokenType.IDENFR)) {
            return false;
        }
        // IDENT 后面的 token
        int i = pos + 1;
        if (i >= tokenList.size()) {
            return false;
        }
        TokenType type = tokenList.get(i).getType();

        // a = 1
        if (type == TokenType.ASSIGN) {
            return true;
        }
        // a + 1 / foo() ...
        if (type != TokenType.LBRACK) {
            return false;
        }
        // 到这里说明形如 a[ ... ]
        int bracketDepth = 1;
        i++;
        while (i < tokenList.size()) {
            type = tokenList.get(i).getType();
            if (type == TokenType.SEMICN || type == TokenType.RBRACE) {
                return false;
            }
            if (type == TokenType.LBRACK) {
                bracketDepth++;
            }
            else if (type == TokenType.RBRACK) {
                bracketDepth--;
                // 找到了最外层 a[...] 的 ]
                if (bracketDepth == 0) {
                    i++;
                    return i < tokenList.size()
                            && tokenList.get(i).getType()
                            == TokenType.ASSIGN;
                }
            }
            else if (type == TokenType.ASSIGN) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void stmt() {
        enterNode("<Stmt>");

        TokenType type = peekType();

        if (type == null) {
            exitNode();
            return;
        }

        switch(type) {
            case IFTK :
                advance();
                advance();
                cond();
                expect(TokenType.RPARENT);
                stmt();
                if(checkType(TokenType.ELSETK)) {
                    advance();
                    stmt();
                }
                break;
            case WHILETK:
                advance();
                advance();
                cond();
                expect(TokenType.RPARENT);
                stmt();
                break;
            case SWITCHTK:
                advance();
                advance();
                exp();
                expect(TokenType.RPARENT);
                advance();
                while (checkType(TokenType.CASETK)
                        || checkType(TokenType.DEFAULTTK)) {
                    caseStmt();
                }
                expect(TokenType.RBRACE);
                break;
            case BREAKTK, CONTINUETK:
                advance();
                expect(TokenType.SEMICN);
                break;
            case RETURNTK:
                advance();
                if(isExpStart()) {
                    exp();
                }
                expect(TokenType.SEMICN);
                break;
            case PRINTFTK:
                advance();                       // printf
                advance();                       // (
                advance();                       // STRINGCONST

                while (checkType(TokenType.COMMA)) {
                    advance();
                    exp();
                }

                expect(TokenType.RPARENT);
                expect(TokenType.SEMICN);
                break;
            case LBRACE:
                block();
                break;
            default:
                if(isAssignStmt()) {
                    lVal();
                    advance();
                    exp();
                    expect(TokenType.SEMICN);
                } else {
                    if(!checkType(TokenType.SEMICN)) {
                        exp();
                    }
                    expect(TokenType.SEMICN);
                }
                break;
        }
        exitNode();
    }

    private boolean isExpStart() {
        if(peekType() == TokenType.IDENFR || peekType() == TokenType.LPARENT
        || peekType() == TokenType.PLUS || peekType() == TokenType.MINU
        || peekType() == TokenType.NOT
        || peekType() == TokenType.INTCON || peekType() == TokenType.CHARCON) {
            return true;
        }
        return false;
    }

    private boolean islValStart() {
        if(peekType() == TokenType.IDENFR) return true;
        return false;
    }

    private boolean isStmtStart() {
        if(peekType() == TokenType.IFTK || peekType() == TokenType.WHILETK
        || peekType() == TokenType.SWITCHTK || peekType() == TokenType.BREAKTK
        || peekType() == TokenType.CONTINUETK || peekType() == TokenType.RETURNTK
        || peekType() == TokenType.PRINTFTK || peekType() == TokenType.LBRACE
        || isExpStart() || islValStart() || peekType() == TokenType.SEMICN
        ) {
            return true;
        }
        return false;
    }

    private void caseStmt() {
        enterNode("<CaseStmt>");

        if(checkType(TokenType.CASETK)) {
            advance();
            number();
            advance();
            while(isStmtStart()) {
                stmt();
            }
        } else if(checkType(TokenType.DEFAULTTK)) {
            advance();
            advance();
            while(isStmtStart()) {
                stmt();
            }
        }

        exitNode();
    }

    private void exp() {
        enterNode("<Exp>");

        addExp();

        exitNode();
    }

    private void cond() {
        enterNode("<Cond>");

        lOrExp();

        exitNode();
    }

    private void lVal() {
        enterNode("<LVal>");

        advance();
        if(peekType() == TokenType.LBRACK) {
            advance();
            exp();
            expect(TokenType.RBRACK);
        }

        exitNode();
    }

    private void primaryExp() {
        enterNode("<PrimaryExp>");

        if(checkType(TokenType.LPARENT)) {
            advance();
            exp();
            expect(TokenType.RPARENT);
        } else if(checkType(TokenType.IDENFR)) {
            lVal();
        } else {
            number();
        }

        exitNode();
    }

    private void number() {
        if (!checkType(TokenType.INTCON) && !checkType(TokenType.CHARCON)) {
            throw new IllegalStateException("Expected integer or character constant, got " + peek());
        }
        enterNode("<Number>");

        advance();

        exitNode();
    }

    private boolean isUnaryOpStart() {
        if(checkType(TokenType.PLUS) || checkType(TokenType.MINU) || checkType(TokenType.NOT)) {
            return true;
        }
        return false;
    }

    private boolean isCastExp() {
        if (peekType() != TokenType.LPARENT) {
            return false;
        }

        return peekType(1) == TokenType.INTTK
                || peekType(1) == TokenType.CHARTK;
    }

    private void unaryExp() {
        enterNode("<UnaryExp>");

        // IDENT '(' funcRParams? ')'
        if (peekType() == TokenType.IDENFR
                && peekType(1) == TokenType.LPARENT) {

            advance();                         // IDENT
            expect(TokenType.LPARENT);      // (

            if (isExpStart()) {
                funcRParams();
            }

            expect(TokenType.RPARENT);      // )
        }
        // unaryOp unaryExp
        else if (peekType() == TokenType.PLUS
                || peekType() == TokenType.MINU
                || peekType() == TokenType.NOT) {

            unaryOp();
            unaryExp();
        }
        // '(' bType ')' unaryExp
        else if (isCastExp()) {
            advance();

            bType();

            expect(TokenType.RPARENT);

            unaryExp();
        }
        // primaryExp
        else {
            primaryExp();
        }

        exitNode();
    }

    private void unaryOp() {
        enterNode("<UnaryOp>");

        advance();

        exitNode();
    }

    private void funcRParams() {
        enterNode("<FuncRParams>");

        exp();
        while(checkType(TokenType.COMMA)) {
            advance();
            exp();
        }

        exitNode();
    }

    private void mulExp() {
        enterNode("<MulExp>");

        unaryExp();
        while(checkType(TokenType.MULT) || checkType(TokenType.DIV) || checkType(TokenType.MOD)) {
            wrapLeftOperand();
            advance();
            unaryExp();
        }

        exitNode();
    }

    private void addExp() {
        enterNode("<AddExp>");

        mulExp();
        while(checkType(TokenType.PLUS) || checkType(TokenType.MINU)) {
            wrapLeftOperand();
            advance();
            mulExp();
        }

        exitNode();
    }

    private void relExp() {
        enterNode("<RelExp>");

        addExp();
        while(checkType(TokenType.LSS) || checkType(TokenType.GRE)
                || checkType(TokenType.LEQ) || checkType(TokenType.GEQ)) {
            wrapLeftOperand();
            advance();
            addExp();
        }

        exitNode();
    }

    private void eqExp() {
        enterNode("<EqExp>");

        relExp();
        while(checkType(TokenType.EQL) || checkType(TokenType.NEQ)) {
            wrapLeftOperand();
            advance();
            relExp();
        }

        exitNode();
    }

    private void lAndExp() {
        enterNode("<LAndExp>");

        eqExp();
        while(checkType(TokenType.AND)) {
            wrapLeftOperand();
            advance();
            eqExp();
        }

        exitNode();
    }

    private void lOrExp() {
        enterNode("<LOrExp>");

        lAndExp();
        while(checkType(TokenType.OR)) {
            wrapLeftOperand();
            advance();
            lAndExp();
        }

        exitNode();
    }

    private void constExp() {
        enterNode("<ConstExp>");

        addExp();

        exitNode();
    }

    public String getErrorContent() {
        return errorContent;
    }
}
