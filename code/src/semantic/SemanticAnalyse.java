package semantic;

import lexer.Token;
import lexer.TokenType;
import semantic.symbolTable.symbol.*;
import syntax.AST.AstNode;

import java.util.*;

/* 语义分析主程序，这个地方主要是收集符号建立符号表 */
public class SemanticAnalyse {
    private AstNode root ;
    private SymbolTable symbolTable = new SymbolTable();
    private int level = 1;
    private int scopeId = 1; // 当前所在作用域
    private int nextScopeId = 1;    // 下一个进入的作用域
    private Deque<Integer> scopeStack = new ArrayDeque<>();
    private ErrorController errorController = new ErrorController();
    private ExprType currentExprType = ExprType.VOID;
    // 当前处于多少层 while 循环中
    private int loopDepth = 0;
    // 当前处于多少层 switch 语句中
    private int switchDepth = 0;

    public enum ExprType {
        INT,
        CHAR,
        INT_ARRAY,
        CHAR_ARRAY,
        VOID,
        ERROR
    }

    // 记录错误
    public void report(Token token, String code) {
        errorController.addError(token.getLine() + " " + code + "\n");
    }

    // 处理 b 类错误 ： 重命名
    private void declare(Symbol symbol, Token token) {
        if(!symbolTable.addSymbol(symbol)) {
            report(token, "b");
        }
    }

    // 处理 c 类错误 ： 符号未定义
    // 在作用域（包括父作用域）中查找符号
    public Symbol findSymbol(String name) {
        Symbol symbol = null;
        symbol = symbolTable.findInScope(name, scopeId);
        if(symbol != null) {
            return symbol;
        }
        for(int parentId : scopeStack) {
            symbol = symbolTable.findInScope(name, parentId);
            if(symbol != null) {
                return symbol;
            }
        }
        return null;
    }

    // 检查是否未定义
    private void checkDefined(Token token) {
        Symbol tmp = findSymbol(token.getLexeme());
        if(tmp == null) {
            report(token,"c");
        }
    }

    // 检查函数调用参数数量和类型是否正确
    private ExprType checkFuncCall(AstNode node) {
        Token token = node.getChildren().get(0).getToken();

        Symbol symbol = findSymbol(token.getLexeme());

        // 获取实参类型
        List<ExprType> actualTypes = new ArrayList<>();

        for (AstNode child : node.getChildren()) {
            if ("<FuncRParams>".equals(child.getName())) {
                for (AstNode param : child.getChildren()) {
                    if ("<Exp>".equals(param.getName())) {
                        actualTypes.add(analyseExpr(param));
                    }
                }
            }
        }
        // c：未定义函数
        if (symbol == null) {
            report(token, "c");
            return ExprType.ERROR;
        }
        // e：变量当作函数使用
        if (!(symbol instanceof FuncSymbol)) {
            report(token, "e");
            return ExprType.ERROR;
        }
        FuncSymbol funcSymbol = (FuncSymbol) symbol;

        List<SymbolType> formalTypes = funcSymbol.getList();

        // d：参数数量不匹配
        if (actualTypes.size() != formalTypes.size()) {
            report(token, "d");
            return ExprType.ERROR;
        }
        // e：参数类型不匹配
        boolean hasError = false;

        for (int i = 0; i < actualTypes.size(); i++) {
            ExprType actual = actualTypes.get(i);
            ExprType formal = variableType(formalTypes.get(i));

            if (actual == ExprType.ERROR) {
                hasError = true;
                continue;
            }

            if (actual != formal) {
                report(token, "e");
                return ExprType.ERROR;
            }
        }

        if (hasError) {
            return ExprType.ERROR;
        }

        return switch (funcSymbol.getSymbolType()) {
            case IntFunc -> ExprType.INT;
            case CharFunc -> ExprType.CHAR;
            case VoidFunc -> ExprType.VOID;
            default -> ExprType.ERROR;
        };
    }

    public ErrorController getErrorController() {
        return errorController;
    }

    public String getErrorContent() {
        return errorController.getErrorContent();
    }

    public SemanticAnalyse(AstNode root) {
        this.root = root;
        initBuiltinFunctions();
    }

