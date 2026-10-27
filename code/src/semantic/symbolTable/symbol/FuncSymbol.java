package semantic.symbolTable.symbol;

import semantic.SemanticAnalyse;

import java.util.List;

public class FuncSymbol extends Symbol {
    List<SymbolType> list;
    private SemanticAnalyse.ExprType returnType;

    public FuncSymbol(String symbolName, SymbolType symbolType, int symbolLevel, int scopeLevel, List<SymbolType> list, SemanticAnalyse.ExprType returnType) {
        super(symbolName, symbolType, symbolLevel, scopeLevel);
        this.list = list;
    }

    public List<SymbolType> getList() { return list; }
}
