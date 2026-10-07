import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import syntax.Parser;
import syntax.AST.AstNode;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

// 从 code 目录运行：java -cp <编译输出目录> ExpressionOutputCheck
public class ExpressionOutputCheck {
    private static final String[] LEVELS = {
            "<MulExp>", "<AddExp>", "<RelExp>", "<EqExp>", "<LAndExp>", "<LOrExp>"
    };

    private static AstNode parse(String source) {
        Lexer lexer = new Lexer(source);
        var tokens = new ArrayList<Token>();
        Token token;
        while ((token = lexer.nextToken()) != null) {
            if (token.getType() != TokenType.NOTE) tokens.add(token);
        }
        Parser parser = new Parser(tokens);
        AstNode root = parser.start();
        if (!lexer.getErrorContent().isEmpty() || !parser.getErrorContent().isEmpty()) {
            throw new AssertionError(lexer.getErrorContent() + parser.getErrorContent());
        }
        return root;
    }

    private static AstNode find(AstNode node, String name) {
        if (name.equals(node.getName())) return node;
        for (AstNode child : node.getChildren()) {
            AstNode found = find(child, name);
            if (found != null) return found;
        }
        return null;
    }

    private static String operand(int value, int level) {
        String text = "INTCON " + value + "\n<Number>\n<PrimaryExp>\n<UnaryExp>\n";
        for (int i = 0; i < level; i++) text += LEVELS[i] + "\n";
        return text;
    }

    private static void check(String expression, int level, String expected) throws Exception {
        AstNode root = parse("int main(){if(" + expression + ");return 0;}");
        StringWriter text = new StringWriter();
        BufferedWriter writer = new BufferedWriter(text);
        find(root, LEVELS[level]).printTree(writer);
        writer.flush();
        if (!text.toString().equals(expected)) {
            throw new AssertionError(expression + "\nExpected:\n" + expected + "Actual:\n" + text);
        }
    }

    public static void main(String[] args) throws Exception {
        String[][] operators = {
                {"*", "MULT", "/", "DIV"}, {"+", "PLUS", "-", "MINU"},
                {"<", "LSS", ">=", "GEQ"}, {"==", "EQL", "!=", "NEQ"},
                {"&&", "AND", "&&", "AND"}, {"||", "OR", "||", "OR"}
        };
        for (int level = 0; level < LEVELS.length; level++) {
            String tag = LEVELS[level] + "\n";
            String[] op = operators[level];
            check("1", level, operand(1, level) + tag);
            check("1" + op[0] + "2" + op[2] + "3", level,
                    operand(1, level) + tag + op[1] + " " + op[0] + "\n"
                            + operand(2, level) + tag + op[3] + " " + op[2] + "\n"
                            + operand(3, level) + tag);
        }
        check("1+2+3", 1, operand(1, 1) + "<AddExp>\nPLUS +\n"
                + operand(2, 1) + "<AddExp>\nPLUS +\n" + operand(3, 1) + "<AddExp>\n");
        check("1+2*3", 1, operand(1, 1) + "<AddExp>\nPLUS +\n"
                + operand(2, 0) + "<MulExp>\nMULT *\n" + operand(3, 0) + "<MulExp>\n<AddExp>\n");
        for (int i = 1; i <= 6; i++) {
            parse(Files.readString(Path.of("src/test/testfile" + i + ".txt")));
        }
        System.out.println("14 exact expression output checks and 6 sample files passed.");
    }
}