    private void initBuiltinFunctions() {
        List<SymbolType>list = new ArrayList<>();
        Symbol symbol = new FuncSymbol("get_int", SymbolType.IntFunc, 1, 1, list, ExprType.INT);
        symbol.setIsBuiltin(true);
        symbolTable.addSymbol(symbol);

        symbol = new FuncSymbol("get_char", SymbolType.CharFunc, 1, 1, list, ExprType.CHAR);
        symbol.setIsBuiltin(true);
        symbolTable.addSymbol(symbol);

        // 文档的名称栏使用 put_string，函数原型使用 put_str，兼容两个名字。
        String[] strings = {"get_string", "put_int", "put_char", "put_string", "put_str"};
        SymbolType[][] symbolLists = {
                {
                    SymbolType.CharArray, SymbolType.Int
                },
                {
                    SymbolType.Int
                },
                {
                    SymbolType.Char
                },
                {
                    SymbolType.CharArray
                },
                {
                    SymbolType.CharArray
                },
        };

        for(int i = 0; i < strings.length; i++) {
            symbol = new FuncSymbol(strings[i], SymbolType.VoidFunc, 1, 1, Arrays.stream(symbolLists[i]).toList(), ExprType.VOID);
            symbol.setIsBuiltin(true);
            symbolTable.addSymbol(symbol);
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    private void levelUp() {
        level++;
        // 保存父作用域编号
        scopeStack.push(scopeId);
        // 创建新的作用域编号
        scopeId = ++nextScopeId;
    }

    private void levelDown() {
        level--;
        // 恢复父作用域编号
        scopeId = scopeStack.pop();
    }

    // 开始语义分析
    public void start() {
        traverse(root);
    }

    private void traverse(AstNode node) {
        // traverse 建立参数表
        if(node.isTerminal()) {
            return;
        }

        if(node.getName().equals("<ConstDecl>")) {
            extractConstDecl(node);
            return;
        }

        if(node.getName().equals("<VarDecl>")){
            extractValDecl(node);
            return;
        }

        if(node.getName().equals("<FuncDef>")){
            extractFuncDef(node);
            return;
        }

        if (node.getName().equals("<Block>")) {
            extractBlock(node);
            return;
        }

        if (node.getName().equals("<MainFuncDef>")) {
            extractMainFuncDef(node);
            return;
        }

        if ("<Exp>".equals(node.getName())
                || "<ConstExp>".equals(node.getName())
                || "<Cond>".equals(node.getName())
                || "<LVal>".equals(node.getName())
                || "<UnaryExp>".equals(node.getName())) {

            analyseExpr(node);
            return;
        }

        if ("<Stmt>".equals(node.getName())) {
            analyseStmt(node);
            return;
        }

        for (AstNode child : node.getChildren()) {
            traverse(child);
        }
    }

    // constDecl ： 对常量符号的处理
    private void extractConstDecl(AstNode node) {
        boolean isConstant = false;
        String type = null;
        for(AstNode child : node.getChildren()) {
            if(child.isTerminal() && child.getToken().getType() == TokenType.CONSTTK) {
                isConstant = true;
            }
            if (child.isTerminal()
                    && (child.getToken().getType() == TokenType.INTTK
                    || child.getToken().getType() == TokenType.CHARTK)) {
                type = getBType(child);
            }
            if(!child.isTerminal() && child.getName().equals("<ConstDef>")) {
                extractConstDef(type, child, isConstant);
            }
        }
    }

    private void extractConstDef(String type, AstNode node, boolean isConstant) {
        boolean isArray = false;
        Token token = node.getChildren().get(0).getToken();
        String symbolName = "";
        for(AstNode child : node.getChildren()) {
            if(child.isTerminal() && child.getToken().getType() == TokenType.IDENFR) {
                symbolName = child.getToken().getLexeme();
            }
            if(child.isTerminal() && child.getToken().getType() == TokenType.LBRACK) {
                isArray = true;
            }
            if ("<ConstExp>".equals(child.getName())) {
                traverse(child);
            }
            // 先不用管这个变量的值
        }
        SymbolType symbolType = getType(isConstant, false, type, isArray);
        Symbol symbol = new Symbol(symbolName, symbolType, level, scopeId);
        declare(symbol, token);

        for (AstNode child : node.getChildren()) {
            if ("<ConstInitVal>".equals(child.getName())) {
                checkInitVal(child, variableType(symbolType));
            }
        }
    }

    // 将对应的 BType 的位置转换成对应的类型
    private String getBType(AstNode node) {
        Token token = node.isTerminal()
                ? node.getToken()
                : node.getChildren().get(0).getToken();

        return switch (token.getType()) {
            case INTTK -> "Int";
            case CHARTK -> "Char";
            case VOIDTK -> "Void";
            default -> throw new IllegalStateException("不是类型关键字");
        };
    }

    // 获得语义分析对应的变量
    private SymbolType getType(boolean isConstant, boolean isStatic, String btype, boolean isArray) {
        String typeName = "";
        if(isConstant) typeName = "Const";
        else if(isStatic) typeName = "Static";

        typeName += btype;

        if(isArray) typeName += "Array";
        return SymbolTypeMap.getSymbolTypeMap().get(typeName);
    }

    // 对变量声明的处理
    private void extractValDecl(AstNode node) {
        boolean isStatic = false;
        String type = null;
        for(AstNode child : node.getChildren()) {
            if(child.isTerminal() && child.getToken().getType() == TokenType.STATICTK) {
                isStatic = true;
            }
            if (child.isTerminal()
                    && (child.getToken().getType() == TokenType.INTTK
                    || child.getToken().getType() == TokenType.CHARTK)) {
                type = getBType(child);
            }
            if(!child.isTerminal() && child.getName().equals("<VarDef>")) {
                extractValDef(isStatic, type, child);
            }
        }
    }

    private void extractValDef(boolean isStatic, String type, AstNode node) {
        boolean isArray = false;
        Token token = node.getChildren().get(0).getToken();
        String symbolName = "";
        for(AstNode child : node.getChildren()) {
            if(child.isTerminal() && child.getToken().getType() == TokenType.IDENFR) {
                symbolName = child.getToken().getLexeme();
            }
            if(child.isTerminal() && child.getToken().getType() == TokenType.LBRACK) {
                isArray = true;
            }
            if ("<ConstExp>".equals(child.getName())) {
                traverse(child);
            }
        }
        SymbolType symbolType = getType(false, isStatic, type, isArray);
        Symbol symbol = new Symbol(symbolName, symbolType, level, scopeId);
        declare(symbol, token);

        // 登记当前变量后，再检查初始化表达式
        for (AstNode child : node.getChildren()) {
            if ("<InitVal>".equals(child.getName())) {
                checkInitVal(child, variableType(symbolType));
            }
        }
    }

    // 检查这个 initVal 这个部分类型和左类型是否匹配
    private void checkInitVal(AstNode node, ExprType type) {
        ExprType elementType = switch (type) {
            case INT_ARRAY -> ExprType.INT;
            case CHAR_ARRAY -> ExprType.CHAR;
            default -> type;
        };

        Set<Integer> typeErrorLines = new HashSet<>();
        for (AstNode child : node.getChildren()) {
            boolean mismatch;
            if (child.isTerminal() && child.getToken().getType() == TokenType.STRCON) {
                // 按本实验说明，字符串可用于初始化 char 或 char[]。
                mismatch = type != ExprType.CHAR && type != ExprType.CHAR_ARRAY;
            } else if ("<Exp>".equals(child.getName())
                    || "<ConstExp>".equals(child.getName())) {
                ExprType actual = analyseExpr(child);
                mismatch = actual != ExprType.ERROR && actual != elementType;
            } else {
                continue;
            }
            if (mismatch) {
                Token valueToken = firstToken(child);
                // 同一行不重复记录初值类型错误，但继续检查后面的元素。
                if (typeErrorLines.add(valueToken.getLine())) report(valueToken, "e");
            }
        }
    }

    private Token firstToken(AstNode node) {
        while (!node.isTerminal()) node = node.getChildren().get(0);
        return node.getToken();
    }

    private void extractFuncDef(AstNode node) {
        // 这个地方要注意参数的插入顺序：先函数，后函数的参数，最后是block中的内容，顺序不能错误
        String type = null;
        String symbolName = "";
        AstNode paramsNode = null;
        AstNode blockNode = null;
        Token token = null;
        for (AstNode child : node.getChildren()) {
            if (!child.isTerminal() && child.getName().equals("<FuncType>")) {
                type = getBType(child);
            }
            if (child.isTerminal()
                    && child.getToken().getType() == TokenType.IDENFR) {
                symbolName = child.getToken().getLexeme();
                token = child.getToken();
            }
            if (!child.isTerminal() && child.getName().equals("<FuncFParams>")) {
                paramsNode = child;
            }
            if (!child.isTerminal() && child.getName().equals("<Block>")) {
                blockNode = child;
            }
        }
        // 2. 仅提取形参类型，不插入形参符号
        List<SymbolType> paramTypes = new ArrayList<>();
        if (paramsNode != null) {
            paramTypes = collectParamTypes(paramsNode);
        }
        // 3. 注册函数符号（level = 1）
        SymbolType symbolType = getFuncType(type);
        FuncSymbol funcSymbol = new FuncSymbol(
                symbolName, symbolType, level, scopeId, paramTypes, variableType(getFuncType(type))
        );
        declare(funcSymbol, token);

        // 4. 进入函数作用域
        ExprType previousType = currentExprType;
        currentExprType = variableType(getFuncType(type));
        levelUp();
        scopeId = nextScopeId;
        // 5. 注册形参
        if (paramsNode != null) {
            extractFuncFParams(paramsNode);
        }
        // 6. 遍历函数体子节点
        if (blockNode != null) {
            for (AstNode child : blockNode.getChildren()) {
                traverse(child);
            }
        }
        if(currentExprType == ExprType.CHAR || currentExprType == ExprType.INT) {
            checkMissingReturn(blockNode);
        }
        // 7. 退出函数作用域
        levelDown();
        currentExprType = previousType;
    }

    // 检查 g 类错误：有返回值的函数末尾缺少 return
    private void checkMissingReturn(AstNode blockNode) {
        if (blockNode == null) {
            return;
        }
        List<AstNode> children = blockNode.getChildren();
        AstNode lastNode = null;
        Token rightBrace = null;
        // 找到 Block 最后一个有效语句或声明，以及右大括号
        for (AstNode child : children) {
            if (!child.isTerminal()) {
                lastNode = child;
            }
            if (child.isTerminal()
                    && child.getToken().getType() == TokenType.RBRACE) {
                rightBrace = child.getToken();
            }
        }
        boolean hasReturn = false;
        // 最后一项必须是 Stmt
        if (lastNode != null && "<Stmt>".equals(lastNode.getName())) {
            AstNode first = lastNode.getChildren().get(0);
            if (first.isTerminal()
                    && first.getToken().getType() == TokenType.RETURNTK) {
                hasReturn = true;
            }
        }
        if (!hasReturn && rightBrace != null) {
            report(rightBrace, "g");
        }
    }

    private List<SymbolType> collectParamTypes(AstNode node) {
        List<SymbolType> paramTypes = new ArrayList<>();
        for (AstNode child : node.getChildren()) {
            if (child.isTerminal() || !"<FuncFParam>".equals(child.getName())) {
                continue;
            }
            String btype = null;
            boolean isArray = false;
            for (AstNode paramChild : child.getChildren()) {
                if (paramChild.isTerminal()
                        && (paramChild.getToken().getType() == TokenType.INTTK
                        || paramChild.getToken().getType() == TokenType.CHARTK)) {
                    btype = getBType(paramChild);
                }
                if (paramChild.isTerminal()
                        && paramChild.getToken().getType()
                        == TokenType.LBRACK) {
                    isArray = true;
                }
            }
            SymbolType type = getType(false, false, btype, isArray);
            paramTypes.add(type);
        }
        return paramTypes;
    }

    private SymbolType getFuncType(String btype) {
        String typeName = "";
        typeName += btype;
        typeName += "Func";
        return SymbolTypeMap.getSymbolTypeMap().get(typeName);
    }

    private List<SymbolType> extractFuncFParams(AstNode node) {
        List<SymbolType> paramTypes = new ArrayList<>();
        for(AstNode child : node.getChildren()) {
            if(!child.isTerminal() && child.getName().equals("<FuncFParam>")) {
                paramTypes.add(extractFuncFParam(child));
            }
        }
        return paramTypes;
    }

    private SymbolType extractFuncFParam(AstNode node) {
        String btype = null;
        boolean isArray = false;
        String symbolName = "";
        Token token = null;
        for(AstNode child : node.getChildren()) {
            if (child.isTerminal()
                    && (child.getToken().getType() == TokenType.INTTK
                    || child.getToken().getType() == TokenType.CHARTK)) {
                btype = getBType(child);
            }
            if(child.isTerminal() && child.getToken().getType() == TokenType.LBRACK) {
                isArray = true;
            }
            if(child.isTerminal() && child.getToken().getType() == TokenType.IDENFR) {
                symbolName = child.getToken().getLexeme();
                token = child.getToken();
            }
        }
        SymbolType symbolType = getType(false, false, btype, isArray);
        Symbol symbol = new Symbol(symbolName, symbolType, level, scopeId);
        declare(symbol, token);
        return symbolType;
    }

    private void extractBlock(AstNode node) {
        levelUp();

        for(AstNode child : node.getChildren()) {
            traverse(child);
        }

        levelDown();
    }

    private void extractMainFuncDef(AstNode node) {
        String symbolName = "main";
        Token token = null;
        AstNode blockNode = null;
        SymbolType symbolType = getFuncType("Int");
        for(AstNode child : node.getChildren()) {
            if(child.isTerminal() && child.getToken().getType() == TokenType.MAINTK) {
                token = child.getToken();
            }
            if("<Block>".equals(child.getName())) {
                blockNode = child;
            }
        }
        List<SymbolType> paramTypes = new ArrayList<>();
        Symbol symbol = new FuncSymbol(symbolName, symbolType, level, scopeId, paramTypes, ExprType.INT);
        declare(symbol, token);

        ExprType previousType = currentExprType;
        currentExprType = ExprType.INT;

        for(AstNode child : node.getChildren()) {
            if(!child.isTerminal()) {
                traverse(child);
            }
        }
        checkMissingReturn(blockNode);
        currentExprType = previousType;
    }

    // 对表达式进行处理，找出表达式内部的问题，同时返回表达式的类型
    private ExprType analyseExpr(AstNode node) {
        return switch (node.getName()) {
            case "<Exp>", "<ConstExp>", "<Cond>" ->
                    analyseExpr(node.getChildren().get(0));

            case "<Number>" -> {
                Token token = node.getChildren().get(0).getToken();
                yield token.getType() == TokenType.INTCON
                        ? ExprType.INT
                        : ExprType.CHAR;
            }
            case "<PrimaryExp>" -> {
                AstNode first = node.getChildren().get(0);

                // '(' Exp ')'：分析中间的 Exp
                if (first.isTerminal()) {
                    yield analyseExpr(node.getChildren().get(1));
                }

                // LVal 或 Number
                yield analyseExpr(first);
            }

            case "<LVal>" -> analyseLVal(node);

            case "<MulExp>", "<AddExp>" ->
                    analyseArithmeticExpr(node);

            case "<UnaryExp>" -> analyseUnaryExpr(node);

            case "<RelExp>", "<EqExp>",
                 "<LAndExp>", "<LOrExp>" ->
                    analyseConditionExpr(node);

            default -> throw new IllegalStateException(
                    "尚未处理的表达式节点：" + node.getName()
            );
        };
    }

    private ExprType analyseArithmeticExpr(AstNode node) {
        List<AstNode> children = node.getChildren();

        // 没有实际运算，直接传递孩子的类型
        if (children.size() == 1) {
            return analyseExpr(children.get(0));
        }

        // 先分析两边，保证右边的错误也能被发现
        ExprType left = analyseExpr(children.get(0));
        ExprType right = analyseExpr(children.get(2));

        Token operator = children.get(1).getToken();

        // 子表达式已经出错，不再由当前运算追加类型错误
        if (left == ExprType.ERROR || right == ExprType.ERROR) {
            return ExprType.ERROR;
        }

        boolean sameType = left == right;
        boolean basicType = left == ExprType.INT
                || left == ExprType.CHAR;

        if (!sameType || !basicType) {
            report(operator, "e");
            return ExprType.ERROR;
        }

        // INT 运算得到 INT，CHAR 运算得到 CHAR
        return left;
    }

    private ExprType variableType(SymbolType type) {
        return switch (type) {
            case Int, ConstInt, StaticInt, IntFunc -> ExprType.INT;
            case Char, ConstChar, StaticChar, CharFunc -> ExprType.CHAR;

            case IntArray, ConstIntArray, StaticIntArray ->
                    ExprType.INT_ARRAY;

            case CharArray, ConstCharArray, StaticCharArray ->
                    ExprType.CHAR_ARRAY;

            case VoidFunc ->  ExprType.VOID;

            default -> throw new IllegalStateException(
                    "不是变量或常量类型：" + type
            );
        };
    }

    // 对 LVal 进行分析，如果是没有[]的数组，返回类型为对应的array，其余的返回对应的类型
    private ExprType analyseLVal(AstNode node) {
        Token token = node.getChildren().get(0).getToken();
        Symbol symbol = findSymbol(token.getLexeme());

        // 检查数组下标表达式
        boolean hasIndex = false;
        ExprType indexType = null;

        for (AstNode child : node.getChildren()) {
            if ("<Exp>".equals(child.getName())) {
                hasIndex = true;
                indexType = analyseExpr(child);
            }
        }

        // c：未定义的名字
        if (symbol == null) {
            report(token, "c");
            return ExprType.ERROR;
        }

        // e：函数当作变量使用
        if (symbol instanceof FuncSymbol) {
            report(token, "e");
            return ExprType.ERROR;
        }

        ExprType type = variableType(symbol.getSymbolType());

        // 没有数组下标
        if (!hasIndex) {
            return type;
        }

        // 下标内部已经出错
        if (indexType == ExprType.ERROR) {
            return ExprType.ERROR;
        }

        // e：数组下标必须是 int
        if (indexType != ExprType.INT) {
            report(token, "e");
            return ExprType.ERROR;
        }

        // e：非数组变量不能使用下标
        return switch (type) {
            case INT_ARRAY -> ExprType.INT;
            case CHAR_ARRAY -> ExprType.CHAR;
            default -> {
                report(token, "e");
                yield ExprType.ERROR;
            }
        };
    }

    private ExprType analyseUnaryExpr(AstNode node) {
        List<AstNode> children = node.getChildren();
        AstNode first = children.get(0);

        // 情况一：PrimaryExp，返回对应类型
        if ("<PrimaryExp>".equals(first.getName())) {
            return analyseExpr(first);
        }

        // 情况二：函数调用，
        if (first.isTerminal()
                && first.getToken().getType() == TokenType.IDENFR) {
            return checkFuncCall(node);
        }

        // 情况三：一元运算 + - !
        if ("<UnaryOp>".equals(first.getName())) {
            return analyseUnaryOp(node);
        }

        // 情况四：强制类型转换 (int) 或 (char)
        if (first.isTerminal()
                && first.getToken().getType() == TokenType.LPARENT) {
            return analyseCastExpr(node);
        }

        throw new IllegalStateException(
                "无法识别的 UnaryExp 节点"
        );
    }

    private ExprType analyseUnaryOp(AstNode node) {
        List<AstNode> children = node.getChildren();

        // UnaryOp UnaryExp
        AstNode operand = children.get(1);

        ExprType type = analyseExpr(operand);

        if (type == ExprType.ERROR) {
            return ExprType.ERROR;
        }

        // 一元运算不能直接作用于整个数组或 void
        if (type == ExprType.INT_ARRAY
                || type == ExprType.CHAR_ARRAY
                || type == ExprType.VOID) {

            Token operator = children.get(0)
                    .getChildren().get(0).getToken();

            report(operator, "e");
            return ExprType.ERROR;
        }

        Token operator = children.get(0).getChildren().get(0).getToken();
        return operator.getType() == TokenType.NOT ? ExprType.INT : type;
    }

    private ExprType transFromStringType(String type) {
        switch (type) {
            case "Int": return ExprType.INT;
            case "Char": return ExprType.CHAR;
            case "IntArray": return ExprType.INT_ARRAY;
            case "CharArray": return ExprType.CHAR_ARRAY;
            case "Void": return ExprType.VOID;
            default: return ExprType.ERROR;
        }
    }

    private ExprType analyseCastExpr(AstNode node) {
        List<AstNode> children = node.getChildren();

        ExprType targetType = ExprType.ERROR;
        AstNode operand = children.get(children.size() - 1);

        for (AstNode child : children) {
            if (child.isTerminal() &&
                    (child.getToken().getType() == TokenType.CHARTK
                            || child.getToken().getType() == TokenType.INTTK
                            || child.getToken().getType() == TokenType.VOIDTK)) {
                String type = getBType(child);
                targetType = transFromStringType(type);
                break;
            }
        }

        // 分析被转换的表达式
        ExprType sourceType = analyseExpr(operand);

        if (sourceType == ExprType.ERROR) {
            return ExprType.ERROR;
        }

        if (sourceType != ExprType.INT
                && sourceType != ExprType.CHAR) {
            return ExprType.ERROR;
        }

        return targetType;
    }

    private ExprType analyseConditionExpr(AstNode node) {
        List<AstNode> children = node.getChildren();

        if (children.size() == 1) {
            return analyseExpr(children.get(0));
        }

        ExprType left = analyseExpr(children.get(0));
        ExprType right = analyseExpr(children.get(2));

        if (left == ExprType.ERROR || right == ExprType.ERROR) {
            return ExprType.ERROR;
        }

        if (left != right
                || (left != ExprType.INT && left != ExprType.CHAR)) {
            Token operator = children.get(1).getToken();
            report(operator, "e");
            return ExprType.ERROR;
        }

        // 关系运算和逻辑运算产生真假值 0 或 1
        return ExprType.INT;
    }

    // 处理 stmt 中可能出现的错误
    private void analyseStmt(AstNode node) {
        List<AstNode> children = node.getChildren();
        AstNode first = children.get(0);
        if("<LVal>".equals(children.get(0).getName())) {
            checkAssignment(node);
            return ;
        }
        if (first.isTerminal()) {
            TokenType type = first.getToken().getType();
            switch (type) {
                case RETURNTK -> {
                    checkReturn(node);
                    return;
                }
                case SWITCHTK -> {
                    switchDepth++;
                    try {
                        checkSwitch(node);
                    } finally {
                        switchDepth--;
                    }
                    return;
                }
                case PRINTFTK -> {
                    checkPrintf(node);
                    return;
                }
                case WHILETK -> {
                    loopDepth++;
                    try {
                        for(var child : node.getChildren()) {
                            traverse(child);
                        }
                    } finally {
                        loopDepth--;
                    }
                    return ;
                }
                case BREAKTK -> {
                    // break 必须处于 while 或 switch 中
                    if (loopDepth == 0 && switchDepth == 0) {
                        report(first.getToken(), "m");
                    }
                    return;
                }
                case CONTINUETK -> {
                    // continue 必须处于 while 中
                    if (loopDepth == 0) {
                        report(first.getToken(), "m");
                    }
                    return;
                }
            }
        }
        for (AstNode child : children) {
            traverse(child);
        }
    }

    // 处理 stmt -> lVal = Exp ; 这个语法错误
    private void checkAssignment(AstNode node) {
        AstNode lValNode = null;
        AstNode expNode = null;
        for (AstNode child : node.getChildren()) {
            if ("<LVal>".equals(child.getName())) {
                lValNode = child;
            } else if ("<Exp>".equals(child.getName())) {
                expNode = child;
            }
        }
        if (lValNode == null || expNode == null) {
            return;
        }

        Token lValToken = lValNode.getChildren().get(0).getToken();
        Symbol lValSymbol = findSymbol(lValToken.getLexeme());

        if(lValSymbol != null && isConstant(lValSymbol)) {
           report(lValToken, "h");
        }

        ExprType left = analyseExpr(lValNode);
        ExprType right = analyseExpr(expNode);
        if (left == ExprType.ERROR || right == ExprType.ERROR) {
            return;
        }
        if (left != right) {
            Token token = lValNode.getChildren().get(0).getToken();
            report(token, "e");
        }
    }

    // 判断一个符号是否为常量
    private boolean isConstant(Symbol symbol) {
        return switch (symbol.getSymbolType()) {
            case ConstInt, ConstChar,
                 ConstIntArray, ConstCharArray -> true;
            default -> false;
        };
    }

    private void checkReturn(AstNode node) {
        Token returnToken = node.getChildren().get(0).getToken();
        AstNode expNode = null;
        for (AstNode child : node.getChildren()) {
            if ("<Exp>".equals(child.getName())) {
                expNode = child;
                break;
            }
        }
        // return; 不存在返回表达式
        if (expNode == null) {
            if (currentExprType != ExprType.VOID) {
                report(returnToken, "e");
            }
            return;
        }
        // 情况二：return Exp;
        ExprType actualType = analyseExpr(expNode);
        // void 函数返回了表达式，属于 f 类错误
        if (currentExprType == ExprType.VOID) {
            report(returnToken, "f");
            return;
        }
        // 表达式内部已经发生错误，不重复报告
        if (actualType == ExprType.ERROR) {
            return;
        }
        // 非 void 函数，返回类型不一致属于 e 类错误
        if (actualType != currentExprType) {
            report(returnToken, "e");
        }
    }

    private void checkSwitch(AstNode node) {
        ExprType switchType = ExprType.ERROR;
        // 获取 switch 表达式的类型
        for (AstNode child : node.getChildren()) {
            if ("<Exp>".equals(child.getName())) {
                switchType = analyseExpr(child);
                break;
            }
        }
        // switch 表达式必须是 int 或 char
        if (switchType != ExprType.INT
                && switchType != ExprType.CHAR) {
            if (switchType != ExprType.ERROR) {
                Token token = node.getChildren().get(0).getToken();
                report(token, "e");
            }
            switchType = ExprType.ERROR;
        }

        Set<Integer> switchSet = new HashSet<>();
        boolean isHaveDefault = false;

        // 4. 遍历 CaseStmt
        for (AstNode child : node.getChildren()) {
            if (!"<CaseStmt>".equals(child.getName())) {
                continue;
            }

            AstNode first = child.getChildren().get(0);
            Token token = first.getToken();

            if (token.getType() == TokenType.CASETK) {

                // 获取当前 case 的 Number
                for (AstNode caseChild : child.getChildren()) {
                    if ("<Number>".equals(caseChild.getName())) {
                        ExprType caseType = analyseExpr(caseChild);
                        if (caseType == switchType) {
                            int value = getCaseValue(caseChild);
                            if (!switchSet.add(value)) {
                                report(token, "n");
                            }
                        }
                    }
                }
            } else if (token.getType() == TokenType.DEFAULTTK) {
                // 第二次及以后出现 default
                if (isHaveDefault) {
                    report(token, "n");
                }
                isHaveDefault = true;
            }
            // 保留已有的 e 类检测和内部语句遍历
            checkCaseStmt(child, switchType);
        }
    }

    private int getCaseValue(AstNode node) {
        Token token = node.getChildren().get(0).getToken();
        String value = token.getLexeme();

        if (token.getType() == TokenType.INTCON) {
            return Integer.parseInt(value);
        }

        String content = value.substring(1, value.length() - 1);

        if (content.length() == 1) {
            return content.charAt(0);
        }

        return switch (content) {
            case "\\n" -> '\n';
            case "\\t" -> '\t';
            case "\\r" -> '\r';
            case "\\0" -> '\0';
            case "\\\\" -> '\\';
            case "\\'" -> '\'';
            default -> throw new IllegalStateException(
                    "非法字符常量：" + value
            );
        };
    }

    // 检查 case 的类型，并继续分析内部语句
    private void checkCaseStmt(AstNode node, ExprType switchType) {
        List<AstNode> children = node.getChildren();
        AstNode first = children.get(0);
        // 1. 判断是否为 case 分支
        if (first.isTerminal()
                && first.getToken().getType() == TokenType.CASETK) {
            // 2. 找到 Number
            for (AstNode child : children) {
                if ("<Number>".equals(child.getName())) {
                    ExprType caseType = analyseExpr(child);
                    // 3. 类型不匹配，报告 e
                    if (switchType != ExprType.ERROR
                            && caseType != switchType) {
                        report(first.getToken(), "e");
                    }
                    break;
                }
            }
        }
        // 4. 继续分析 case/default 内部的语句
        for (AstNode child : children) {
            if ("<Stmt>".equals(child.getName())) {
                traverse(child);
            }
        }
    }

    // 检查 printf 中格式说明符与实参的数量、类型是否匹配
    private void checkPrintf(AstNode node) {
        List<AstNode> children = node.getChildren();

        Token printfToken = children.get(0).getToken();

        String formatString = null;

        // 记录格式说明符所要求的类型
        List<ExprType> formalTypes = new ArrayList<>();

        // 记录实参表达式的实际类型
        List<ExprType> actualTypes = new ArrayList<>();

        // 1. 获取格式字符串和实参类型
        for (AstNode child : children) {

            if (child.isTerminal()
                    && child.getToken().getType() == TokenType.STRCON) {
                formatString = child.getToken().getLexeme();
            }
            if ("<Exp>".equals(child.getName())) {
                actualTypes.add(analyseExpr(child));
            }
        }
        if (formatString == null) {
            return;
        }
        // 2. 提取格式说明符
        for (int i = 0; i < formatString.length() - 1; i++) {
            if (formatString.charAt(i) != '%') {
                continue;
            }
            char format = formatString.charAt(i + 1);
            switch (format) {
                case 'd' -> formalTypes.add(ExprType.INT);
                case 'c' -> formalTypes.add(ExprType.CHAR);
                case 's' -> formalTypes.add(ExprType.CHAR_ARRAY);
            }
            // 跳过已经处理的格式字符
            i++;
        }
        // 3. l 类错误：数量不匹配
        if (formalTypes.size() != actualTypes.size()) {
            report(printfToken, "l");
            return;
        }
        // 4. e 类错误：类型不匹配
        for (int i = 0; i < formalTypes.size(); i++) {
            ExprType formal = formalTypes.get(i);
            ExprType actual = actualTypes.get(i);
            // 子表达式已经出现错误，不重复报告
            if (actual == ExprType.ERROR) {
                continue;
            }
            if (formal != actual) {
                report(printfToken, "e");
                return;
            }
        }
    }
}
