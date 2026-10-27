package semantic.symbolTable.symbol;

import java.util.HashMap;
import java.util.Map;

public class Symbol {
    private String symbolName ;
    private SymbolType symbolType ;
    private int symbolLevel ;
    private int scopeLevel ;
    private boolean isBuiltin = false;

    public Symbol(String symbolName,SymbolType symbolType, int symbolLevel, int scopeLevel) {
        this.symbolLevel = symbolLevel;
        this.symbolType = symbolType;
        this.symbolName = symbolName;
        this.scopeLevel = scopeLevel;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public int getSymbolLevel() {
        return symbolLevel;
    }

    public int getScopeLevel() { return scopeLevel; }

    public void setIsBuiltin(boolean isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    public boolean getIsBuiltin() {
        return isBuiltin;
    }
}
