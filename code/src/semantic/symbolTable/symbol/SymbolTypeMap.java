package semantic.symbolTable.symbol;

import java.util.Map;

public class SymbolTypeMap {
    private static final Map<String, SymbolType> symbolTypeMap = Map.ofEntries(
        Map.entry("Int", SymbolType.Int),
        Map.entry("ConstInt", SymbolType.ConstInt),
        Map.entry("StaticInt", SymbolType.StaticInt),
        Map.entry("IntArray",SymbolType.IntArray),
        Map.entry("ConstIntArray", SymbolType.ConstIntArray),
        Map.entry("StaticIntArray", SymbolType.StaticIntArray),
        Map.entry("Char", SymbolType.Char),
        Map.entry("ConstChar", SymbolType.ConstChar),
        Map.entry("StaticChar", SymbolType.StaticChar),
        Map.entry("CharArray", SymbolType.CharArray),
        Map.entry("ConstCharArray", SymbolType.ConstCharArray),
        Map.entry("StaticCharArray", SymbolType.StaticCharArray),
        Map.entry("VoidFunc", SymbolType.VoidFunc),
        Map.entry("IntFunc", SymbolType.IntFunc),
        Map.entry("CharFunc", SymbolType.CharFunc)
    );

    public static Map<String, SymbolType> getSymbolTypeMap() { return symbolTypeMap; }
}
