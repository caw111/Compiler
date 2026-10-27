package semantic.symbolTable.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/* 栈式符号管理当前这一层所拥有的符号 */
public class StackSymbolTable {
    private Map<String, Symbol> SymbolStack = new LinkedHashMap<>();

    public Map<String, Symbol> getSymbolStack() {
        return SymbolStack;
    }

    public Collection<Symbol> getSymbols() {
        return SymbolStack.values();
    }

    public void setSymbolStack(Map<String, Symbol> symbolStack) {
        SymbolStack = symbolStack;
    }

    public void addSymbol(Symbol symbol) {
        getSymbolStack().put(symbol.getSymbolName(), symbol);
    }

    // 判断这个符号是否在这个表中
    public boolean isHaveSymbol(Symbol symbol) {
        Symbol tmp = getSymbolStack().get(symbol.getSymbolName());
        if(tmp == null) {
            return false;
        }
        return true;
    }
}