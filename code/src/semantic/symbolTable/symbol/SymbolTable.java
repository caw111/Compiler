package semantic.symbolTable.symbol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class SymbolTable {
    private final HashMap<Integer, StackSymbolTable> symbolTables = new HashMap<>();  // 每个分析器独立的符号表

    public HashMap<Integer, StackSymbolTable> getSymbolTables() {
        return symbolTables;
    }

    public SymbolTable() {
        symbolTables.put(0, new StackSymbolTable());
    }

    public boolean addSymbol(Symbol symbol) {
        // 如果当前层为空，新建一个 symbolTables
        StackSymbolTable stackSymbolTable = symbolTables.computeIfAbsent(
                symbol.getScopeLevel(),
                id -> new StackSymbolTable()
        );

        // 如果返回 true 表示成功插入一个空位置，返回 false 表示重定义
        return stackSymbolTable.getSymbolStack().putIfAbsent(symbol.getSymbolName(), symbol) == null;
    }

    // 对这个符号进行检查，看看是否存在错误
    public boolean isHaveSymbol(Symbol symbol) {
        StackSymbolTable stackSymbolTable = symbolTables.get(symbol.getScopeLevel());
        if(stackSymbolTable.isHaveSymbol(symbol)) {
            return true;
        }
        return false;
    }

    public Symbol findInScope(String symbolName, int scopeLevel) {
        StackSymbolTable stackSymbolTable = symbolTables.get(scopeLevel);
        if(stackSymbolTable == null) {
            return null;
        }
        return stackSymbolTable.getSymbolStack().get(symbolName);
    }

    public void printSymbolTable(BufferedWriter fwOut) throws IOException {
        List<Integer> scopeLevels = new ArrayList<>(symbolTables.keySet());
        Collections.sort(scopeLevels);

        for(Integer scopeLevel : scopeLevels) {
            StackSymbolTable stackSymbolTable = symbolTables.get(scopeLevel);
            for(Symbol symbol : stackSymbolTable.getSymbols()) {
                if (!symbol.getIsBuiltin() && !"main".equals(symbol.getSymbolName())) {
                    fwOut.write(scopeLevel + " " + symbol.getSymbolName() + " " + symbol.getSymbolType() + "\n");
                }
            }
        }
    }
}
