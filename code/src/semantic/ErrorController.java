package semantic;

import semantic.symbolTable.symbol.Symbol;
import semantic.symbolTable.symbol.SymbolTable;
import syntax.AST.AstNode;

import java.util.ArrayList;
import java.util.List;

/* 记录语义分析中出现的错误 */
public class ErrorController {
    private List<ErrorToken> errorTokenList = new ArrayList<>();
    private String errorContent = "";

    public ErrorController() {}

    public ErrorController(String errorContent) {
        this.errorContent = errorContent;
    }

    public void addError(String s) {
        errorContent = errorContent + s;
    }

    public String getErrorContent() {
        return errorContent;
    }
}
